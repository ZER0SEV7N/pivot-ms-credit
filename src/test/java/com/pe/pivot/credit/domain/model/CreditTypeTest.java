package com.pe.pivot.credit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CreditType} product metadata and convenience methods.
 */
@DisplayName("CreditType product metadata")
class CreditTypeTest {

  @Test
  @DisplayName("Personal loan metadata is correct")
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
  @DisplayName("Business loan metadata is correct")
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
  @DisplayName("Personal credit card metadata is correct")
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
  @DisplayName("Business credit card metadata is correct")
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
