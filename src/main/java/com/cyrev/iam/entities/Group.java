package com.cyrev.iam.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

@Entity
@Where(clause = "deleted = false")
@Table(name = "groups")
@Getter
@Setter
public class Group extends BaseEntity{


    @Column(unique = true)
    private String externalId; // Entra objectId

    private String displayName;
    private String provider;

    @ManyToMany
    @JoinTable(
      name = "group_members",
      joinColumns = @JoinColumn(name = "group_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();
}
