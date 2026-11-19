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
   * Determines if this strategy supports the given credit operation type. In this case, it only
   * supports PAYMENT operations.
   *
   * @param operationType - The type of credit operation to check for support.
   * @return - true if the operation type is PAYMENT, false otherwise.
   */
  @Override
  public boolean supports(CreditOperationType operationType) {
    return operationType == CreditOperationType.PAYMENT;
  }

  /**
   * Applies the payment operation to an ACTIVE or OVERDUE credit product.
   *
   * @param credit the credit to apply the payment to
   * @param amount the amount to pay
   * @return a Single emitting the updated credit
   */
  @Override
  public Single<Credit> applyOperation(Credit credit, BigDecimal amount) {
    // Validate that the credit is in a state that allows payments (ACTIVE or OVERDUE).
    if (credit.getStatus() != CreditStatus.ACTIVE
            && credit.getStatus() != CreditStatus.OVERDUE) {
      return Single.error(new BusinessRuleException(
              "Credit product cannot receive payments in current status: " + credit.getStatus()));
    }

    // Validate that the credit has an outstanding debt (usedAmount > 0).
    BigDecimal currentUsed = Optional.ofNullable(credit.getUsedAmount()).orElse(BigDecimal.ZERO);

    // Validate that the payment amount does not exceed the current outstanding debt.
    if (currentUsed.compareTo(BigDecimal.ZERO) == 0) {
      return Single.error(new BusinessRuleException(
              "Credit product has no outstanding debt (usedAmount is 0.00)."));
    }

    // Validate that the payment amount does not exceed the current outstanding debt.
    if (amount.compareTo(currentUsed) > 0) {
      return Single.error(new BusinessRuleException(
              "Payment amount (" + amount + ") cannot exceed current outstanding debt ("
                      + currentUsed + ")."));
    }

    BigDecimal newUsedAmount = currentUsed.subtract(amount);
    credit.setUsedAmount(newUsedAmount);
    // Update the available amount and potentially change the credit status based on the payment.
    updatePostPaymentState(credit, amount, newUsedAmount);
    credit.setUpdatedAt(Instant.now());

    return Single.just(credit);
  }

  /**
   * Updates available credit line for cards and transitions lifecycle status after payment.
   *
   * @param credit the credit to update
   * @param paidAmount the amount that was paid
   * @param newUsedAmount the remaining used amount after the payment
   */
  private void updatePostPaymentState(
          Credit credit, BigDecimal paidAmount, BigDecimal newUsedAmount) {
    // If the credit is a credit card, increase the available amount by the paid amount.
    if (credit.getCreditType().isCreditCard()) {
      BigDecimal currentAvailable =
              Optional.ofNullable(credit.getAvailableAmount()).orElse(BigDecimal.ZERO);
      credit.setAvailableAmount(currentAvailable.add(paidAmount));
    }

    // If the credit is a loan and the new used amount is zero, transition to PAID_OFF status.
    if (newUsedAmount.compareTo(BigDecimal.ZERO) == 0 && credit.getCreditType().isLoan()) {
      credit.setStatus(CreditStatus.PAID_OFF);
    // If the credit was previously OVERDUE, transition it back to ACTIVE status.
    } else if (credit.getStatus() == CreditStatus.OVERDUE) {
      credit.setStatus(CreditStatus.ACTIVE);
    }
  }
}