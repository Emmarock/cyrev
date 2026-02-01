package com.cyrev.iam.service;

import com.cyrev.iam.domain.UserCreationDTO;
import com.cyrev.iam.domain.UserUpdateRequestDTO;
import com.cyrev.iam.entities.User;
import com.cyrev.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUser(UUID id) {
        return userRepository.findById(id).orElseThrow(()->new RuntimeException("Invalid user"));
    }

    public User createUser(UserCreationDTO userCreationDTO) {

        User entity = userMapper.toEntity(userCreationDTO);

        return userRepository.save(entity);
    }

    public User updateUser(UUID id, UserUpdateRequestDTO updated) {

        return userRepository.findById(id).map(user -> {
            if (updated.getManagerId() != null) {
                userRepository.findById(updated.getManagerId()).ifPresent(user::setManager);
            }

            if (updated.getEndDate() != null) {
                user.setEndDate(updated.getEndDate());
            }
            if (updated.getDepartment() != null) {
                user.setDepartment(updated.getDepartment());
            }
            if (updated.getDivision() != null) {
                user.setDivision(updated.getDivision());
            }
            if (updated.getUnit() != null) {
                user.setUnit(updated.getUnit());
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
