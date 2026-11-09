package com.pe.pivot.credit.domain.strategy.validation;

import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditType;
import com.pe.pivot.credit.domain.model.CustomerSummary;
import io.reactivex.rxjava3.core.Completable;

import java.util.List;

/**
 * Validation strategy for BUSINESS customers requesting corporate loans or credit cards.
 */
public class BusinessCreditStrategy implements CreditValidationStrategy {

  /**
    * Checks if the strategy supports the given customer type.
    *
    * @param customerType The type of the customer.
    * @return true if the strategy supports the customer type, false otherwise.
    */
  @Override
  public boolean supports(String customerType) {
    return "BUSINESS".equalsIgnoreCase(customerType);
  }

  /**
   * Validates the credit request for a BUSINESS customer based on business rules.
   *
   * @param customer - The summary of the customer requesting credit.
   * @param newCredit - The new credit product being requested.
   * @param existingCredits - The list of existing credit products the customer has.
   * @return - A Completable that completes if the validation passes, or emits an error if it fails.
   */
  @Override
  public Completable validate(CustomerSummary customer, Credit newCredit, List<Credit> existingCredits) {
    return Completable.fromAction(() -> {
      // Business customers cannot request PERSONAL_LOAN or PERSONAL_CREDIT_CARD products.
      if (newCredit.getCreditType().isPersonalProduct()) {
        throw new BusinessRuleException("Business customers cannot request PERSONAL_LOAN or PERSONAL_CREDIT_CARD products.");
      }
    });
  }
}