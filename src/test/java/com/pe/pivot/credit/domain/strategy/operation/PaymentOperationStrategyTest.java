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
@DisplayName("Should be able to process credit payments")
class PaymentOperationStrategyTest {

  private PaymentOperationStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new PaymentOperationStrategy();
  }

  @Test
  @DisplayName("Should be applied successfully on an active loan with a partial payment")
  void shouldBeAppliedSuccessfullyOnActiveLoanWithPartialPayment() {
    // (happy path)
    // Arrange
    Credit credit = credit(CreditType.PERSONAL_LOAN, CreditStatus.ACTIVE, "100.00", null);

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("40.00"))
            .test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
    observer.assertValueCount(1);
    observer.assertValue(credit);
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("60.00");
    assertThat(credit.getStatus()).isEqualTo(CreditStatus.ACTIVE);
    assertThat(credit.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should be supported only for payment operations")
  void shouldBeSupportedOnlyForPaymentOperations() {
    // Arrange - no dependencies required

    // Act
    boolean paymentSupported = strategy.supports(CreditOperationType.PAYMENT);
    boolean consumptionSupported = strategy.supports(CreditOperationType.CONSUMPTION);
    boolean nullSupported = strategy.supports(null);

    // Assert
    assertThat(paymentSupported).isTrue();
    assertThat(consumptionSupported).isFalse();
    assertThat(nullSupported).isFalse();
  }

  @Test
  @DisplayName("Should be rejected when the credit status does not allow payments")
  void shouldBeRejectedWhenCreditStatusDoesNotAllowPayments() {
    // (error: non-payable credit status)
    // Arrange
    Credit credit = credit(CreditType.PERSONAL_LOAN, CreditStatus.CLOSED, "100.00", null);

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("40.00"))
            .test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("cannot receive payments"));
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("100.00");
  }

  @Test
  @DisplayName("Should be rejected when there is no outstanding debt")
  void shouldBeRejectedWhenThereIsNoOutstandingDebt() {
    // (error: no outstanding debt)
    // Arrange
    Credit credit = credit(CreditType.PERSONAL_LOAN, CreditStatus.ACTIVE, "0.00", null);

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("10.00"))
            .test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("no outstanding debt"));
  }

  @Test
  @DisplayName("Should be rejected treating a null used amount as no outstanding debt")
  void shouldBeRejectedTreatingNullUsedAmountAsNoOutstandingDebt() {
    // (error: null outstanding debt)
    // Arrange
    Credit credit = credit(CreditType.PERSONAL_LOAN, CreditStatus.ACTIVE, null, null);

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("10.00"))
            .test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
  }

  @Test
  @DisplayName("Should be rejected when the payment exceeds the outstanding debt")
  void shouldBeRejectedWhenPaymentExceedsOutstandingDebt() {
    // (error: payment greater than outstanding debt)
    // Arrange
    Credit credit = credit(CreditType.PERSONAL_LOAN, CreditStatus.ACTIVE, "100.00", null);

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("150.00"))
            .test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("cannot exceed"));
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("100.00");
  }

  @Test
  @DisplayName("Should be marked as paid off when a business loan debt is fully settled")
  void shouldBeMarkedAsPaidOffWhenBusinessLoanDebtIsFullySettled() {
    // Arrange
    Credit credit = credit(CreditType.BUSINESS_LOAN, CreditStatus.ACTIVE, "100.00", null);

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("100.00"))
            .test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
    observer.assertValue(credit);
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("0.00");
    assertThat(credit.getStatus()).isEqualTo(CreditStatus.PAID_OFF);
  }

  @Test
  @DisplayName("Should be restored to active when an overdue debt is partially paid")
  void shouldBeRestoredToActiveWhenOverdueDebtIsPartiallyPaid() {
    // Arrange
    Credit credit = credit(CreditType.PERSONAL_LOAN, CreditStatus.OVERDUE, "100.00", null);

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("40.00"))
            .test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("60.00");
    assertThat(credit.getStatus()).isEqualTo(CreditStatus.ACTIVE);
  }

  @Test
  @DisplayName("Should be restored the available limit on a credit card payment")
  void shouldBeRestoredTheAvailableLimitOnCreditCardPayment() {
    // Arrange
    Credit credit = credit(CreditType.PERSONAL_CREDIT_CARD, CreditStatus.ACTIVE, "100.00",
            "400.00");

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("40.00"))
            .test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
    observer.assertValue(credit);
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("60.00");
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("440.00");
  }

  @Test
  @DisplayName("Should be calculated treating a null available amount as zero on a card payment")
  void shouldBeCalculatedTreatingNullAvailableAmountAsZeroOnCardPayment() {
    // Arrange
    Credit credit = credit(CreditType.BUSINESS_CREDIT_CARD, CreditStatus.ACTIVE, "100.00", null);

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("40.00"))
            .test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
    observer.assertValueCount(1);
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("40.00");
  }

  private Credit credit(CreditType type, CreditStatus status, String usedAmount,
                        String availableAmount) {
    return Credit.builder()
            .creditType(type)
            .status(status)
            .usedAmount(usedAmount == null ? null : new BigDecimal(usedAmount))
            .availableAmount(availableAmount == null ? null : new BigDecimal(availableAmount))
            .build();
  }
}
