package com.erp.Enterprise.Resource.Planning.repository;

import com.erp.Enterprise.Resource.Planning.entity.Deduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeductionRepository extends JpaRepository<Deduction, Long> {
    Optional<Deduction> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
