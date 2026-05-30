package com.erp.Enterprise.Resource.Planning.dto;

import com.erp.Enterprise.Resource.Planning.entity.PayslipStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayslipResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        String institution,
        BigDecimal baseSalary,
        BigDecimal house,
        BigDecimal transport,
        BigDecimal grossSalary,
        BigDecimal employeeTax,
        BigDecimal pension,
        BigDecimal medicalInsurance,
        BigDecimal others,
        BigDecimal netSalary,
        PayslipStatus status,
        Integer month,
        Integer year,
        LocalDateTime paidAt
) {
}
