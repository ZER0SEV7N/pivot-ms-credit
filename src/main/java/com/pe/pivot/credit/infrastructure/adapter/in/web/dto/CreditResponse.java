package com.pe.pivot.credit.infrastructure.adapter.in.web.dto;

import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.model.CreditType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable Response DTO representing a credit product.
 * Note: creditNumberHash is strictly internal to MongoDB and never exposed via API.
 */
@Builder
public record CreditResponse(
        String id,
        String creditNumber,
        String customerId,
        CreditType creditType,
        String currency,
        BigDecimal creditLimit,
        BigDecimal usedAmount,
        BigDecimal availableAmount,
        BigDecimal interestRate,
        Integer paymentDueDay,
        CreditStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}