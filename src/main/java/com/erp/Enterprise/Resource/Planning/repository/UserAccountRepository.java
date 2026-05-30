package com.erp.Enterprise.Resource.Planning.repository;

import com.erp.Enterprise.Resource.Planning.entity.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    @EntityGraph(attributePaths = {"employee", "roles"})
    Optional<UserAccount> findByEmployeeEmailIgnoreCase(String email);

    boolean existsByEmployeeEmailIgnoreCase(String email);

    @Override
    @EntityGraph(attributePaths = {"employee", "roles"})
    List<UserAccount> findAll();
}
