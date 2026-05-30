package com.erp.Enterprise.Resource.Planning.dto;

import com.erp.Enterprise.Resource.Planning.entity.Role;
import com.erp.Enterprise.Resource.Planning.entity.UserStatus;

import java.util.Set;

public record UserSummaryResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String fullName,
        String email,
        UserStatus status,
        Set<Role> roles
) {
}
