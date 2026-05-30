package com.erp.Enterprise.Resource.Planning.repository;

import com.erp.Enterprise.Resource.Planning.entity.Message;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @EntityGraph(attributePaths = {"employee", "payslip"})
    List<Message> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
}
