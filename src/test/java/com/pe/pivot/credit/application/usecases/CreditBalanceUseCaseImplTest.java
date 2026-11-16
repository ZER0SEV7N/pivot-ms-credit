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
@DisplayName("CreditBalanceUseCaseImpl balance updates")
class CreditBalanceUseCaseImplTest {

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
            .id("credit-1")
            .status(CreditStatus.ACTIVE)
            .usedAmount(new BigDecimal("100.00"))
            .availableAmount(new BigDecimal("400.00"))
            .build();
  }

  @Test
  @DisplayName("Applies the operation and persists the updated credit")
  void appliesOperationAndPersistsUpdatedCredit() {
    // Arrange
    Credit updatedCredit = Credit.builder()
            .id("credit-1")
            .status(CreditStatus.ACTIVE)
            .usedAmount(new BigDecimal("150.00"))
            .availableAmount(new BigDecimal("350.00"))
            .build();
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(credit));
    when(operationStrategy.supports(CreditOperationType.PAYMENT)).thenReturn(true);
    when(operationStrategy.applyOperation(credit, new BigDecimal("50.00")))
            .thenReturn(Single.just(updatedCredit));
    when(creditRepository.save(updatedCredit)).thenReturn(Single.just(updatedCredit));

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance("credit-1", CreditOperationType.PAYMENT, new BigDecimal("50.00"))
                    .test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(updatedCredit);
    observer.assertComplete();
    verify(creditRepository).save(updatedCredit);
  }

  @Test
  @DisplayName("Rejects the operation when the credit is not active")
  void rejectsOperationWhenCreditIsNotActive() {
    // Arrange
    credit.setStatus(CreditStatus.CLOSED);
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(credit));

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance("credit-1", CreditOperationType.PAYMENT, BigDecimal.TEN).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    verifyNoInteractions(operationStrategy);
    verifyNoInteractions(creditRepository);
  }

  @Test
  @DisplayName("Rejects a null amount")
  void rejectsNullAmount() {
    // Arrange
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(credit));

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance("credit-1", CreditOperationType.CONSUMPTION, null).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Rejects a zero amount")
  void rejectsZeroAmount() {
    // Arrange
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(credit));

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance("credit-1", CreditOperationType.CONSUMPTION, BigDecimal.ZERO)
                    .test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Rejects a negative amount")
  void rejectsNegativeAmount() {
    // Arrange
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(credit));

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance("credit-1", CreditOperationType.CONSUMPTION,
                    new BigDecimal("-10.00")).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Rejects an unsupported operation type")
  void rejectsUnsupportedOperationType() {
    // Arrange
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(credit));
    when(operationStrategy.supports(CreditOperationType.CONSUMPTION)).thenReturn(false);

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance("credit-1", CreditOperationType.CONSUMPTION, BigDecimal.TEN)
                    .test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Propagates the credit not found error")
  void propagatesCreditNotFoundError() {
    // Arrange
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(
            Single.error(new CreditNotFoundException("Credit not found")));

    // Act
    TestObserver<Credit> observer =
            useCase.updateBalance("credit-1", CreditOperationType.PAYMENT, BigDecimal.TEN).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(CreditNotFoundException.class);
    verify(creditRepository, never()).save(any());
  }
}
