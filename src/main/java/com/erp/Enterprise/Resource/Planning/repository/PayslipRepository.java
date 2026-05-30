package com.erp.Enterprise.Resource.Planning.repository;

import com.erp.Enterprise.Resource.Planning.entity.Payslip;
import com.erp.Enterprise.Resource.Planning.entity.PayslipStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    boolean existsByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);

    @EntityGraph(attributePaths = {"employee", "employee.employment"})
    List<Payslip> findByMonthAndYear(Integer month, Integer year);

    @EntityGraph(attributePaths = {"employee", "employee.employment"})
    List<Payslip> findByMonthAndYearAndStatus(Integer month, Integer year, PayslipStatus status);

    @EntityGraph(attributePaths = {"employee", "employee.employment"})
    List<Payslip> findByEmployeeIdOrderByYearDescMonthDesc(Long employeeId);

    @EntityGraph(attributePaths = {"employee", "employee.employment"})
    List<Payslip> findByEmployeeIdAndStatusOrderByYearDescMonthDesc(Long employeeId, PayslipStatus status);

    @EntityGraph(attributePaths = {"employee", "employee.employment"})
    Optional<Payslip> findByIdAndEmployeeId(Long id, Long employeeId);
}
