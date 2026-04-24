package com.cyrev.iam.service;

import com.cyrev.common.dtos.CompleteSignupDTO;
import com.cyrev.common.dtos.UserCreationDTO;
import com.cyrev.common.dtos.UserUpdateRequestDTO;
import com.cyrev.common.entities.Address;
import com.cyrev.common.entities.Organization;
import com.cyrev.common.entities.SaasTenant;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.AddressRepository;
import com.cyrev.common.repository.OrganizationRepository;
import com.cyrev.common.repository.SaasTenantRepository;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.common.services.NotificationPublisherService;
import com.cyrev.iam.entra.service.onboarding.SaasTenantService;
import com.cyrev.iam.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final NotificationPublisherService notificationPublisherService;
    private final EmailVerificationService emailVerificationService;
    private final SaasTenantRepository saasTenantRepository;
    public List<User> getTenantAllUsers(UUID tenantId) {
        return userRepository.findAllByTenantId(tenantId);
    }

    public User getUser(UUID id) {
        return userRepository.findById(id).orElseThrow(()->new RuntimeException("Invalid user"));
    }

    public User findTenantUser(UUID id, UUID tenantId) {
        return userRepository.findUserByIdAndTenant_Id(id,tenantId).orElseThrow(()-> new BadRequestException("User not found in this tenant"));
    }

    @Transactional
    public void completeSignup(String tenantId, CompleteSignupDTO completeSignupDTO) {
        try{
            SaasTenant saasTenant =  saasTenantRepository.findByEntraTenantId(tenantId)
                    .orElseThrow(()->new BadRequestException("Tenant not found"));
            var user = userRepository.findByEmailAndTenant_Id(completeSignupDTO.getBusinessEmail(), saasTenant.getId())
                    .orElseThrow(()->new BadRequestException("User not found in this tenant"));
            log.info("about to create address with tenant id {} user id {}", saasTenant.getId(), user.getId());
            Address address = addressRepository.findByTenant_Id(saasTenant.getId())
                    .orElseGet(()->{
                        Address innerAddress = userMapper.toAddress(completeSignupDTO.getCompanyAddress());
                        innerAddress.setTenant(saasTenant);
                        addressRepository.save(innerAddress);
                        return innerAddress;
                    });
            log.info("Signup complete with tenant id {} address id {}", saasTenant.getId(), address.getId());
            notificationPublisherService.publishSignupEvent(user.getFirstName(), user.getEmail());
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    @Transactional
    public User createUser(String tenantId, UserCreationDTO userCreationDTO) throws BadRequestException {
        validateCorporateEmail(userCreationDTO.getBusinessEmail());
        if(userRepository.existsByEmail(userCreationDTO.getBusinessEmail())) {
            throw new BadRequestException("Email already exists");
        }
        if(userRepository.findByUsername(userCreationDTO.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }
        if(userRepository.findByEmailDomain(userCreationDTO.getBusinessEmail().split("@")[1]).isPresent()) {
            throw new BadRequestException("Your organization already exists in cyrev, please contact our customer support for more information.");
        }

        SaasTenant saasTenant = null;
        if(tenantId!=null) {
            saasTenant = saasTenantRepository.findByEntraTenantId(tenantId).orElseThrow(()->new BadRequestException("Tenant not found"));
        }
        User entity = userMapper.toEntity(userCreationDTO);
        entity.setTenant(saasTenant);
        User user = userRepository.save(entity);
        Address address = userMapper.toAddress(userCreationDTO.getCompanyAddress());
        address.setTenant(saasTenant);
        addressRepository.save(address);
        userRepository.save(user);
        // create email event here
        String verificationLink = emailVerificationService.generateVerificationLink(user);
        notificationPublisherService.publishVerificationEvent(user.getFirstName(), user.getEmail(), verificationLink);
        return user;
    }

    public User updateUser(UUID id, UserUpdateRequestDTO updated) {
        return userRepository.findById(id).map(user -> {
            if (updated.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(updated.getPassword()));
                user.setEmailVerified(true);
            }
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("Invalid user ID: " + id));
    }


    public boolean deleteUser(UUID id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return true;
        }).orElse(false);
    }

    public String extractDomain(String email) {
        return email.substring(email.indexOf("@") + 1).toLowerCase();
    }
    private static final Set<String> PUBLIC_EMAIL_DOMAINS = Set.of(
            "gmail.com",
            "yahoo.com",
            "hotmail.com",
            "outlook.com",
            "icloud.com",
            "aol.com",
            "protonmail.com"
    );
    public void validateCorporateEmail(String email) {
        String domain = extractDomain(email);

        if (PUBLIC_EMAIL_DOMAINS.contains(domain)) {
            throw new BadRequestException("Public email domains are not allowed. Please use your corporate email.");
        }
    }
}
