package com.pe.pivot.credit.application.usecases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.exception.CreditNotFoundException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditOperationType;
import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.port.in.FindCreditByIdUseCase;
import com.pe.pivot.credit.domain.port.out.CreditRepositoryPort;
import com.pe.pivot.credit.domain.strategy.operation.CreditOperationStrategy;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link CreditBalanceUseCaseImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Should be able to update credit balances")
class CreditBalanceUseCaseImplTest {

  private static final String CREDIT_ID = "credit-1";

  @Mock
  private CreditRepositoryPort creditRepository;

  @Mock
  private FindCreditByIdUseCase findCreditByIdUseCase;

  @Mock
  private CreditOperationStrategy operationStrategy;

  private CreditBalanceUseCaseImpl useCase;

  private Credit credit;

  @BeforeEach
  void setUp() {
    useCase = new CreditBalanceUseCaseImpl(creditRepository, findCreditByIdUseCase,
            List.of(operationStrategy));
    credit = Credit.builder()
            .id(CREDIT_ID)
            .status(CreditStatus.ACTIVE)
            .usedAmount(new BigDecimal("100.00"))
            .availableAmount(new BigDecimal("400.00"))
            .build();
  }

  @Test
  @DisplayName("Should be applied and persisted when the strategy supports the operation")
  void shouldBeAppliedAndPersistedWhenStrategySupportsTheOperation() {
    // (happy path)
    // Arrange
    Credit updatedCredit = Credit.builder()
            .id(CREDIT_ID)
            .status(CreditStatus.ACTIVE)
            .usedAmount(new BigDecimal("150.00"))
            .availableAmount(new BigDecimal("350.00"))
            .build();
    when(findCreditByIdUseCase.findById(CREDIT_ID)).thenReturn(Single.just(credit));
    when(operationStrategy.supports(CreditOperationType.PAYMENT)).thenReturn(true);
    when(operationStrategy.applyOperation(credit, new BigDecimal("50.00")))
            .thenReturn(Single.just(updatedCredit));
    when(creditRepository.save(updatedCredit)).thenReturn(Single.just(updatedCredit));

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance(CREDIT_ID, CreditOperationType.PAYMENT, new BigDecimal("50.00"))
                    .test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
    observer.assertValueCount(1);
    observer.assertValue(updatedCredit);
    verify(operationStrategy).applyOperation(credit, new BigDecimal("50.00"));
    verify(creditRepository).save(updatedCredit);
  }

  @Test
  @DisplayName("Should be rejected when the amount is null")
  void shouldBeRejectedWhenAmountIsNull() {
    // (error: null amount)
    // Arrange
    when(findCreditByIdUseCase.findById(CREDIT_ID)).thenReturn(Single.just(credit));

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance(CREDIT_ID, CreditOperationType.CONSUMPTION, null).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("greater than zero"));
    verifyNoInteractions(operationStrategy);
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be rejected when the amount is zero")
  void shouldBeRejectedWhenAmountIsZero() {
    // (error: zero amount)
    // Arrange
    when(findCreditByIdUseCase.findById(CREDIT_ID)).thenReturn(Single.just(credit));

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance(CREDIT_ID, CreditOperationType.CONSUMPTION, BigDecimal.ZERO)
                    .test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    verifyNoInteractions(operationStrategy);
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be rejected when the amount is negative")
  void shouldBeRejectedWhenAmountIsNegative() {
    // (error: negative amount)
    // Arrange
    when(findCreditByIdUseCase.findById(CREDIT_ID)).thenReturn(Single.just(credit));

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance(CREDIT_ID, CreditOperationType.CONSUMPTION,
                    new BigDecimal("-10.00")).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be rejected when no strategy supports the operation type")
  void shouldBeRejectedWhenNoStrategySupportsTheOperationType() {
    // (error: unsupported operation type)
    // Arrange
    when(findCreditByIdUseCase.findById(CREDIT_ID)).thenReturn(Single.just(credit));
    when(operationStrategy.supports(CreditOperationType.CONSUMPTION)).thenReturn(false);

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance(CREDIT_ID, CreditOperationType.CONSUMPTION, BigDecimal.TEN)
                    .test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("Unsupported credit operation"));
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be propagated when the credit does not exist")
  void shouldBePropagatedWhenCreditDoesNotExist() {
    // (error: credit not found)
    // Arrange
    when(findCreditByIdUseCase.findById(CREDIT_ID)).thenReturn(
            Single.error(new CreditNotFoundException("Credit not found")));

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance(CREDIT_ID, CreditOperationType.PAYMENT, BigDecimal.TEN).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(CreditNotFoundException.class);
    verifyNoInteractions(operationStrategy);
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be propagated when the strategy rejects the operation")
  void shouldBePropagatedWhenStrategyRejectsTheOperation() {
    // (error: business rule rejected by strategy, e.g. non-active credit)
    // Arrange
    when(findCreditByIdUseCase.findById(CREDIT_ID)).thenReturn(Single.just(credit));
    when(operationStrategy.supports(CreditOperationType.PAYMENT)).thenReturn(true);
    when(operationStrategy.applyOperation(credit, BigDecimal.TEN)).thenReturn(
            Single.error(new BusinessRuleException("Credit product cannot receive payments")));

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance(CREDIT_ID, CreditOperationType.PAYMENT, BigDecimal.TEN).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be propagated when the repository fails while saving")
  void shouldBePropagatedWhenRepositoryFailsWhileSaving() {
    // (error: persistence failure)
    // Arrange
    when(findCreditByIdUseCase.findById(CREDIT_ID)).thenReturn(Single.just(credit));
    when(operationStrategy.supports(CreditOperationType.PAYMENT)).thenReturn(true);
    when(operationStrategy.applyOperation(credit, BigDecimal.TEN)).thenReturn(Single.just(credit));
    when(creditRepository.save(credit)).thenReturn(
            Single.error(new IllegalStateException("database unavailable")));

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance(CREDIT_ID, CreditOperationType.PAYMENT, BigDecimal.TEN).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(IllegalStateException.class);
  }
}
