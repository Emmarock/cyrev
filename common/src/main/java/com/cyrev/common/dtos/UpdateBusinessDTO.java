package com.cyrev.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBusinessDTO {

    private String companyName;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private UUID relationshipOwnerId;
    private ContractStatus contractStatus;
}