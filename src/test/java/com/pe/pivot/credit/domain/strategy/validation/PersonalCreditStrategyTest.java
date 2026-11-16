package com.pe.pivot.credit.domain.strategy.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.model.CreditType;
import com.pe.pivot.credit.domain.model.CustomerSummary;
import io.reactivex.rxjava3.observers.TestObserver;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PersonalCreditStrategy}.
 */
@DisplayName("PersonalCreditStrategy customer validation")
class PersonalCreditStrategyTest {

  private PersonalCreditStrategy strategy;

  private CustomerSummary customer;

  @BeforeEach
  void setUp() {
    strategy = new PersonalCreditStrategy();
    customer = CustomerSummary.builder()
            .id("cust-p")
            .customerType("PERSONAL")
            .status("ACTIVE")
            .build();
  }

  @Test
  @DisplayName("Supports only personal customers")
  void supportsOnlyPersonalCustomers() {
    // Act
    boolean personalSupported = strategy.supports("PERSONAL");
    boolean businessSupported = strategy.supports("BUSINESS");
    boolean nullSupported = strategy.supports(null);

    // Assert
    assertThat(personalSupported).isTrue();
    assertThat(businessSupported).isFalse();
    assertThat(nullSupported).isFalse();
  }

  @Test
  @DisplayName("Accepts a personal credit card when no restrictions apply")
  void acceptsPersonalCreditCardWhenNoRestrictionsApply() {
    // Arrange
    Credit newCredit = Credit.builder().creditType(CreditType.PERSONAL_CREDIT_CARD).build();

    // Act
    TestObserver<Void> observer = strategy
            .validate(customer, newCredit, Collections.emptyList())
            .test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
  }

  @Test
  @DisplayName("Rejects a business loan for a personal customer")
  void rejectsBusinessLoanForPersonalCustomer() {
    // Arrange
    Credit newCredit = Credit.builder().creditType(CreditType.BUSINESS_LOAN).build();

    // Act
    TestObserver<Void> observer = strategy
            .validate(customer, newCredit, Collections.emptyList())
            .test();

    // Assert
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("Personal customers cannot request"));
  }

  @Test
  @DisplayName("Rejects a business credit card for a personal customer")
  void rejectsBusinessCreditCardForPersonalCustomer() {
    // Arrange
    Credit newCredit = Credit.builder().creditType(CreditType.BUSINESS_CREDIT_CARD).build();

    // Act
    TestObserver<Void> observer = strategy
            .validate(customer, newCredit, Collections.emptyList())
            .test();

    // Assert
    observer.assertError(BusinessRuleException.class);
  }

  @Test
  @DisplayName("Rejects a second active personal loan")
  void rejectsSecondActivePersonalLoan() {
    // Arrange
    Credit newCredit = Credit.builder().creditType(CreditType.PERSONAL_LOAN).build();
    Credit existingLoan = Credit.builder()
            .creditType(CreditType.PERSONAL_LOAN)
            .status(CreditStatus.ACTIVE)
            .build();

    // Act
    TestObserver<Void> observer = strategy
            .validate(customer, newCredit, List.of(existingLoan))
            .test();

    // Assert
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("only allowed to hold one active"));
  }

  @Test
  @DisplayName("Allows a personal loan when the previous loan is closed")
  void allowsPersonalLoanWhenPreviousLoanIsClosed() {
    // Arrange
    Credit newCredit = Credit.builder().creditType(CreditType.PERSONAL_LOAN).build();
    Credit closedLoan = Credit.builder()
            .creditType(CreditType.PERSONAL_LOAN)
            .status(CreditStatus.CLOSED)
            .build();

    // Act
    TestObserver<Void> observer = strategy
            .validate(customer, newCredit, List.of(closedLoan))
            .test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
  }

  @Test
  @DisplayName("Allows a personal loan when other credits are not personal loans")
  void allowsPersonalLoanWhenExistingCreditsAreNotPersonalLoans() {
    // Arrange
    Credit newCredit = Credit.builder().creditType(CreditType.PERSONAL_LOAN).build();
    Credit existingCard = Credit.builder()
            .creditType(CreditType.PERSONAL_CREDIT_CARD)
            .status(CreditStatus.ACTIVE)
            .build();

    // Act
    TestObserver<Void> observer = strategy
            .validate(customer, newCredit, List.of(existingCard))
            .test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
  }
}
