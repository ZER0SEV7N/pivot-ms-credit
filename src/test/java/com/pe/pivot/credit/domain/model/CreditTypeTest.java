package com.pe.pivot.credit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CreditType} product metadata and convenience methods.
 */
@DisplayName("Should expose CreditType product metadata")
class CreditTypeTest {

  @Test
  @DisplayName("Should expose the correct personal loan metadata")
  void personalLoanMetadataIsCorrect() {
    // Arrange
    CreditType type = CreditType.PERSONAL_LOAN;

    // Act
    boolean creditCard = type.isCreditCard();
    boolean business = type.isBusinessProduct();

    // Assert
    assertThat(type.getNumberPrefix()).isEqualTo("LN-");
    assertThat(creditCard).isFalse();
    assertThat(business).isFalse();
  }

  @Test
  @DisplayName("Should expose the correct business loan metadata")
  void businessLoanMetadataIsCorrect() {
    // Arrange
    CreditType type = CreditType.BUSINESS_LOAN;

    // Act
    boolean creditCard = type.isCreditCard();
    boolean business = type.isBusinessProduct();

    // Assert
    assertThat(type.getNumberPrefix()).isEqualTo("LN-");
    assertThat(creditCard).isFalse();
    assertThat(business).isTrue();
  }

  @Test
  @DisplayName("Should expose the correct personal credit card metadata")
  void personalCreditCardMetadataIsCorrect() {
    // Arrange
    CreditType type = CreditType.PERSONAL_CREDIT_CARD;

    // Act
    boolean creditCard = type.isCreditCard();
    boolean business = type.isBusinessProduct();

    // Assert
    assertThat(type.getNumberPrefix()).isEqualTo("4557-8800-");
    assertThat(creditCard).isTrue();
    assertThat(business).isFalse();
  }

  @Test
  @DisplayName("Should expose the correct business credit card metadata")
  void businessCreditCardMetadataIsCorrect() {
    // Arrange
    CreditType type = CreditType.BUSINESS_CREDIT_CARD;

    // Act
    boolean creditCard = type.isCreditCard();
    boolean business = type.isBusinessProduct();

    // Assert
    assertThat(type.getNumberPrefix()).isEqualTo("4557-8800-");
    assertThat(creditCard).isTrue();
    assertThat(business).isTrue();
  }
}
