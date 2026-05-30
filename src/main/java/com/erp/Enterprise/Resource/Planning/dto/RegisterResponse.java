package com.erp.Enterprise.Resource.Planning.dto;

public record RegisterResponse(
        String email,
        String message,
        long otpExpiresInMinutes
) {
}
