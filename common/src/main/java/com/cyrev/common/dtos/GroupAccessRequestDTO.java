package com.cyrev.common.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GroupAccessRequestDTO {

    @NotBlank(message = "groupId is required")
    private String groupId;

    private String justification;
}