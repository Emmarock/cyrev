package com.cyrev.common.repository;

import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.Role;
import com.cyrev.common.entities.AppAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppAssignmentRepository
        extends JpaRepository<AppAssignment, String> {
    List<AppAssignment> findByManager_Id(UUID managerId);
    List<AppAssignment> findByUserId(UUID userId);
    Optional<AppAssignment> findByUserIdAndApp(UUID userId, App app);
    Optional<AppAssignment> findByAppAndRoleAndUserId(App app, Role role, UUID userId);

}
