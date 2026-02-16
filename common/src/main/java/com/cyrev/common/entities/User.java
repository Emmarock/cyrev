package com.cyrev.common.entities;
import com.cyrev.common.dtos.Role;
import com.cyrev.common.dtos.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;


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

    @Column
    private String secret;

    private String firstName;
    private String lastName;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false)
    private boolean mfaEnabled = false;

}

