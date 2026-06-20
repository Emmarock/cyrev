package com.cyrev.common.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessPackageDto {
    private String id;
    private String displayName;
    private String description;
}
