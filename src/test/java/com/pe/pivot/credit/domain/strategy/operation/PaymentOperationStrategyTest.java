package com.pe.pivot.credit.domain.strategy.operation;

import static org.assertj.core.api.Assertions.assertThat;

import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditOperationType;
import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.model.CreditType;
import io.reactivex.rxjava3.observers.TestObserver;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PaymentOperationStrategy}.
 */
@DisplayName("Should handle payment operations")
class PaymentOperationStrategyTest {

  private PaymentOperationStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new PaymentOperationStrategy();
  }

  /**
   * Verifies that the strategy only claims support for payment operations.
   */
  @Test
  @DisplayName("Should support only payment operations")
  void supportsOnlyPaymentOperations() {
    // Act
    boolean paymentSupported = strategy.supports(CreditOperationType.PAYMENT);
    boolean consumptionSupported = strategy.supports(CreditOperationType.CONSUMPTION);

    // Assert
    assertThat(paymentSupported).isTrue();
    assertThat(consumptionSupported).isFalse();
  }

  @Test
  @DisplayName("Should reject payment when there is no outstanding debt")
  void rejectsPaymentWhenThereIsNoOutstandingDebt() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.PERSONAL_LOAN)
            .usedAmount(BigDecimal.ZERO)
            .build();

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("10.00")).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("no outstanding debt"));
  }

  @Test
  @DisplayName("Should treat a null used amount as no outstanding debt")
  void treatsNullUsedAmountAsNoOutstandingDebt() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.PERSONAL_LOAN)
            .usedAmount(null)
            .build();

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("10.00")).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
  }

  @Test
  @DisplayName("Should reject a payment that exceeds the outstanding debt")
  void rejectsPaymentThatExceedsOutstandingDebt() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.PERSONAL_LOAN)
            .usedAmount(new BigDecimal("100.00"))
            .build();

    // Act
    TestObserver<Credit> observer = strategy
            .applyOperation(credit, new BigDecimal("150.00")).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("cannot exceed"));
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("100.00");
  }

  @Test
  @DisplayName("Should apply a partial payment on a loan without changing its status")
  void appliesPartialPaymentOnLoanWithoutChangingStatus() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.PERSONAL_LOAN)
            .usedAmount(new BigDecimal("100.00"))
            .status(CreditStatus.ACTIVE)
            .build();

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("40.00")).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(credit);
    observer.assertComplete();
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("60.00");
    assertThat(credit.getStatus()).isEqualTo(CreditStatus.ACTIVE);
    assertThat(credit.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should mark a loan as paid off when the debt is fully settled")
  void marksLoanAsPaidOffWhenDebtIsFullySettled() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.BUSINESS_LOAN)
            .usedAmount(new BigDecimal("100.00"))
            .status(CreditStatus.ACTIVE)
            .build();

    // Act
    TestObserver<Credit> observer = strategy
            .applyOperation(credit, new BigDecimal("100.00")).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(credit);
    observer.assertComplete();
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("0.00");
    assertThat(credit.getStatus()).isEqualTo(CreditStatus.PAID_OFF);
  }

  @Test
  @DisplayName("Should restore the available limit on a credit card payment")
  void restoresAvailableLimitOnCreditCardPayment() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.PERSONAL_CREDIT_CARD)
            .usedAmount(new BigDecimal("100.00"))
            .availableAmount(new BigDecimal("400.00"))
            .status(CreditStatus.ACTIVE)
            .build();

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("40.00")).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(credit);
    observer.assertComplete();
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("60.00");
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("440.00");
    assertThat(credit.getStatus()).isEqualTo(CreditStatus.ACTIVE);
  }

  @Test
  @DisplayName("Should treat a null available amount as zero on a card payment")
  void treatsNullAvailableAmountAsZeroOnCreditCardPayment() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.BUSINESS_CREDIT_CARD)
            .usedAmount(new BigDecimal("100.00"))
            .availableAmount(null)
            .build();

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("40.00")).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(credit);
    observer.assertComplete();
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("40.00");
  }
}
