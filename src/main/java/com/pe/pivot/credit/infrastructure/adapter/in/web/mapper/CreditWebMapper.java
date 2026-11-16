package com.pe.pivot.credit.infrastructure.adapter.in.web.mapper;

import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditBalanceResponse;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditRequest;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditResponse;
import com.pe.pivot.credit.infrastructure.util.DataMaskingUtil;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Web Mapper converting between immutable Record DTOs and Domain Credit.
 * Reveals unmasked creditNumber only upon creation (POST) and masks it on all queries/updates.
 */
@Component
public class CreditWebMapper {

  /**
   * Maps a CreditRequest DTO to the domain Credit model.
   *
   * @param request - the incoming credit request
   * @return the domain Credit built from the request
   */
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

  /**
   * Builds a balance response for the given credit, masking its credit number.
   *
   * @param domain - the credit whose balance is being consulted
   * @return the balance response DTO
   */
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
