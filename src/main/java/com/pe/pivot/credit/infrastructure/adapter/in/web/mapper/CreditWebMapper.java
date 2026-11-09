package com.pe.pivot.credit.infrastructure.adapter.in.web.mapper;

import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditBalanceResponse;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditRequest;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditResponse;
import com.pe.pivot.credit.infrastructure.util.DataMaskingUtil;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Web Mapper converting between immutable Record DTOs and Domain Credit.
 * Reveals unmasked creditNumber only upon creation (POST) and masks it on all queries/updates.
 */
@Component
public class CreditWebMapper {

  public Credit toDomain(CreditRequest request) {
    return Credit.builder()
            .creditNumber(request.creditNumber())
            .customerId(request.customerId())
            .creditType(request.creditType())
            .currency(request.currency())
            .creditLimit(request.creditLimit())
            .interestRate(request.interestRate())
            .paymentDueDay(request.paymentDueDay())
            .status(request.status())
            .build();
  }

  /**
   * Used ONLY during credit creation (POST) so the client receives the generated number once.
   */
  public CreditResponse toUnmaskedResponse(Credit domain) {
    return buildCreditResponse(domain, domain.getCreditNumber());
  }

  /**
   * Used for all GET, PUT, and PATCH operations, returning the masked number (****5678).
   */
  public CreditResponse toResponse(Credit domain) {
    return buildCreditResponse(domain, DataMaskingUtil.maskCreditNumber(domain.getCreditNumber()));
  }

  public CreditBalanceResponse toBalanceResponse(Credit domain) {
    return CreditBalanceResponse.builder()
            .creditId(domain.getId())
            .creditNumber(DataMaskingUtil.maskCreditNumber(domain.getCreditNumber()))
            .creditType(domain.getCreditType())
            .currency(domain.getCurrency())
            .creditLimit(domain.getCreditLimit())
            .usedAmount(domain.getUsedAmount())
            .availableAmount(domain.getAvailableAmount())
            .status(domain.getStatus())
            .queriedAt(Instant.now())
            .build();
  }

  private CreditResponse buildCreditResponse(Credit domain, String formattedCreditNumber) {
    return CreditResponse.builder()
            .id(domain.getId())
            .creditNumber(formattedCreditNumber)
            .customerId(domain.getCustomerId())
            .creditType(domain.getCreditType())
            .currency(domain.getCurrency())
            .creditLimit(domain.getCreditLimit())
            .usedAmount(domain.getUsedAmount())
            .availableAmount(domain.getAvailableAmount())
            .interestRate(domain.getInterestRate())
            .paymentDueDay(domain.getPaymentDueDay())
            .status(domain.getStatus())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
  }
}