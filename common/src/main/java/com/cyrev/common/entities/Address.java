package com.cyrev.common.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends BaseEntity {
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;
    @Column(name="building_number", nullable = false)
    private String buildingNumber;
    @Column(name="street", nullable = false)
    private String street;
    @Column(name="city", nullable = false)
    private String city;
    @Column(name="state", nullable = false)
    private String state;
    private String postalCode;
    @Column(name="country_code", nullable = false, length = 3)
    private String countryCode;
    @Column(name="country_name", nullable = false)
    private String countryName;
}
