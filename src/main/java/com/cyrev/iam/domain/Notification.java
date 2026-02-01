package com.cyrev.iam.domain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class Notification {
    private NotificationType type;
    private UUID userId;
    private UUID approverId;
    private String message;
    private String actor;

}
