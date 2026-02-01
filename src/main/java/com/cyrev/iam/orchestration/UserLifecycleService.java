package com.cyrev.iam.orchestration;

import com.cyrev.iam.adapters.AppProvisioningAdapter;
import com.cyrev.iam.domain.App;
import com.cyrev.iam.domain.Role;
import com.cyrev.iam.entities.User;
import com.cyrev.iam.domain.UserStatus;
import com.cyrev.iam.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserLifecycleService {

    private final Map<App, AppProvisioningAdapter> adapters;
    private final UserRepository repository;

    public UserLifecycleService(
        List<AppProvisioningAdapter> adapters,
        UserRepository repository
    ) {
        this.adapters = adapters.stream()
            .collect(Collectors.toMap(AppProvisioningAdapter::app, a -> a));
        this.repository = repository;
    }

    @Transactional
    public User createUser(User user, Role role) {
        User saved = repository.save(user);

        saved.getAssignedApps().forEach(app ->
            adapters.get(app).assignUser(saved, role)
        );

        return saved;
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = repository.findById(userId).orElseThrow();
        user.setStatus(UserStatus.SUSPENDED);

        user.getAssignedApps().forEach(app ->
            adapters.get(app).revokeUser(user)
        );
    }
}
