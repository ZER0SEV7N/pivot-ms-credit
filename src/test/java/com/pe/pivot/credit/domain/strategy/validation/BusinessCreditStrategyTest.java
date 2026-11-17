package com.pe.pivot.credit.domain.strategy.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditType;
import com.pe.pivot.credit.domain.model.CustomerSummary;
import io.reactivex.rxjava3.observers.TestObserver;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BusinessCreditStrategy}.
 */
@DisplayName("Should validate business customer credits")
class BusinessCreditStrategyTest {

  private BusinessCreditStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new BusinessCreditStrategy();
  }

  /**
   * Verifies that the strategy only claims support for business customers.
   */
  @Test
  @DisplayName("Should support only business customers")
  void supportsOnlyBusinessCustomers() {
    // Act
    boolean businessSupported = strategy.supports("BUSINESS");
    boolean personalSupported = strategy.supports("PERSONAL");
    boolean nullSupported = strategy.supports(null);

    // Assert
    assertThat(businessSupported).isTrue();
    assertThat(personalSupported).isFalse();
    assertThat(nullSupported).isFalse();
  }

  @Test
  @DisplayName("Should accept a business loan for a business customer")
  void acceptsBusinessLoanForBusinessCustomer() {
    // Arrange
    CustomerSummary customer = businessCustomer();
    Credit newCredit = Credit.builder().creditType(CreditType.BUSINESS_LOAN).build();

    // Act
    TestObserver<Void> observer = strategy
            .validate(customer, newCredit, Collections.emptyList())
            .test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
  }

  @Test
  @DisplayName("Should accept a business credit card for a business customer")
  void acceptsBusinessCreditCardForBusinessCustomer() {
    // Arrange
    CustomerSummary customer = businessCustomer();
    Credit newCredit = Credit.builder().creditType(CreditType.BUSINESS_CREDIT_CARD).build();

    // Act
    TestObserver<Void> observer = strategy
            .validate(customer, newCredit, Collections.emptyList())
            .test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
  }

  @Test
  @DisplayName("Should reject a personal loan for a business customer")
  void rejectsPersonalLoanForBusinessCustomer() {
    // Arrange
    CustomerSummary customer = businessCustomer();
    Credit newCredit = Credit.builder().creditType(CreditType.PERSONAL_LOAN).build();

    // Act
    TestObserver<Void> observer = strategy
            .validate(customer, newCredit, Collections.emptyList())
            .test();

    // Assert
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("Business customers cannot request"));
  }

  @Test
  @DisplayName("Should reject a personal credit card for a business customer")
  void rejectsPersonalCreditCardForBusinessCustomer() {
    // Arrange
    CustomerSummary customer = businessCustomer();
    Credit newCredit = Credit.builder().creditType(CreditType.PERSONAL_CREDIT_CARD).build();

    // Act
    TestObserver<Void> observer = strategy
            .validate(customer, newCredit, Collections.emptyList())
            .test();

    // Assert
    observer.assertError(BusinessRuleException.class);
  }

  private CustomerSummary businessCustomer() {
    return CustomerSummary.builder()
            .id("cust-b")
            .customerType("BUSINESS")
            .status("ACTIVE")
            .build();
  }
}
