package com.pe.pivot.credit.infrastructure.adapter.out.persistence.mapper;

import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.model.CreditType;
import com.pe.pivot.credit.infrastructure.adapter.out.persistence.entity.CreditDocument;
import com.pe.pivot.credit.infrastructure.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Persistence Mapper converting between Domain Credit and MongoDB CreditDocument.
 * Transparently encrypts/decrypts creditNumber using AES-256-GCM.
 */
@Component
@RequiredArgsConstructor
public class CreditPersistenceMapper {

  private final CryptoUtil cryptoUtil;

  /**
   * Maps a domain Credit object to a persistence CreditDocument, encrypting the credit number.
   *
   * @param domain - The domain Credit object to map.
   * @return - The corresponding CreditDocument for persistence.
   */
  public CreditDocument toDocument(Credit domain) {
    return CreditDocument.builder()
            .id(domain.getId())
            .creditNumber(cryptoUtil.encryptAes256(domain.getCreditNumber()))
            .creditNumberHash(domain.getCreditNumberHash())
            .customerId(domain.getCustomerId())
            .creditType(domain.getCreditType() != null ? domain.getCreditType().name() : null)
            .currency(domain.getCurrency())
            .creditLimit(domain.getCreditLimit())
            .usedAmount(domain.getUsedAmount())
            .availableAmount(domain.getAvailableAmount())
            .interestRate(domain.getInterestRate())
            .paymentDueDay(domain.getPaymentDueDay())
            .status(domain.getStatus() != null ? domain.getStatus().name() : null)
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
  }

  /**
   * Maps a persistence CreditDocument to a domain Credit object, decrypting the credit number.
   *
   * @param doc - The persistence CreditDocument to map.
   * @return - The corresponding domain Credit object.
   */
  public Credit toDomain(CreditDocument doc) {
    return Credit.builder()
            .id(doc.getId())
            .creditNumber(cryptoUtil.decryptAes256(doc.getCreditNumber()))
            .creditNumberHash(doc.getCreditNumberHash())
            .customerId(doc.getCustomerId())
            .creditType(doc.getCreditType() != null ? CreditType.valueOf(doc.getCreditType()) : null)
            .currency(doc.getCurrency())
            .creditLimit(doc.getCreditLimit())
            .usedAmount(doc.getUsedAmount())
            .availableAmount(doc.getAvailableAmount())
            .interestRate(doc.getInterestRate())
            .paymentDueDay(doc.getPaymentDueDay())
            .status(doc.getStatus() != null ? CreditStatus.valueOf(doc.getStatus()) : null)
            .createdAt(doc.getCreatedAt())
            .updatedAt(doc.getUpdatedAt())
            .build();
  }
}