package com.cyrev.common.entities;
import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.IdentityStatus;
import com.cyrev.common.dtos.Role;
import com.cyrev.common.dtos.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Where(clause = "deleted = false")
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Column
    private String password;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String employeeId; // e.g., ORG001-0001

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager; // direct manager

    private LocalDate startDate;
    private LocalDate endDate;

    private String department;
    private String unit;
    private String division;

    @Enumerated(EnumType.STRING)
    private IdentityStatus identityStatus; // JOINER, ACTIVE, PRE_LEAVER, LEAVER

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String externalId;
    private String provider;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<App> assignedApps = new HashSet<>();

}

