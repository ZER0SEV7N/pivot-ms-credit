package com.pe.pivot.credit.application.support;

import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.model.CreditType;
import com.pe.pivot.credit.infrastructure.config.CreditRulesProperties;
import com.pe.pivot.credit.infrastructure.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain Factory / Enricher responsible exclusively for initializing default credit parameters,
 * cryptographic blind-index hashes, and product-type financial rules (Loans vs. Credit Cards).
 */
@Component
@RequiredArgsConstructor
public class CreditDomainInitializer {

  private final CreditRulesProperties rules;
  private final CryptoUtil cryptoUtil;

  /**
   * Initializes default values for a Credit domain object, including generating a unique credit number if not provided,
   * setting the credit number hash, default currency, status, interest rate, and product-specific balances based on the credit type.
   *
   * @param credit - the Credit domain object to be initialized with default values and cryptographic hashes
   */
  public void initializeDefaults(Credit credit) {
    CreditType type = credit.getCreditType();

    // Generate a unique credit number if not provided, and compute its SHA-256 hash for secure storage.
    String rawCreditNumber = Optional.ofNullable(credit.getCreditNumber())
            .filter(num -> !num.isBlank())
            .orElseGet(() -> generateCreditNumber(type));

    // Set the credit number and its cryptographic hash, ensuring that the credit object has a unique identifier and secure representation.
    credit.setCreditNumber(rawCreditNumber);
    credit.setCreditNumberHash(cryptoUtil.hashSha256(rawCreditNumber));
    credit.setCurrency(Optional.ofNullable(credit.getCurrency()).orElse("PEN"));
    credit.setStatus(Optional.ofNullable(credit.getStatus()).orElse(CreditStatus.ACTIVE));
    credit.setInterestRate(Optional.ofNullable(credit.getInterestRate()).orElseGet(() -> rules.resolveDefaultRate(type)));

    // Apply product-specific balances based on whether the credit type is a loan or a credit card,
    // ensuring that the used and available amounts are correctly initialized according to business rules.
    applyProductSpecificBalances(credit, type);

    // Set the creation and update timestamps to the current instant,
    // ensuring that the credit object has accurate temporal metadata for auditing and tracking purposes.
    Instant now = Instant.now();
    credit.setCreatedAt(now);
    credit.setUpdatedAt(now);
  }

  /**
   * Applies product-specific balances based on the credit type.
   *
   * @param credit - the Credit domain object to which the balances will be applied
   * @param type - the type of the credit (Loan or Credit Card)
   */
  private void applyProductSpecificBalances(Credit credit, CreditType type) {
    if (type.isLoan()) {
      credit.setUsedAmount(credit.getCreditLimit());
      credit.setAvailableAmount(BigDecimal.ZERO);
      return;
    }

    BigDecimal initialUsed = Optional.ofNullable(credit.getUsedAmount()).orElse(BigDecimal.ZERO);
    credit.setUsedAmount(initialUsed);
    credit.setAvailableAmount(credit.getCreditLimit().subtract(initialUsed));
    credit.setPaymentDueDay(Optional.ofNullable(credit.getPaymentDueDay())
            .orElse(rules.getDefaultPaymentDueDay()));
  }

  private String generateCreditNumber(CreditType type) {
    String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    return type.getNumberPrefix() + randomSuffix;
  }
}