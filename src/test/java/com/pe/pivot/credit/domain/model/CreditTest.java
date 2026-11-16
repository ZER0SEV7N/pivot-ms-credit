package com.pe.pivot.credit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Credit} business logic methods.
 */
@DisplayName("Credit domain business rules")
class CreditTest {

  @Test
  @DisplayName("Can be closed when the used amount is null")
  void canBeClosedWhenUsedAmountIsNull() {
    // Arrange
    Credit credit = Credit.builder().usedAmount(null).build();

    // Act
    boolean result = credit.canBeClosed();

    // Assert
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Can be closed when the used amount is zero")
  void canBeClosedWhenUsedAmountIsZero() {
    // Arrange
    Credit credit = Credit.builder().usedAmount(new BigDecimal("0.00")).build();

    // Act
    boolean result = credit.canBeClosed();

    // Assert
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Cannot be closed when the used amount is greater than zero")
  void cannotBeClosedWhenUsedAmountIsPositive() {
    // Arrange
    Credit credit = Credit.builder().usedAmount(new BigDecimal("0.01")).build();

    // Act
    boolean result = credit.canBeClosed();

    // Assert
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("Detects an active personal loan")
  void activePersonalLoanReturnsTrueForActivePersonalLoan() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.PERSONAL_LOAN)
            .status(CreditStatus.ACTIVE)
            .build();

    // Act
    boolean result = credit.isActivePersonalLoan();

    // Assert
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("Does not detect a closed personal loan as active")
  void activePersonalLoanReturnsFalseForClosedPersonalLoan() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.PERSONAL_LOAN)
            .status(CreditStatus.CLOSED)
            .build();

    // Act
    boolean result = credit.isActivePersonalLoan();

    // Assert
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("Does not detect a paid off personal loan as active")
  void activePersonalLoanReturnsFalseForPaidOffPersonalLoan() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.PERSONAL_LOAN)
            .status(CreditStatus.PAID_OFF)
            .build();

    // Act
    boolean result = credit.isActivePersonalLoan();

    // Assert
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("Does not detect another product type as a personal loan")
  void activePersonalLoanReturnsFalseForNonPersonalLoanProduct() {
    // Arrange
    Credit credit = Credit.builder()
            .creditType(CreditType.BUSINESS_LOAN)
            .status(CreditStatus.ACTIVE)
            .build();

    // Act
    boolean result = credit.isActivePersonalLoan();

    // Assert
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("Getters and setters round trip every field")
  void settersAndGettersRoundTripAllFields() {
    // Arrange
    Credit credit = new Credit();

    // Act
    credit.setId("credit-1");
    credit.setCreditNumber("LN-1234567890");
    credit.setCreditNumberHash("hash");
    credit.setCustomerId("cust-1");
    credit.setCreditType(CreditType.PERSONAL_LOAN);
    credit.setCurrency("PEN");
    credit.setCreditLimit(new BigDecimal("1000.00"));
    credit.setUsedAmount(new BigDecimal("250.00"));
    credit.setAvailableAmount(new BigDecimal("750.00"));
    credit.setInterestRate(new BigDecimal("0.15"));
    credit.setPaymentDueDay(15);
    credit.setStatus(CreditStatus.ACTIVE);
    credit.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
    credit.setUpdatedAt(Instant.parse("2026-01-02T00:00:00Z"));

    // Assert
    assertThat(credit.getId()).isEqualTo("credit-1");
    assertThat(credit.getCreditNumber()).isEqualTo("LN-1234567890");
    assertThat(credit.getCreditNumberHash()).isEqualTo("hash");
    assertThat(credit.getCustomerId()).isEqualTo("cust-1");
    assertThat(credit.getCreditType()).isEqualTo(CreditType.PERSONAL_LOAN);
    assertThat(credit.getCurrency()).isEqualTo("PEN");
    assertThat(credit.getCreditLimit()).isEqualByComparingTo("1000.00");
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("250.00");
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("750.00");
    assertThat(credit.getInterestRate()).isEqualByComparingTo("0.15");
    assertThat(credit.getPaymentDueDay()).isEqualTo(15);
    assertThat(credit.getStatus()).isEqualTo(CreditStatus.ACTIVE);
    assertThat(credit.getCreatedAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    assertThat(credit.getUpdatedAt()).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"));
  }
}
