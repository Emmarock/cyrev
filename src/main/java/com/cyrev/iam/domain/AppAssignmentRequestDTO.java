package com.cyrev.iam.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Data
@NoArgsConstructor
public class AppAssignmentRequestDTO {

    @NotNull
    private Map<App,Role> appsRole;

    private AssignmentStatus status = AssignmentStatus.PENDING;

}
