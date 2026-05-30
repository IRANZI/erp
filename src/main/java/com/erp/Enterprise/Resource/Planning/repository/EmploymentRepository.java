package com.erp.Enterprise.Resource.Planning.repository;

import com.erp.Enterprise.Resource.Planning.entity.Employment;
import com.erp.Enterprise.Resource.Planning.entity.EmploymentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmploymentRepository extends JpaRepository<Employment, Long> {
    @EntityGraph(attributePaths = "employee")
    List<Employment> findByStatus(EmploymentStatus status);
}
