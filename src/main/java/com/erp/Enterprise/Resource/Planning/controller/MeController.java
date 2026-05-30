package com.erp.Enterprise.Resource.Planning.controller;

import com.erp.Enterprise.Resource.Planning.dto.EmailResponse;
import com.erp.Enterprise.Resource.Planning.dto.EmployeeResponse;
import com.erp.Enterprise.Resource.Planning.dto.MessageResponse;
import com.erp.Enterprise.Resource.Planning.dto.PayslipResponse;
import com.erp.Enterprise.Resource.Planning.service.CurrentUserService;
import com.erp.Enterprise.Resource.Planning.service.EmployeeService;
import com.erp.Enterprise.Resource.Planning.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
public class MeController {
    private final CurrentUserService currentUserService;
    private final EmployeeService employeeService;
    private final PayrollService payrollService;

    @GetMapping
    EmployeeResponse myDetails() {
        return employeeService.toResponse(currentUserService.currentEmployee());
    }

    @GetMapping("/payslips")
    List<PayslipResponse> myPayslips() {
        return payrollService.listCurrentEmployeePayslips();
    }

    @GetMapping("/payslips/pending")
    List<PayslipResponse> myPendingPayslips() {
        return payrollService.listCurrentEmployeePendingPayslips();
    }

    @GetMapping("/payslips/{id}")
    PayslipResponse myPayslip(@PathVariable Long id) {
        return payrollService.getCurrentEmployeePayslip(id);
    }

    @GetMapping("/payslips/{id}/download")
    ResponseEntity<String> downloadPayslip(@PathVariable Long id) {
        String payslip = payrollService.downloadCurrentEmployeePayslip(id);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("payslip-" + id + ".txt")
                        .build()
                        .toString())
                .body(payslip);
    }

    @PostMapping("/payslips/{id}/email")
    EmailResponse emailPayslip(@PathVariable Long id) {
        return payrollService.emailCurrentEmployeePayslip(id);
    }

    @GetMapping("/messages")
    List<MessageResponse> myMessages() {
        return payrollService.listCurrentEmployeeMessages();
    }
}
