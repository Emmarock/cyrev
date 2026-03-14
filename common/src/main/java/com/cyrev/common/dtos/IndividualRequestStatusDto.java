package com.cyrev.common.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndividualRequestStatusDto {
    private String principalId;
    private GovernanceStatus status;
    private String errorMessage;
}