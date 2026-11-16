package com.pe.pivot.credit.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
  /**
   * Determines whether this credit product can be logically closed, which requires that the used
   * amount is zero or null (no outstanding debt).
   *
   * @return true if the credit product has no outstanding debt
   */
  public boolean canBeClosed() {
    return this.usedAmount == null || this.usedAmount.compareTo(BigDecimal.ZERO) == 0;
  }

  /**
   * Determines whether this credit product is a personal loan that is still active.
   *
   * @return true if the credit is a personal loan that is not closed or paid off
   */
  public boolean isActivePersonalLoan() {
    return this.creditType == CreditType.PERSONAL_LOAN
            && this.status != CreditStatus.CLOSED
            && this.status != CreditStatus.PAID_OFF;
  }
}
