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
@DisplayName("Should handle consumption operations")
class ConsumptionOperationStrategyTest {

  private ConsumptionOperationStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new ConsumptionOperationStrategy();
  }

  /**
   * Verifies that the strategy only claims support for consumption operations.
   */
  @Test
  @DisplayName("Should support only consumption operations")
  void supportsOnlyConsumptionOperations() {
    // Act
    boolean consumptionSupported = strategy.supports(CreditOperationType.CONSUMPTION);
    boolean paymentSupported = strategy.supports(CreditOperationType.PAYMENT);

    // Assert
    assertThat(consumptionSupported).isTrue();
    assertThat(paymentSupported).isFalse();
  }

  @Test
  @DisplayName("Should reject consumption on loan products")
  void rejectsConsumptionOnLoanProducts() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.PERSONAL_LOAN)
            .usedAmount(new BigDecimal("100.00"))
            .availableAmount(new BigDecimal("0.00"))
            .build();

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("10.00")).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("100.00");
  }

  @Test
  @DisplayName("Should reject consumption when the available limit is insufficient")
  void rejectsConsumptionWhenAvailableLimitIsInsufficient() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.PERSONAL_CREDIT_CARD)
            .usedAmount(new BigDecimal("450.00"))
            .availableAmount(new BigDecimal("50.00"))
            .build();

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("60.00")).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("Insufficient available credit"));
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("450.00");
  }

  @Test
  @DisplayName("Should apply consumption updating used and available amounts")
  void appliesConsumptionUpdatingUsedAndAvailableAmounts() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.PERSONAL_CREDIT_CARD)
            .usedAmount(new BigDecimal("100.00"))
            .availableAmount(new BigDecimal("400.00"))
            .build();

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("50.00")).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(credit);
    observer.assertComplete();
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("150.00");
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("350.00");
    assertThat(credit.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should treat a null used amount as zero")
  void treatsNullUsedAmountAsZero() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.BUSINESS_CREDIT_CARD)
            .usedAmount(null)
            .availableAmount(new BigDecimal("200.00"))
            .build();

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, new BigDecimal("25.00")).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(credit);
    observer.assertComplete();
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("25.00");
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("175.00");
  }

  @Test
  @DisplayName("Should treat a null available amount as zero")
  void treatsNullAvailableAmountAsZero() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.PERSONAL_CREDIT_CARD)
            .usedAmount(new BigDecimal("10.00"))
            .availableAmount(null)
            .build();

    // Act
    TestObserver<Credit> observer = strategy.applyOperation(credit, BigDecimal.ZERO).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(credit);
    observer.assertComplete();
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("10.00");
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("0");
  }
}
