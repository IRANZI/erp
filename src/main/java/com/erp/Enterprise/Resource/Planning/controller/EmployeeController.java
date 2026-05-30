package com.erp.Enterprise.Resource.Planning.controller;

import com.erp.Enterprise.Resource.Planning.dto.CreateEmployeeRequest;
import com.erp.Enterprise.Resource.Planning.dto.EmployeeResponse;
import com.erp.Enterprise.Resource.Planning.dto.UpdateEmploymentStatusRequest;
import com.erp.Enterprise.Resource.Planning.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    List<EmployeeResponse> listEmployees() {
        return employeeService.listEmployees();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    EmployeeResponse getEmployee(@PathVariable Long id) {
        return employeeService.getEmployee(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MANAGER')")
    EmployeeResponse createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        return employeeService.createEmployee(request);
    }

    @PatchMapping("/{id}/employment-status")
    @PreAuthorize("hasRole('MANAGER')")
    EmployeeResponse updateEmploymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmploymentStatusRequest request
    ) {
        return employeeService.updateEmploymentStatus(id, request.status());
    }
}
