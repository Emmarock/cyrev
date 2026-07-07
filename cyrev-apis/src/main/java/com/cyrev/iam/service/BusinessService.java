package com.cyrev.iam.service;

import com.cyrev.common.dtos.BusinessResponseDTO;
import com.cyrev.common.dtos.ContractStatus;
import com.cyrev.common.dtos.CreateBusinessDTO;
import com.cyrev.common.dtos.UpdateBusinessDTO;
import com.cyrev.common.entities.Business;
import com.cyrev.common.entities.SaasTenant;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.BusinessRepository;
import com.cyrev.common.repository.SaasTenantRepository;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.iam.exceptions.BadRequestException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessService {

    private static final String DEFAULT_EMPLOYEE_ID_FORMAT = "%s-%05d";

    private final BusinessRepository businessRepository;
    private final SaasTenantRepository saasTenantRepository;
    private final UserRepository userRepository;

    private BusinessResponseDTO toDto(Business b) {
        User owner = b.getRelationshipOwner();
        return BusinessResponseDTO.builder()
                .id(b.getId())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .createdBy(b.getCreatedBy())
                .updatedBy(b.getUpdatedBy())
                .version(b.getVersion())
                .orgCode(b.getOrgCode())
                .companyName(b.getCompanyName())
                .contractStartDate(b.getContractStartDate())
                .contractEndDate(b.getContractEndDate())
                .contractStatus(b.getContractStatus())
                .employeeIdFormat(b.getEmployeeIdFormat())
                .employeeIdSequence(b.getEmployeeIdSequence())
                .relationshipOwnerId(owner.getId())
                .relationshipOwnerName(owner.getFirstName() + " " + owner.getLastName())
                .tenantId(b.getTenant().getId())
                .build();
    }

    @Transactional
    public BusinessResponseDTO registerBusiness(UUID tenantInternalId, CreateBusinessDTO dto) {
        String normalizedOrgCode = dto.getOrgCode().toUpperCase();

        if (businessRepository.existsByOrgCode(normalizedOrgCode)) {
            throw new BadRequestException("Business with org code '" + normalizedOrgCode + "' already exists");
        }

        if (dto.getContractEndDate() != null && dto.getContractEndDate().isBefore(dto.getContractStartDate())) {
            throw new BadRequestException("Contract end date cannot be before start date");
        }

        SaasTenant tenant = saasTenantRepository.findById(tenantInternalId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + tenantInternalId));

        User relationshipOwner = userRepository.findUserByIdAndTenant_Id(dto.getRelationshipOwnerId(), tenantInternalId)
                .orElseThrow(() -> new BadRequestException("Relationship owner not found in this tenant"));

        String employeeIdFormat = dto.getEmployeeIdFormat() == null || dto.getEmployeeIdFormat().isBlank()
                ? DEFAULT_EMPLOYEE_ID_FORMAT
                : dto.getEmployeeIdFormat();

        Business business = Business.builder()
                .orgCode(normalizedOrgCode)
                .companyName(dto.getCompanyName())
                .contractStartDate(dto.getContractStartDate())
                .contractEndDate(dto.getContractEndDate())
                .relationshipOwner(relationshipOwner)
                .tenant(tenant)
                .contractStatus(resolveInitialContractStatus(dto.getContractStartDate(), dto.getContractEndDate()))
                .employeeIdFormat(employeeIdFormat)
                .employeeIdSequence(0L)
                .deleted(false)
                .build();

        Business saved = businessRepository.save(business);
        log.info("Registered business {} ({}) under tenant {}", saved.getId(), normalizedOrgCode, tenantInternalId);
        return toDto(saved);
    }

    @Transactional
    public BusinessResponseDTO updateBusiness(UUID tenantInternalId, UUID businessId, UpdateBusinessDTO dto) {
        Business business = requireBusiness(tenantInternalId, businessId);

        if (dto.getCompanyName() != null) {
            business.setCompanyName(dto.getCompanyName());
        }
        if (dto.getContractStartDate() != null) {
            business.setContractStartDate(dto.getContractStartDate());
        }
        if (dto.getContractEndDate() != null) {
            business.setContractEndDate(dto.getContractEndDate());
        }
        if (dto.getContractStatus() != null) {
            business.setContractStatus(dto.getContractStatus());
        }
        if (dto.getRelationshipOwnerId() != null) {
            User owner = userRepository.findUserByIdAndTenant_Id(dto.getRelationshipOwnerId(), tenantInternalId)
                    .orElseThrow(() -> new BadRequestException("Relationship owner not found in this tenant"));
            business.setRelationshipOwner(owner);
        }

        if (business.getContractEndDate() != null
                && business.getContractEndDate().isBefore(business.getContractStartDate())) {
            throw new BadRequestException("Contract end date cannot be before start date");
        }

        return toDto(businessRepository.save(business));
    }

    @Transactional(readOnly = true)
    public List<BusinessResponseDTO> listForTenant(UUID tenantInternalId) {
        return businessRepository.findAllByTenant_Id(tenantInternalId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public BusinessResponseDTO getForTenant(UUID tenantInternalId, UUID businessId) {
        return toDto(requireBusiness(tenantInternalId, businessId));
    }

    private Business requireBusiness(UUID tenantInternalId, UUID businessId) {
        return businessRepository.findByIdAndTenant_Id(businessId, tenantInternalId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found: " + businessId));
    }

    private ContractStatus resolveInitialContractStatus(LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();
        if (start.isAfter(today)) {
            return ContractStatus.DRAFT;
        }
        if (end != null && end.isBefore(today)) {
            return ContractStatus.EXPIRED;
        }
        return ContractStatus.ACTIVE;
    }
}