package com.cyrev.iam.service;


import com.cyrev.iam.entities.User;
import com.cyrev.iam.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReconciliationService {

    private final UserRepository userRepository;

    public ReconciliationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public int reconcileWithEntra() {
        // Example logic: fetch users from Microsoft Entra
        List<User> internalUsers = userRepository.findAll();
        // call external API to fetch Entra users (pseudo-code)
        // List<User> entraUsers = entraClient.fetchUsers();

        // Compare and sync
        int reconciledCount = 0;
        // reconciliation logic here
        return reconciledCount;
    }
}
