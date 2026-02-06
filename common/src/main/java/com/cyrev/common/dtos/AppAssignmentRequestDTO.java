package com.cyrev.common.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class AppAssignmentRequestDTO {

    @NotNull
    private Map<App, Role> appsRole;

    private AssignmentStatus status = AssignmentStatus.PENDING;

}
