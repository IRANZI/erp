package com.erp.Enterprise.Resource.Planning.repository;

import com.erp.Enterprise.Resource.Planning.entity.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

    @EntityGraph(attributePaths = {"employment", "userAccount", "userAccount.roles"})
    Optional<Employee> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"employment", "userAccount", "userAccount.roles"})
    Optional<Employee> findWithEmploymentById(Long id);

    @Override
    @EntityGraph(attributePaths = {"employment", "userAccount", "userAccount.roles"})
    List<Employee> findAll();
}
