package com.pe.pivot.credit.domain.strategy.operation;

import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditOperationType;
import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Strategy responsible exclusively for processing credit card CONSUMPTION operations.
 */
public class ConsumptionOperationStrategy implements CreditOperationStrategy {

  /**
   * Determines if this strategy supports the given operation type.
   *
   * @param operationType - The type of credit operation to check.
   * @return - true if the operation type is CONSUMPTION, false otherwise.
   */
  @Override
  public boolean supports(CreditOperationType operationType) {
    return operationType == CreditOperationType.CONSUMPTION;
  }

  /**
   * Applies the CONSUMPTION operation to the given credit, updating its used and available amounts.
   *
   * @param credit - The credit object to which the operation will be applied.
   * @param amount - The amount to be consumed from the credit.
   * @return - A Single emitting the updated credit object, or an error if the operation cannot be applied.
   */
  @Override
  public Single<Credit> applyOperation(Credit credit, BigDecimal amount) {
    // Validate that the credit type is a credit card
    if (!credit.getCreditType().isCreditCard()) {
      return Single.error(new BusinessRuleException("CONSUMPTION operations are only allowed on Credit Card products."));
    }

    // Validate that the credit is active
    BigDecimal currentAvailable = Optional.ofNullable(credit.getAvailableAmount()).orElse(BigDecimal.ZERO);
    if (currentAvailable.compareTo(amount) < 0) {
      return Single.error(new BusinessRuleException("Insufficient available credit limit for consumption. Available: " + currentAvailable));
    }

    // Update the credit's used and available amounts
    BigDecimal currentUsed = Optional.ofNullable(credit.getUsedAmount()).orElse(BigDecimal.ZERO);
    credit.setUsedAmount(currentUsed.add(amount));
    credit.setAvailableAmount(currentAvailable.subtract(amount));
    credit.setUpdatedAt(Instant.now());

    // Return the updated credit object wrapped in a Single
    return Single.just(credit);
  }
}