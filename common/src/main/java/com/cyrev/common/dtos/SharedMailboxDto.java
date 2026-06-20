package com.cyrev.common.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SharedMailboxDto {
    private String id;
    private String displayName;
    private String primarySmtpAddress;
    private String userPrincipalName;
}
