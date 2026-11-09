package com.pe.pivot.credit.domain.strategy.validation;

import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditType;
import com.pe.pivot.credit.domain.model.CustomerSummary;
import io.reactivex.rxjava3.core.Completable;

import java.util.List;

/**
 * Validation strategy for PERSONAL customers requesting loans or credit cards.
 */
public class PersonalCreditStrategy implements CreditValidationStrategy {

  /**
   * Validates the credit request for a PERSONAL customer.
   *
   * @param customerType - The type of customer (should be "PERSONAL").
   * @return - true if the strategy supports the given customer type, false otherwise.
   */
  @Override
  public boolean supports(String customerType) {
    return "PERSONAL".equalsIgnoreCase(customerType);
  }

  /**
   * Validates the credit request for a PERSONAL customer based on business rules.
   *
   * @param customer - The summary of the customer requesting credit.
   * @param newCredit - The new credit product being requested.
   * @param existingCredits - The list of existing credit products the customer has.
   * @return - A Completable that completes if the validation passes, or emits an error if it fails.
   */
  @Override
  public Completable validate(CustomerSummary customer, Credit newCredit, List<Credit> existingCredits) {

    return Completable.fromAction(() -> {
      // Business Rule: Personal customers cannot request BUSINESS_LOAN or BUSINESS_CREDIT_CARD products.
      if (newCredit.getCreditType().isBusinessProduct()) {
        throw new BusinessRuleException("Personal customers cannot request BUSINESS_LOAN or BUSINESS_CREDIT_CARD products.");
      }

      // Business Rule: Personal customers can only have one active PERSONAL_LOAN at a time.
      if (newCredit.getCreditType() == CreditType.PERSONAL_LOAN && existingCredits.stream().anyMatch(Credit::isActivePersonalLoan)) {
        throw new BusinessRuleException("Personal customers are only allowed to hold one active PERSONAL_LOAN at a time.");
      }
    });
  }
}

