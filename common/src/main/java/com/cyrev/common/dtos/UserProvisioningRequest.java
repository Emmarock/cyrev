package com.cyrev.common.dtos;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class UserProvisioningRequest {
    private UUID userId;
}
