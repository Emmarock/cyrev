package com.cyrev.common.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AutomationJobStatus {
    private String jobId;
    private String status;
    private String statusDetails;
    private String startTime;
    private String endTime;
    private String exception;
}
