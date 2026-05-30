package com.erp.Enterprise.Resource.Planning.dto;

import java.math.BigDecimal;

public record DeductionResponse(
        Long id,
        String name,
        BigDecimal ratePercent,
        boolean active
) {
}
