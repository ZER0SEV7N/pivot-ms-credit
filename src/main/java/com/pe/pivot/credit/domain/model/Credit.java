package com.pe.pivot.credit.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Core Domain Model representing an Active Bank Credit Product (Loan or Credit Card).
 * Free of framework and persistence annotations.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Credit {

  private String id;
  private String creditNumber;
  private String creditNumberHash;
  private String customerId;
  private CreditType creditType;
  private String currency;
  private BigDecimal creditLimit;
  private BigDecimal usedAmount;
  private BigDecimal availableAmount;
  private BigDecimal interestRate;
  private Integer paymentDueDay;
  private CreditStatus status;
  private Instant createdAt;
  private Instant updatedAt;

  // Business Logic Methods
  // Only can be closed when the used amount is zero or null (no debt)
  public boolean canBeClosed() {
    return this.usedAmount == null || this.usedAmount.compareTo(BigDecimal.ZERO) == 0;
  }

  // A Personal Loan is considered active if it is not closed or paid off
  public boolean isActivePersonalLoan() {
    return this.creditType == CreditType.PERSONAL_LOAN
            && this.status != CreditStatus.CLOSED
            && this.status != CreditStatus.PAID_OFF;
  }
}