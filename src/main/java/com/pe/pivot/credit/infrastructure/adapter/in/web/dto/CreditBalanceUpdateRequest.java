package com.pe.pivot.credit.infrastructure.adapter.in.web.dto;

import com.pe.pivot.credit.domain.model.CreditOperationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Immutable Request DTO for processing credit payments or credit card consumptions.
 */
@Builder
public record CreditBalanceUpdateRequest(
        @NotNull(message = "operationType is required (PAYMENT or CONSUMPTION)")
        CreditOperationType operationType,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be greater than zero")
        BigDecimal amount
) {
}