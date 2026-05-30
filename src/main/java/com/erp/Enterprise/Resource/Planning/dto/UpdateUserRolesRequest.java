package com.erp.Enterprise.Resource.Planning.dto;

import com.erp.Enterprise.Resource.Planning.entity.Role;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateUserRolesRequest(@NotEmpty Set<Role> roles) {
}
