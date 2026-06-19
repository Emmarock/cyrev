package com.cyrev.iam.service;

import com.cyrev.common.dtos.*;
import com.cyrev.common.entities.Business;
import com.cyrev.common.entities.BusinessUser;
import com.cyrev.common.entities.User;
import com.cyrev.common.mapper.BusinessUserMapper;
import com.cyrev.common.repository.BusinessRepository;
import com.cyrev.common.repository.BusinessUserRepository;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.common.services.NotificationPublisherService;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import com.cyrev.iam.exceptions.BadRequestException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessUserService {

    private static final List<Role> APPROVER_ROLES = List.of(Role.ADMIN, Role.SUPER_ADMIN);
    private static final String GRAPH_USERS_URI = "/users";

    private final BusinessUserRepository businessUserRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ResilientGraphClient graphClient;
    private final NotificationPublisherService notificationPublisher;
    private final BusinessUserMapper businessUserMapper;

    /**
     * Relationship-manager flow: persists a local business_user record in PENDING_APPROVAL
     * and notifies admins. The Entra account is not provisioned here — it is created when
     * an admin approves the request.
     */
    @Transactional
    public BusinessUserDto onboardBusinessUser(UUID tenantInternalId, CreateBusinessUserDTO dto) {
        Business business = businessRepository.findWithLockingById(dto.getBusinessId())
                .orElseThrow(() -> new EntityNotFoundException("Business not found: " + dto.getBusinessId()));

        if (business.getTenant() == null || !tenantInternalId.equals(business.getTenant().getId())) {
            throw new BadRequestException("Business does not belong to this tenant");
        }
        if (business.getContractStatus() == ContractStatus.EXPIRED
                || business.getContractStatus() == ContractStatus.TERMINATED) {
            throw new BadRequestException("Cannot onboard users to a business with a "
                    + business.getContractStatus() + " contract");
        }

        BusinessUser manager = null;
        if (dto.getManagerId() != null) {
            manager = businessUserRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new BadRequestException("Manager not found: " + dto.getManagerId()));
            if (!business.getId().equals(manager.getBusiness().getId())) {
                throw new BadRequestException("Manager must belong to the same business");
            }
        }

        long nextSequence = business.getEmployeeIdSequence() + 1;
        String employeeId = formatEmployeeId(business, nextSequence);
        business.setEmployeeIdSequence(nextSequence);

        BusinessUser businessUser = BusinessUser.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .employeeId(employeeId)
                .business(business)
                .manager(manager)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .unit(dto.getUnit())
                .department(dto.getDepartment())
                .division(dto.getDivision())
                .identityStatus(IdentityStatus.PRE_JOINER)
                .approvalStatus(ApprovalStatus.PENDING_APPROVAL)
                .deleted(false)
                .build();

        BusinessUser saved = businessUserRepository.save(businessUser);
        log.info("Onboarded business user {} (employeeId={}) pending admin approval", saved.getId(), employeeId);

        notifyAdmins(tenantInternalId, saved, business);

        return businessUserMapper.toDto(saved);
    }

    @Transactional
    public BusinessUserDto approve(UUID tenantInternalId, UUID businessUserId, String approverPrincipal, String entraTenantId) {
        BusinessUser businessUser = requireBusinessUser(tenantInternalId, businessUserId);
        if (businessUser.getApprovalStatus() != ApprovalStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Business user is not pending approval");
        }

        String entraObjectId = createEntraAccount(entraTenantId, businessUser);
        businessUser.setEntraObjectId(entraObjectId);

        businessUser.setApprovalStatus(ApprovalStatus.APPROVED);
        businessUser.setApprovalDecidedBy(approverPrincipal);
        businessUser.setApprovalDecidedAt(Instant.now());
        businessUser.setIdentityStatus(IdentityStatus.ACTIVE);

        BusinessUser saved = businessUserRepository.save(businessUser);
        notifyDecision(saved, true, null);
        return businessUserMapper.toDto(saved);
    }

    @Transactional
    public BusinessUserDto reject(UUID tenantInternalId, UUID businessUserId, String approverPrincipal, String reason) {
        BusinessUser businessUser = requireBusinessUser(tenantInternalId, businessUserId);
        if (businessUser.getApprovalStatus() != ApprovalStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Business user is not pending approval");
        }
        if(StringUtils.isEmpty(reason)) {
            throw new BadRequestException("The must be a reason for rejection");
        }

        businessUser.setApprovalStatus(ApprovalStatus.REJECTED);
        businessUser.setApprovalDecidedBy(approverPrincipal);
        businessUser.setApprovalDecidedAt(Instant.now());
        businessUser.setApprovalReason(reason);
        businessUser.setIdentityStatus(IdentityStatus.LEAVER);

        BusinessUser saved = businessUserRepository.save(businessUser);
        notifyDecision(saved, false, reason);
        return businessUserMapper.toDto(saved);
    }

    /**
     * Offboarding: marks the business_user as PRE_LEAVER and disables their Entra account.
     */
    @Transactional
    public BusinessUserDto offboard(UUID tenantInternalId, UUID businessUserId, String entraTenantId) {
        BusinessUser businessUser = requireBusinessUser(tenantInternalId, businessUserId);
        if (businessUser.getIdentityStatus() == IdentityStatus.LEAVER) {
            throw new BadRequestException("Business user already offboarded");
        }

        disableEntraAccount(entraTenantId, businessUser);

        businessUser.setIdentityStatus(IdentityStatus.LEAVER);
        businessUser =  businessUserRepository.save(businessUser);
        return businessUserMapper.toDto(businessUser);
    }

    public List<BusinessUserDto> listPendingApprovals(UUID tenantInternalId) {
        return businessUserRepository
                .findAllByBusiness_Tenant_IdAndApprovalStatus(
                        tenantInternalId,
                        ApprovalStatus.PENDING_APPROVAL
                )
                .stream()
                .map(businessUserMapper::toDto)
                .toList();
    }

    public List<BusinessUserDto> listForBusiness(UUID businessId) {
        return businessUserRepository.findAllByBusiness_Id(businessId)
                .stream()
                .map(businessUserMapper::toDto)
                .toList();
    }

    public BusinessUserDto get(UUID tenantInternalId, UUID businessUserId) {
        return businessUserMapper.toDto(requireBusinessUser(tenantInternalId, businessUserId));
    }

    private BusinessUser requireBusinessUser(UUID tenantInternalId, UUID businessUserId) {
        return businessUserRepository.findByIdAndBusiness_Tenant_Id(businessUserId, tenantInternalId)
                .orElseThrow(() -> new EntityNotFoundException("Business user not found: " + businessUserId));
    }

    private String formatEmployeeId(Business business, long sequence) {
        String format = business.getEmployeeIdFormat();
        try {
            return String.format(format, business.getOrgCode(), sequence);
        } catch (Exception ex) {
            log.warn("Invalid employee id format '{}' on business {}, falling back to default",
                    format, business.getId());
            return String.format("%s-%05d", business.getOrgCode(), sequence);
        }
    }

    private String createEntraAccount(String entraTenantId, BusinessUser businessUser) {
        if (entraTenantId == null || entraTenantId.isBlank()) {
            log.warn("No Entra tenant id resolved; skipping Entra provisioning for employee {}",
                    businessUser.getEmployeeId());
            return null;
        }

        Business business = businessUser.getBusiness();
        String employeeId = businessUser.getEmployeeId();

        Map<String, Object> passwordProfile = new HashMap<>();
        passwordProfile.put("password", generateTemporaryPassword());
        passwordProfile.put("forceChangePasswordNextSignIn", true);

        Map<String, Object> body = new HashMap<>();
        body.put("accountEnabled", true);
        body.put("displayName", businessUser.getFirstName() + " " + businessUser.getLastName());
        body.put("mailNickname", employeeId.toLowerCase());
        body.put("userPrincipalName", employeeId.toLowerCase() + "@" + entraTenantId);
        body.put("givenName", businessUser.getFirstName());
        body.put("surname", businessUser.getLastName());
        body.put("department", businessUser.getDepartment());
        body.put("employeeId", employeeId);
        body.put("companyName", business == null ? null : business.getCompanyName());
        body.put("passwordProfile", passwordProfile);

        try {
            Map<String, Object> response = graphClient.postForBody(entraTenantId, GRAPH_USERS_URI, body);
            Object createdId = response == null ? null : response.get("id");
            return createdId == null ? null : createdId.toString();
        } catch (Exception ex) {
            log.error("Failed to provision Entra account for employee {}", employeeId, ex);
            throw new BadRequestException("Unable to provision user in Entra: " + ex.getMessage());
        }
    }

    private void disableEntraAccount(String entraTenantId, BusinessUser businessUser) {
        if (entraTenantId == null || businessUser.getEntraObjectId() == null) {
            return;
        }
        graphClient.patch(entraTenantId,
                GRAPH_USERS_URI + "/" + businessUser.getEntraObjectId(),
                Map.of("accountEnabled", false));
    }

    private void notifyAdmins(UUID tenantInternalId, BusinessUser businessUser, Business business) {
        List<User> admins = userRepository.findAllByTenant_IdAndRoleIn(tenantInternalId, APPROVER_ROLES);
        if (admins.isEmpty()) {
            log.warn("No admins to notify for tenant {} pending approval {}",
                    tenantInternalId, businessUser.getId());
            return;
        }

        String fullName = businessUser.getFirstName() + " " + businessUser.getLastName();
        admins.forEach(admin -> notificationPublisher.publishBusinessUserPendingApproval(
                admin.getEmail(),
                fullName,
                businessUser.getEmployeeId(),
                business.getCompanyName(),
                businessUser.getId()
        ));
    }

    private void notifyDecision(BusinessUser businessUser, boolean approved, String reason) {
        Business business = businessUser.getBusiness();
        if (business == null || business.getRelationshipOwner() == null) {
            return;
        }
        notificationPublisher.publishBusinessUserDecision(
                business.getRelationshipOwner().getEmail(),
                businessUser.getFirstName() + " " + businessUser.getLastName(),
                businessUser.getEmployeeId(),
                approved,
                reason
        );
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "!Aa1";
    }
}