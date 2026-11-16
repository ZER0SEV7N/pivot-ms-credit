package com.pe.pivot.credit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CustomerSummary} projection predicates.
 */
@DisplayName("CustomerSummary projection predicates")
class CustomerSummaryTest {

  @Test
  @DisplayName("Detects an active customer ignoring case")
  void detectsActiveCustomerIgnoringCase() {
    // Arrange
    CustomerSummary customer = CustomerSummary.builder().status("active").build();

    // Act
    boolean active = customer.isActive();

    // Assert
    assertThat(active).isTrue();
  }

  @Test
  @DisplayName("Returns false when the customer is not active")
  void returnsFalseWhenCustomerIsNotActive() {
    // Arrange
    CustomerSummary customer = CustomerSummary.builder().status("BLOCKED").build();

    // Act
    boolean active = customer.isActive();

    // Assert
    assertThat(active).isFalse();
  }

  @Test
  @DisplayName("Returns false when the customer status is null")
  void returnsFalseWhenCustomerStatusIsNull() {
    // Arrange
    CustomerSummary customer = CustomerSummary.builder().status(null).build();

    // Act
    boolean active = customer.isActive();

    // Assert
    assertThat(active).isFalse();
  }

  @Test
  @DisplayName("Detects a personal customer ignoring case")
  void detectsPersonalCustomerIgnoringCase() {
    // Arrange
    CustomerSummary customer = CustomerSummary.builder().customerType("personal").build();

    // Act
    boolean personal = customer.isPersonal();

    // Assert
    assertThat(personal).isTrue();
  }

  @Test
  @DisplayName("Detects a business customer ignoring case")
  void detectsBusinessCustomerIgnoringCase() {
    // Arrange
    CustomerSummary customer = CustomerSummary.builder().customerType("business").build();

    // Act
    boolean business = customer.isBusiness();

    // Assert
    assertThat(business).isTrue();
    assertThat(customer.isPersonal()).isFalse();
  }

  @Test
  @DisplayName("Returns false for a null customer type")
  void returnsFalseForNullCustomerType() {
    // Arrange
    CustomerSummary customer = CustomerSummary.builder().customerType(null).build();

    // Act
    boolean personal = customer.isPersonal();
    boolean business = customer.isBusiness();

    // Assert
    assertThat(personal).isFalse();
    assertThat(business).isFalse();
  }
}
