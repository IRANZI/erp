package com.erp.Enterprise.Resource.Planning.dto;

public record AuthResponse(
        String tokenType,
        String accessToken,
        long expiresInMinutes,
        UserSummaryResponse user
) {
}
