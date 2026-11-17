package com.pe.pivot.credit.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditOperationType;
import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.model.CreditType;
import com.pe.pivot.credit.domain.port.in.CreateCreditUseCase;
import com.pe.pivot.credit.domain.port.in.DeleteCreditUseCase;
import com.pe.pivot.credit.domain.port.in.RetrieveCreditUseCase;
import com.pe.pivot.credit.domain.port.in.UpdateCreditBalanceUseCase;
import com.pe.pivot.credit.domain.port.in.UpdateCreditUseCase;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditBalanceResponse;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditBalanceUpdateRequest;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditRequest;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditResponse;
import com.pe.pivot.credit.infrastructure.adapter.in.web.mapper.CreditWebMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link CreditController}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Should expose the credit REST endpoints")
class CreditControllerTest {

  @Mock
  private CreateCreditUseCase createCreditUseCase;

  @Mock
  private RetrieveCreditUseCase retrieveCreditUseCase;

  @Mock
  private UpdateCreditUseCase updateCreditUseCase;

  @Mock
  private UpdateCreditBalanceUseCase updateCreditBalanceUseCase;

  @Mock
  private DeleteCreditUseCase deleteCreditUseCase;

  private CreditController controller;

  private Credit credit;

  @BeforeEach
  void setUp() {
    controller = new CreditController(createCreditUseCase, retrieveCreditUseCase,
            updateCreditUseCase, updateCreditBalanceUseCase, deleteCreditUseCase,
            new CreditWebMapper());
    credit = Credit.builder()
            .id("credit-1")
            .creditNumber("LN-1234567890")
            .creditNumberHash("hashed")
            .customerId("cust-1")
            .creditType(CreditType.PERSONAL_LOAN)
            .currency("PEN")
            .creditLimit(new BigDecimal("1000.00"))
            .usedAmount(new BigDecimal("1000.00"))
            .availableAmount(BigDecimal.ZERO)
            .interestRate(new BigDecimal("0.15"))
            .paymentDueDay(15)
            .status(CreditStatus.ACTIVE)
            .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
            .updatedAt(Instant.parse("2026-01-01T00:00:00Z"))
            .build();
  }

  @Test
  @DisplayName("Should return an unmasked credit number when creating a credit")
  void createCreditReturnsUnmaskedCreditNumber() {
    // Arrange
    CreditRequest request = CreditRequest.builder()
            .creditNumber("LN-1234567890")
            .customerId("cust-1")
            .creditType(CreditType.PERSONAL_LOAN)
            .currency("PEN")
            .creditLimit(new BigDecimal("1000.00"))
            .interestRate(new BigDecimal("0.15"))
            .paymentDueDay(15)
            .status(CreditStatus.ACTIVE)
            .build();
    when(createCreditUseCase.create(any(Credit.class))).thenReturn(Single.just(credit));

    // Act
    TestObserver<CreditResponse> observer = controller.createCredit(request).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValueCount(1);
    observer.assertValue(response -> "LN-1234567890".equals(response.creditNumber()));
    observer.assertComplete();
    ArgumentCaptor<Credit> captor = ArgumentCaptor.forClass(Credit.class);
    verify(createCreditUseCase).create(captor.capture());
    assertThat(captor.getValue().getCustomerId()).isEqualTo("cust-1");
    assertThat(captor.getValue().getCreditLimit()).isEqualByComparingTo("1000.00");
  }

  @Test
  @DisplayName("Should return masked credit numbers when retrieving all credits")
  void getAllCreditsReturnsMaskedCreditNumbers() {
    // Arrange
    when(retrieveCreditUseCase.findAll()).thenReturn(Flowable.just(credit));

    // Act
    TestSubscriber<CreditResponse> observer = controller.getAllCredits().test();

    // Assert
    observer.assertNoErrors();
    observer.assertValueCount(1);
    observer.assertValue(response -> "*********7890".equals(response.creditNumber()));
    observer.assertComplete();
    verify(retrieveCreditUseCase).findAll();
  }

  @Test
  @DisplayName("Should return a masked credit number when retrieving a credit by id")
  void getCreditByIdReturnsMaskedCreditNumber() {
    // Arrange
    when(retrieveCreditUseCase.findById("credit-1")).thenReturn(Single.just(credit));

    // Act
    TestObserver<CreditResponse> observer = controller.getCreditById("credit-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(response -> "credit-1".equals(response.id()));
    observer.assertValue(response -> "*********7890".equals(response.creditNumber()));
    observer.assertComplete();
    verify(retrieveCreditUseCase).findById("credit-1");
  }

  @Test
  @DisplayName("Should return a masked credit number when retrieving a credit by number")
  void getCreditByNumberReturnsMaskedCreditNumber() {
    // Arrange
    when(retrieveCreditUseCase.findByCreditNumber("LN-1234567890"))
            .thenReturn(Single.just(credit));

    // Act
    TestObserver<CreditResponse> observer =
            controller.getCreditByNumber("LN-1234567890").test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(response -> "*********7890".equals(response.creditNumber()));
    observer.assertComplete();
    verify(retrieveCreditUseCase).findByCreditNumber("LN-1234567890");
  }

  @Test
  @DisplayName("Should return masked credit numbers for a customer")
  void getCreditsByCustomerIdReturnsMaskedCreditNumbers() {
    // Arrange
    when(retrieveCreditUseCase.findByCustomerId("cust-1")).thenReturn(Flowable.just(credit));

    // Act
    TestSubscriber<CreditResponse> observer =
            controller.getCreditsByCustomerId("cust-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertValueCount(1);
    observer.assertValue(response -> "cust-1".equals(response.customerId()));
    observer.assertComplete();
    verify(retrieveCreditUseCase).findByCustomerId("cust-1");
  }

  @Test
  @DisplayName("Should return the credit balance with a masked number")
  void getCreditBalanceReturnsBalanceWithMaskedNumber() {
    // Arrange
    when(retrieveCreditUseCase.findById("credit-1")).thenReturn(Single.just(credit));

    // Act
    TestObserver<CreditBalanceResponse> observer =
            controller.getCreditBalance("credit-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertValueCount(1);
    observer.assertValue(response -> "credit-1".equals(response.creditId()));
    observer.assertValue(response -> "*********7890".equals(response.creditNumber()));
    observer.assertValue(response -> response.queriedAt() != null);
    observer.assertComplete();
  }

  @Test
  @DisplayName("Should delegate the balance update and mask the credit number")
  void updateCreditBalanceDelegatesOperationAndReturnsMaskedNumber() {
    // Arrange
    when(updateCreditBalanceUseCase.updateBalance("credit-1", CreditOperationType.PAYMENT,
            new BigDecimal("50.00"))).thenReturn(Single.just(credit));
    CreditBalanceUpdateRequest request = CreditBalanceUpdateRequest.builder()
            .operationType(CreditOperationType.PAYMENT)
            .amount(new BigDecimal("50.00"))
            .build();

    // Act
    TestObserver<CreditResponse> observer =
            controller.updateCreditBalance("credit-1", request).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(response -> "*********7890".equals(response.creditNumber()));
    observer.assertComplete();
    verify(updateCreditBalanceUseCase)
            .updateBalance("credit-1", CreditOperationType.PAYMENT, new BigDecimal("50.00"));
  }

  @Test
  @DisplayName("Should delegate the update and mask the credit number")
  void updateCreditDelegatesUpdateAndReturnsMaskedNumber() {
    // Arrange
    CreditRequest request = CreditRequest.builder()
            .currency("USD")
            .interestRate(new BigDecimal("0.20"))
            .paymentDueDay(10)
            .status(CreditStatus.BLOCKED)
            .build();
    when(updateCreditUseCase.updateCredit(eq("credit-1"), any(Credit.class)))
            .thenReturn(Single.just(credit));

    // Act
    TestObserver<CreditResponse> observer = controller.updateCredit("credit-1", request).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(response -> "*********7890".equals(response.creditNumber()));
    observer.assertComplete();
    ArgumentCaptor<Credit> captor = ArgumentCaptor.forClass(Credit.class);
    verify(updateCreditUseCase).updateCredit(eq("credit-1"), captor.capture());
    assertThat(captor.getValue().getCurrency()).isEqualTo("USD");
    assertThat(captor.getValue().getPaymentDueDay()).isEqualTo(10);
  }

  @Test
  @DisplayName("Should complete deletion when the use case completes")
  void deleteCreditCompletesWhenUseCaseCompletes() {
    // Arrange
    when(deleteCreditUseCase.delete("credit-1")).thenReturn(Completable.complete());

    // Act
    TestObserver<Void> observer = controller.deleteCredit("credit-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
    verify(deleteCreditUseCase).delete("credit-1");
  }
}
