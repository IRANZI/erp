package com.erp.Enterprise.Resource.Planning.dto;

import com.erp.Enterprise.Resource.Planning.entity.EmploymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateEmploymentStatusRequest(@NotNull EmploymentStatus status) {
}
