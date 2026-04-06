package com.cyrev.iam.service;


import com.cyrev.common.entities.User;
import com.cyrev.common.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReconciliationService {

    private final UserRepository userRepository;

    public ReconciliationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public int reconcileWithEntra() {
        List<User> internalUsers = userRepository.findAll();
        int reconciledCount = 0;
        return reconciledCount;
    }
}
