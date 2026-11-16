package com.pe.pivot.credit.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Rich Enumeration of Active Bank Credit Products encapsulating product metadata and behavior.
 */
@Getter
@RequiredArgsConstructor
public enum CreditType {
  PERSONAL_LOAN(Prefix.LOAN, true, true),
  BUSINESS_LOAN(Prefix.LOAN, true, false),
  PERSONAL_CREDIT_CARD(Prefix.CARD, false, true),
  BUSINESS_CREDIT_CARD(Prefix.CARD, false, false);

  private final String numberPrefix;
  private final boolean loan;
  private final boolean personalProduct;

  // Convenience methods for product type checks, allowing for clear and concise logic when determining the nature of a credit product.
  public boolean isCreditCard() { return !this.loan; }

  // Convenience methods for product type checks, allowing for clear and concise logic when determining the nature of a credit product.
  public boolean isBusinessProduct() { return !this.personalProduct; }

  /**
   * Prefix constants for credit product number generation, distinguishing between loan and credit card products.
   * For Loan products, the prefix is "LN-", while for Credit Card products, the prefix is "4557-8800-".
   * For example, a personal loan number might be "LN-1234567890", while a business credit card number might be "4557-8800-1234-5678".
   * These prefixes are used to ensure that generated credit product numbers are unique and easily identifiable by product type.
   * The prefixes are defined as static final constants within the Prefix inner class to encapsulate them and prevent external modification,
   * adhering to the principles of immutability and encapsulation.
   * This design choice allows for easy updates to the prefixes in the future, if necessary, without affecting the rest of the codebase,
   * and provides a clear and centralized location for managing these constants.
   */
  private static final class Prefix {
    private static final String LOAN = "LN-";
    private static final String CARD = "4557-8800-";
  }
}