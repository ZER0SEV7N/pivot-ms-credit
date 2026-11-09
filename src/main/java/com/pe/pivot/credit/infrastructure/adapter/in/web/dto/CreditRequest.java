package com.pe.pivot.credit.infrastructure.adapter.in.web.dto;

import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.model.CreditType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Immutable Request DTO using Java 17 Record for creating or updating a credit product.
 */
@Builder
public record CreditRequest(
        String creditNumber,

        @NotBlank(message = "customerId is required")
        String customerId,

        @NotNull(message = "creditType is required")
        CreditType creditType,

        @NotBlank(message = "currency is required")
        String currency,

        @NotNull(message = "creditLimit is required")
        @Positive(message = "creditLimit must be greater than zero")
        BigDecimal creditLimit,

        @PositiveOrZero(message = "interestRate must be zero or positive")
        BigDecimal interestRate,

        @Min(value = 1, message = "paymentDueDay must be between 1 and 31")
        @Max(value = 31, message = "paymentDueDay must be between 1 and 31")
        Integer paymentDueDay,

        CreditStatus status
) {
}