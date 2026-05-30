package com.erp.Enterprise.Resource.Planning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "deductions",
        uniqueConstraints = @UniqueConstraint(name = "uk_deduction_name", columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
public class Deduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "rate_percent", nullable = false, precision = 7, scale = 2)
    private BigDecimal ratePercent;

    @Column(nullable = false)
    private boolean active = true;
}
