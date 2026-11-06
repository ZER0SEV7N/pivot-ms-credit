package com.pe.pivot.credit.domain.model;

/**
 * Enumeration of Active Bank Credit Products with domain behavior methods.
 */
public enum CreditType {
  PERSONAL_LOAN,
  BUSINESS_LOAN,
  PERSONAL_CREDIT_CARD,
  BUSINESS_CREDIT_CARD;

  public boolean isLoan() {
    return this == PERSONAL_LOAN || this == BUSINESS_LOAN;
  }

  public boolean isCreditCard() {
    return this == PERSONAL_CREDIT_CARD || this == BUSINESS_CREDIT_CARD;
  }
}