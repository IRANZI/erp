package com.erp.Enterprise.Resource.Planning.service;

import com.erp.Enterprise.Resource.Planning.entity.Employee;
import com.erp.Enterprise.Resource.Planning.exception.ApiException;
import com.erp.Enterprise.Resource.Planning.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final EmployeeRepository employeeRepository;

    public String currentEmail() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return authentication.getName();
    }

    @Transactional(readOnly = true)
    public Employee currentEmployee() {
        return employeeRepository.findByEmailIgnoreCase(currentEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated employee was not found."));
    }
}
