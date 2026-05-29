package com.cyrev.common.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SharedMailboxAccessRequestDTO {

    @NotBlank(message = "sharedMailboxId is required (UPN or object id)")
    private String sharedMailboxId;

    /**
     * Defaults to {@code FullAccess} when omitted.
     */
    @Pattern(regexp = "(?i)FullAccess|SendAs",
            message = "accessRight must be FullAccess or SendAs")
    private String accessRight;

    private String justification;
}