package com.erp.Enterprise.Resource.Planning.dto;

import com.erp.Enterprise.Resource.Planning.entity.EmploymentStatus;
import com.erp.Enterprise.Resource.Planning.entity.Role;
import com.erp.Enterprise.Resource.Planning.entity.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String district,
        String mobile,
        LocalDate dateOfBirth,
        String institution,
        String department,
        String position,
        BigDecimal baseSalary,
        EmploymentStatus employmentStatus,
        LocalDate joiningDate,
        Long userId,
        UserStatus userStatus,
        Set<Role> roles
) {
}
