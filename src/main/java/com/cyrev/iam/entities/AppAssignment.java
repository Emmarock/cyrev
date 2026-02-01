package com.cyrev.iam.entities;

import com.cyrev.iam.domain.App;
import com.cyrev.iam.domain.AssignmentStatus;
import com.cyrev.iam.domain.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

@Entity
@Where(clause = "deleted = false")
@Table(
  name = "app_assignments",
  uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "app"})
)
@Getter
@Setter
public class AppAssignment extends BaseEntity{

    @ManyToOne(optional = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;
    @Enumerated(EnumType.STRING)
    private App app;
    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    @Lob
    private String failureReason;
}
