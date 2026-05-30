package com.erp.Enterprise.Resource.Planning.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        Integer month,
        Integer year,
        String content,
        LocalDateTime createdAt
) {
}
