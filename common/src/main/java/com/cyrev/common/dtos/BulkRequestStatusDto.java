package com.cyrev.common.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkRequestStatusDto {
    private String tenantId;
    private GovernanceOperationType operationType;
    private int total;
    private int completed;
    private int failed;
    private int pending;
    private List<IndividualRequestStatusDto> requests;
}