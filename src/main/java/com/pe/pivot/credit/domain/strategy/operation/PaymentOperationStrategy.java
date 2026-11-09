package com.pe.pivot.credit.domain.strategy.operation;

import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditOperationType;
import com.pe.pivot.credit.domain.model.CreditStatus;
import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Strategy responsible exclusively for processing credit PAYMENT operations.
 */
public class PaymentOperationStrategy implements CreditOperationStrategy {

  /**
   * Determines if this strategy supports the given credit operation type. In this case, it only supports PAYMENT operations.
   *
   * @param operationType - The type of credit operation to check for support.
   * @return - true if the operation type is PAYMENT, false otherwise.
   */
  @Override
  public boolean supports(CreditOperationType operationType) {
    return operationType == CreditOperationType.PAYMENT;
  }

  /**
   * Applies the payment operation to the given credit.
   *
   * @param credit - The credit to apply the payment to.
   * @param amount - The amount to pay.
   * @return - A Single emitting the updated credit.
   */
  @Override
  public Single<Credit> applyOperation(Credit credit, BigDecimal amount) {
    BigDecimal currentUsed = Optional.ofNullable(credit.getUsedAmount()).orElse(BigDecimal.ZERO);

    // Validate that the credit has an outstanding debt and that the payment amount does not exceed it.
    if (currentUsed.compareTo(BigDecimal.ZERO) == 0) {
      return Single.error(new BusinessRuleException("Credit product has no outstanding debt (usedAmount is 0.00)."));
    }

    // Validate that the payment amount does not exceed the current outstanding debt.
    if (amount.compareTo(currentUsed) > 0) {
      return Single.error(new BusinessRuleException("Payment amount (" + amount + ") cannot exceed current outstanding debt (" + currentUsed + ")."));
    }

    // Update the used amount after the payment and adjust the credit's state accordingly.
    BigDecimal newUsedAmount = currentUsed.subtract(amount);
    credit.setUsedAmount(newUsedAmount);
    updatePostPaymentState(credit, amount, newUsedAmount);
    credit.setUpdatedAt(Instant.now());

    return Single.just(credit);
  }

  private void updatePostPaymentState(Credit credit, BigDecimal paidAmount, BigDecimal newUsedAmount) {
    if (credit.getCreditType().isCreditCard()) {
      BigDecimal currentAvailable = Optional.ofNullable(credit.getAvailableAmount()).orElse(BigDecimal.ZERO);
      credit.setAvailableAmount(currentAvailable.add(paidAmount));
      return;
    }

    if (newUsedAmount.compareTo(BigDecimal.ZERO) == 0) {
      credit.setStatus(CreditStatus.PAID_OFF);
    }
  }
}