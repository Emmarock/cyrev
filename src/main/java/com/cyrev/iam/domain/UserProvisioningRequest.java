package com.cyrev.iam.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class UserProvisioningRequest {
    private UUID userId;
}
