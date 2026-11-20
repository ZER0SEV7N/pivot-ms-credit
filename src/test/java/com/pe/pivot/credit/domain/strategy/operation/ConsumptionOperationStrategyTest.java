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
 * Unit tests for {@link ConsumptionOperationStrategy}.
 */
@DisplayName("Should be able to process credit card consumptions")
class ConsumptionOperationStrategyTest {

  private ConsumptionOperationStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new ConsumptionOperationStrategy();
  }

  @Test
  @DisplayName("Should be applied successfully when the credit card is active and has enough limit")
  void shouldBeAppliedSuccessfullyWhenCreditCardIsActiveAndHasEnoughLimit() {
    // (happy path)
    // Arrange
    Credit credit = creditCard(CreditStatus.ACTIVE, "100.00", "400.00");

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("50.00"))
            .test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
    observer.assertValueCount(1);
    observer.assertValue(credit);
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("150.00");
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("350.00");
    assertThat(credit.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should be supported only for consumption operations")
  void shouldBeSupportedOnlyForConsumptionOperations() {
    // Arrange - no dependencies required

    // Act
    boolean consumptionSupported = strategy.supports(CreditOperationType.CONSUMPTION);
    boolean paymentSupported = strategy.supports(CreditOperationType.PAYMENT);
    boolean nullSupported = strategy.supports(null);

    // Assert
    assertThat(consumptionSupported).isTrue();
    assertThat(paymentSupported).isFalse();
    assertThat(nullSupported).isFalse();
  }

  @Test
  @DisplayName("Should be rejected when the credit is not active")
  void shouldBeRejectedWhenCreditIsNotActive() {
    // (error: non-active credit status)
    // Arrange
    Credit credit = creditCard(CreditStatus.CLOSED, "100.00", "400.00");

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("50.00"))
            .test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("current status"));
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("100.00");
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("400.00");
  }

  @Test
  @DisplayName("Should be rejected when the product is a loan")
  void shouldBeRejectedWhenProductIsLoan() {
    // (error: non credit card product)
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.PERSONAL_LOAN)
            .status(CreditStatus.ACTIVE)
            .usedAmount(new BigDecimal("100.00"))
            .availableAmount(new BigDecimal("0.00"))
            .build();

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("10.00"))
            .test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("only allowed on Credit Card"));
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("100.00");
  }

  @Test
  @DisplayName("Should be rejected when the available limit is insufficient")
  void shouldBeRejectedWhenAvailableLimitIsInsufficient() {
    // (error: insufficient available limit)
    // Arrange
    Credit credit = creditCard(CreditStatus.ACTIVE, "450.00", "50.00");

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("60.00"))
            .test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("Insufficient available credit"));
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("450.00");
  }

  @Test
  @DisplayName("Should be calculated treating a null used amount as zero")
  void shouldBeCalculatedTreatingNullUsedAmountAsZero() {
    // Arrange
    Credit credit = creditCard(CreditStatus.ACTIVE, null, "200.00");

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("25.00"))
            .test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
    observer.assertValue(credit);
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("25.00");
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("175.00");
  }

  @Test
  @DisplayName("Should be calculated treating a null available amount as zero")
  void shouldBeCalculatedTreatingNullAvailableAmountAsZero() {
    // Arrange
    Credit credit = creditCard(CreditStatus.ACTIVE, "10.00", null);

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, BigDecimal.ZERO).test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
    observer.assertValueCount(1);
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("10.00");
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("0.00");
  }

  private Credit creditCard(CreditStatus status, String usedAmount, String availableAmount) {
    return Credit.builder()
            .creditType(CreditType.PERSONAL_CREDIT_CARD)
            .status(status)
            .usedAmount(usedAmount == null ? null : new BigDecimal(usedAmount))
            .availableAmount(availableAmount == null ? null : new BigDecimal(availableAmount))
            .build();
  }
}
