package com.cyrev.common.dtos;

import lombok.Data;

@Data
public class SharedMailboxRequest {
    private String sharedMailbox;
    private String user;
    private Boolean fullAccess;
    private Boolean sendAs;
    private Boolean sendOnBehalf;
}
