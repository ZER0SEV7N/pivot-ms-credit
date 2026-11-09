package com.pe.pivot.credit.infrastructure.adapter.in.web.dto;

import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.model.CreditType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable Response DTO for consulting credit balance, available line, and outstanding debt.
 */
@Builder
public record CreditBalanceResponse(
        String creditId,
        String creditNumber,
        CreditType creditType,
        String currency,
        BigDecimal creditLimit,
        BigDecimal usedAmount,
        BigDecimal availableAmount,
        CreditStatus status,
        Instant queriedAt
) {
}