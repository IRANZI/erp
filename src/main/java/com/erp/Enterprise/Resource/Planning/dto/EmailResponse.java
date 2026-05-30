package com.erp.Enterprise.Resource.Planning.dto;

public record EmailResponse(
        String recipient,
        String subject,
        String message
) {
}
