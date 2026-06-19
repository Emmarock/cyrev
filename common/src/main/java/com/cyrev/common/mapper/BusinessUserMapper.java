package com.cyrev.common.mapper;

import com.cyrev.common.dtos.BusinessUserDto;
import com.cyrev.common.entities.BusinessUser;


public final class BusinessUserMapper {

    private BusinessUserMapper() {
    }

    public BusinessUserDto toDto(BusinessUser entity) {

        return BusinessUserDto.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .version(entity.getVersion())
                .deleted(entity.isDeleted())

                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .employeeId(entity.getEmployeeId())

                .businessId(
                        entity.getBusiness() != null
                                ? entity.getBusiness().getId()
                                : null
                )

                .managerId(
                        entity.getManager() != null
                                ? entity.getManager().getId()
                                : null
                )
                .managerName(
                        entity.getManager() != null
                                ? entity.getManager().getFirstName() + " "
                                + entity.getManager().getLastName()
                                : null
                )

                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())

                .unit(entity.getUnit())
                .department(entity.getDepartment())
                .division(entity.getDivision())

                .identityStatus(entity.getIdentityStatus())
                .approvalStatus(entity.getApprovalStatus())

                .approvalDecidedBy(entity.getApprovalDecidedBy())
                .approvalDecidedAt(entity.getApprovalDecidedAt())
                .approvalReason(entity.getApprovalReason())

                .entraObjectId(entity.getEntraObjectId())
                .build();
    }
}