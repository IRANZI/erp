package com.erp.Enterprise.Resource.Planning.controller;

import com.erp.Enterprise.Resource.Planning.dto.PayrollRequest;
import com.erp.Enterprise.Resource.Planning.dto.PayslipResponse;
import com.erp.Enterprise.Resource.Planning.service.PayrollService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {
    private final PayrollService payrollService;

    @PostMapping("/process")
    @PreAuthorize("hasRole('MANAGER')")
    List<PayslipResponse> processPayroll(@Valid @RequestBody PayrollRequest request) {
        return payrollService.processPayroll(request);
    }

    @PostMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
    List<PayslipResponse> approvePayroll(@Valid @RequestBody PayrollRequest request) {
        return payrollService.approvePayroll(request);
    }

    @GetMapping("/{year}/{month}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    List<PayslipResponse> listPayroll(
            @PathVariable @Min(2000) @Max(2100) Integer year,
            @PathVariable @Min(1) @Max(12) Integer month
    ) {
        return payrollService.listPayroll(month, year);
    }
}
