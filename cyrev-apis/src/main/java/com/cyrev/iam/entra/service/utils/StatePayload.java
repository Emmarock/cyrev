package com.cyrev.iam.entra.service.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class StatePayload {
    private LocalDateTime expiryTime;
    private String state;
}