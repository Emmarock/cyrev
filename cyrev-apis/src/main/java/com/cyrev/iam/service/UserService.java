package com.cyrev.iam.service;

import com.cyrev.common.dtos.UserCreationDTO;
import com.cyrev.common.dtos.UserUpdateRequestDTO;
import com.cyrev.common.entities.Address;
import com.cyrev.common.entities.Organization;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.AddressRepository;
import com.cyrev.common.repository.OrganizationRepository;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.common.services.NotificationPublisherService;
import com.cyrev.iam.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final OrganizationMapper organizationMapper;
    private final NotificationPublisherService notificationPublisherService;
    private final EmailVerificationService emailVerificationService;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUser(UUID id) {
        return userRepository.findById(id).orElseThrow(()->new RuntimeException("Invalid user"));
    }

    @Transactional
    public User createUser(UserCreationDTO userCreationDTO) throws BadRequestException {
        if(userRepository.findByEmail(userCreationDTO.getUsername()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }
        if(userRepository.findByUsername(userCreationDTO.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }
        if(organizationRepository.existsByCode(userCreationDTO.getOrganization().getCode())) {
            throw new BadRequestException("Organization already exists");
        }
        if(organizationRepository.findByName(userCreationDTO.getOrganization().getName()).isPresent()) {
            throw new BadRequestException("Organization already exists");
        }
        User entity = userMapper.toEntity(userCreationDTO);
        User user = userRepository.save(entity);
        // create organization
        Organization org = organizationMapper.toEntity(userCreationDTO.getOrganization(), user);
        Organization organization = organizationRepository.save(org);
        Address address = userMapper.toAddress(userCreationDTO.getCompanyAddress(), organization);
        addressRepository.save(address);
        user.setOrganization(organization);
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

}
