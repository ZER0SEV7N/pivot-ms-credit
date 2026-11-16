package com.pe.pivot.credit.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.exception.CreditNotFoundException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.model.CreditType;
import com.pe.pivot.credit.domain.port.in.FindCreditByIdUseCase;
import com.pe.pivot.credit.domain.port.out.CreditRepositoryPort;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link DeleteCreditUseCaseImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteCreditUseCaseImpl credit closure")
class DeleteCreditUseCaseImplTest {

  @Mock
  private CreditRepositoryPort creditRepository;

  @Mock
  private FindCreditByIdUseCase findCreditByIdUseCase;

  private DeleteCreditUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    useCase = new DeleteCreditUseCaseImpl(creditRepository, findCreditByIdUseCase);
  }

  @Test
  @DisplayName("Closes the credit when there is no outstanding debt")
  void closesCreditWhenThereIsNoOutstandingDebt() {
    // Arrange
    Credit credit = Credit.builder()
            .id("credit-1")
            .creditType(CreditType.PERSONAL_LOAN)
            .usedAmount(BigDecimal.ZERO)
            .status(CreditStatus.ACTIVE)
            .build();
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(credit));
    when(creditRepository.save(credit)).thenReturn(Single.just(credit));

    // Act
    TestObserver<Void> observer = useCase.delete("credit-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
    assertThat(credit.getStatus()).isEqualTo(CreditStatus.CLOSED);
    assertThat(credit.getUpdatedAt()).isNotNull();
    verify(creditRepository).save(credit);
  }

  @Test
  @DisplayName("Fails when the credit is already closed")
  void failsWhenCreditIsAlreadyClosed() {
    // Arrange
    Credit credit = Credit.builder()
            .id("credit-1")
            .usedAmount(BigDecimal.ZERO)
            .status(CreditStatus.CLOSED)
            .build();
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(credit));

    // Act
    TestObserver<Void> observer = useCase.delete("credit-1").test();

    // Assert
    observer.assertError(BusinessRuleException.class);
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Fails when the credit has outstanding debt")
  void failsWhenCreditHasOutstandingDebt() {
    // Arrange
    Credit credit = Credit.builder()
            .id("credit-1")
            .usedAmount(new BigDecimal("150.00"))
            .status(CreditStatus.ACTIVE)
            .build();
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(credit));

    // Act
    TestObserver<Void> observer = useCase.delete("credit-1").test();

    // Assert
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("outstanding debt"));
    verify(creditRepository, never()).save(any());
    assertThat(credit.getStatus()).isEqualTo(CreditStatus.ACTIVE);
  }

  @Test
  @DisplayName("Propagates the credit not found error")
  void propagatesCreditNotFoundError() {
    // Arrange
    when(findCreditByIdUseCase.findById("credit-1"))
            .thenReturn(Single.error(new CreditNotFoundException("Credit not found")));

    // Act
    TestObserver<Void> observer = useCase.delete("credit-1").test();

    // Assert
    observer.assertError(CreditNotFoundException.class);
    verifyNoInteractions(creditRepository);
  }

  @Test
  @DisplayName("Closes the credit when the used amount is null")
  void closesCreditWhenUsedAmountIsNull() {
    // Arrange
    Credit credit = Credit.builder()
            .id("credit-1")
            .usedAmount(null)
            .status(CreditStatus.PAID_OFF)
            .build();
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(credit));
    when(creditRepository.save(credit)).thenReturn(Single.just(credit));

    // Act
    TestObserver<Void> observer = useCase.delete("credit-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
    assertThat(credit.getStatus()).isEqualTo(CreditStatus.CLOSED);
  }
}
