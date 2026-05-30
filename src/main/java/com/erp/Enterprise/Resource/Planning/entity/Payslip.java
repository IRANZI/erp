package com.erp.Enterprise.Resource.Planning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payslips",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payslip_employee_month_year",
                columnNames = {"employee_id", "salary_month", "salary_year"}
        ),
        indexes = @Index(name = "idx_payslip_month_year", columnList = "salary_month,salary_year")
)
@Getter
@Setter
@NoArgsConstructor
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "salary_month", nullable = false)
    private Integer month;

    @Column(name = "salary_year", nullable = false)
    private Integer year;

    @Column(name = "base_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "house_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal houseAmount;

    @Column(name = "transport_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal transportAmount;

    @Column(name = "gross_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal grossSalary;

    @Column(name = "employee_tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal employeeTaxAmount;

    @Column(name = "pension_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal pensionAmount;

    @Column(name = "medical_insurance_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal medicalInsuranceAmount;

    @Column(name = "other_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal otherAmount;

    @Column(name = "net_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayslipStatus status = PayslipStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
