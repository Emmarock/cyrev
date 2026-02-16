package com.cyrev.common.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate = LocalDate.now();

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    // Owners relationship: many-to-many (assuming a User entity exists)
    @ManyToMany
    @JoinTable(
        name = "organization_owners",
        joinColumns = @JoinColumn(name = "organization_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> owners;
}
