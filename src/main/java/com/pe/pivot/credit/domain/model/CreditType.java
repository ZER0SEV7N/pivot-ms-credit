package com.pe.pivot.credit.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Rich Enumeration of Active Bank Credit Products encapsulating product metadata and behavior.
 */
@Getter
@RequiredArgsConstructor
public enum CreditType {
  PERSONAL_LOAN("LN-", true, true),
  BUSINESS_LOAN("LN-", true, false),
  PERSONAL_CREDIT_CARD("4557-8800-", false, true),
  BUSINESS_CREDIT_CARD("4557-8800-", false, false);

  private final String numberPrefix;
  private final boolean loan;
  private final boolean personalProduct;

  public boolean isCreditCard() {
    return !this.loan;
  }

  public boolean isBusinessProduct() {
    return !this.personalProduct;
  }
}