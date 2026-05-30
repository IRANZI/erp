package com.erp.Enterprise.Resource.Planning.dto;

import com.erp.Enterprise.Resource.Planning.entity.EmploymentStatus;
import com.erp.Enterprise.Resource.Planning.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record CreateEmployeeRequest(
        @NotBlank String employeeCode,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        String district,
        String mobile,
        @Past LocalDate dateOfBirth,
        @NotBlank String institution,
        @NotBlank String department,
        @NotBlank String position,
        @NotNull @Positive BigDecimal baseSalary,
        EmploymentStatus status,
        @NotNull @PastOrPresent LocalDate joiningDate,
        @NotBlank @Size(min = 6) String password,
        Set<Role> roles
) {
}
