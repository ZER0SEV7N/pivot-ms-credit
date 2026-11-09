package com.pe.pivot.credit.domain.strategy.validation;

import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CustomerSummary;
import io.reactivex.rxjava3.core.Completable;

import java.util.List;

/**
 * Strategy interface for validating credit product eligibility according to customer type.
 */
public interface CreditValidationStrategy {

  boolean supports(String customerType);

  Completable validate(CustomerSummary customer, Credit newCredit, List<Credit> existingCredits);
}
