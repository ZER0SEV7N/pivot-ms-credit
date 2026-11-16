package com.pe.pivot.credit.application.usecases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pe.pivot.credit.application.support.CreditDomainInitializer;
import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.exception.CustomerNotFoundException;
import com.pe.pivot.credit.domain.exception.DuplicateCreditException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditType;
import com.pe.pivot.credit.domain.model.CustomerSummary;
import com.pe.pivot.credit.domain.port.out.CreditRepositoryPort;
import com.pe.pivot.credit.domain.port.out.CustomerClientPort;
import com.pe.pivot.credit.domain.strategy.validation.CreditValidationStrategy;
import com.pe.pivot.credit.infrastructure.config.CreditRulesProperties;
import com.pe.pivot.credit.infrastructure.util.CryptoUtil;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link CreateCreditUseCaseImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCreditUseCaseImpl credit creation flow")
class CreateCreditUseCaseImplTest {

  @Mock
  private CreditRepositoryPort creditRepository;

  @Mock
  private CustomerClientPort customerClient;

  @Mock
  private CreditValidationStrategy validationStrategy;

  @Mock
  private CreditRulesProperties rulesProperties;

  @Mock
  private CreditDomainInitializer creditInitializer;

  @Mock
  private CryptoUtil cryptoUtil;

  private CreateCreditUseCaseImpl useCase;

  private Credit credit;

  @BeforeEach
  void setUp() {
    useCase = new CreateCreditUseCaseImpl(creditRepository, customerClient,
            List.of(validationStrategy), rulesProperties, creditInitializer, cryptoUtil);
    credit = Credit.builder()
            .customerId("cust-1")
            .creditType(CreditType.PERSONAL_LOAN)
            .creditNumber("LN-10000001")
            .build();
  }

  @Test
  @DisplayName("Creates a credit when the customer is active and validations pass")
  void createsCreditWhenCustomerIsActiveAndValidationsPass() {
    // Arrange
    CustomerSummary customer = activeCustomer();
    Credit existingCredit = Credit.builder().id("credit-old").build();
    Credit savedCredit = Credit.builder().id("credit-new").customerId("cust-1").build();
    when(customerClient.findCustomerById("cust-1")).thenReturn(Maybe.just(customer));
    when(creditRepository.findByCustomerId("cust-1")).thenReturn(Flowable.just(existingCredit));
    when(validationStrategy.supports("PERSONAL")).thenReturn(true);
    when(validationStrategy.validate(customer, credit, List.of(existingCredit)))
            .thenReturn(Completable.complete());
    when(creditRepository.existsByCreditNumberHash(any())).thenReturn(Single.just(false));
    when(creditRepository.save(credit)).thenReturn(Single.just(savedCredit));

    // Act
    TestObserver<Credit> observer = useCase.create(credit).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValueCount(1);
    observer.assertValue(savedCredit);
    observer.assertComplete();
    verify(creditInitializer).initializeDefaults(credit);
    verify(creditRepository).save(credit);
  }

  @Test
  @DisplayName("Fails when the customer does not exist")
  void failsWhenCustomerDoesNotExist() {
    // Arrange
    when(customerClient.findCustomerById("cust-1")).thenReturn(Maybe.empty());

    // Act
    TestObserver<Credit> observer = useCase.create(credit).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(CustomerNotFoundException.class);
    verifyNoInteractions(creditRepository);
    verifyNoInteractions(creditInitializer);
  }

  @Test
  @DisplayName("Fails when the customer is not active")
  void failsWhenCustomerIsNotActive() {
    // Arrange
    CustomerSummary inactiveCustomer = CustomerSummary.builder()
            .id("cust-1")
            .customerType("PERSONAL")
            .status("BLOCKED")
            .build();
    when(customerClient.findCustomerById("cust-1")).thenReturn(Maybe.just(inactiveCustomer));

    // Act
    TestObserver<Credit> observer = useCase.create(credit).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    verifyNoInteractions(creditRepository);
  }

  @Test
  @DisplayName("Fails when no validation strategy supports the customer type")
  void failsWhenNoValidationStrategySupportsCustomerType() {
    // Arrange
    CustomerSummary customer = activeCustomer();
    when(customerClient.findCustomerById("cust-1")).thenReturn(Maybe.just(customer));
    when(creditRepository.findByCustomerId("cust-1")).thenReturn(Flowable.empty());
    when(validationStrategy.supports("PERSONAL")).thenReturn(false);

    // Act
    TestObserver<Credit> observer = useCase.create(credit).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Fails when the validation strategy rejects the credit")
  void failsWhenValidationStrategyRejectsTheCredit() {
    // Arrange
    CustomerSummary customer = activeCustomer();
    BusinessRuleException rejection = new BusinessRuleException("Credit rejected by strategy");
    when(customerClient.findCustomerById("cust-1")).thenReturn(Maybe.just(customer));
    when(creditRepository.findByCustomerId("cust-1")).thenReturn(Flowable.empty());
    when(validationStrategy.supports("PERSONAL")).thenReturn(true);
    when(validationStrategy.validate(customer, credit, Collections.emptyList()))
            .thenReturn(Completable.error(rejection));

    // Act
    TestObserver<Credit> observer = useCase.create(credit).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(rejection);
    verify(creditRepository, never()).save(any());
    verifyNoInteractions(creditInitializer);
  }

  @Test
  @DisplayName("Fails when the credit number already exists")
  void failsWhenCreditNumberAlreadyExists() {
    // Arrange
    CustomerSummary customer = activeCustomer();
    when(customerClient.findCustomerById("cust-1")).thenReturn(Maybe.just(customer));
    when(creditRepository.findByCustomerId("cust-1")).thenReturn(Flowable.empty());
    when(validationStrategy.supports("PERSONAL")).thenReturn(true);
    when(validationStrategy.validate(customer, credit, Collections.emptyList()))
            .thenReturn(Completable.complete());
    when(creditRepository.existsByCreditNumberHash(any())).thenReturn(Single.just(true));

    // Act
    TestObserver<Credit> observer = useCase.create(credit).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(DuplicateCreditException.class);
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Propagates repository errors while querying customer credits")
  void propagatesRepositoryErrorWhenQueryingCustomerCredits() {
    // Arrange
    CustomerSummary customer = activeCustomer();
    when(customerClient.findCustomerById("cust-1")).thenReturn(Maybe.just(customer));
    when(creditRepository.findByCustomerId("cust-1"))
            .thenReturn(Flowable.error(new IllegalStateException("Database unavailable")));

    // Act
    TestObserver<Credit> observer = useCase.create(credit).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(IllegalStateException.class);
    verify(creditRepository, never()).save(any());
  }

  private CustomerSummary activeCustomer() {
    return CustomerSummary.builder()
            .id("cust-1")
            .customerType("PERSONAL")
            .status("ACTIVE")
            .build();
  }
}
