package com.pe.pivot.credit.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
 * Unit tests for {@link UpdateCreditUseCaseImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Should cover credit administrative updates")
class UpdateCreditUseCaseImplTest {

  @Mock
  private CreditRepositoryPort creditRepository;

  @Mock
  private FindCreditByIdUseCase findCreditByIdUseCase;

  private UpdateCreditUseCaseImpl useCase;

  private Credit existing;

  @BeforeEach
  void setUp() {
    useCase = new UpdateCreditUseCaseImpl(creditRepository, findCreditByIdUseCase);
    existing = Credit.builder()
            .id("credit-1")
            .creditType(CreditType.PERSONAL_LOAN)
            .currency("PEN")
            .interestRate(new BigDecimal("0.10"))
            .paymentDueDay(5)
            .status(CreditStatus.ACTIVE)
            .build();
  }

  @Test
  @DisplayName("Should update all provided administrative fields")
  void updatesAllProvidedAdministrativeFields() {
    // Arrange
    Credit incoming = Credit.builder()
            .currency("USD")
            .interestRate(new BigDecimal("0.20"))
            .paymentDueDay(10)
            .status(CreditStatus.BLOCKED)
            .build();
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(existing));
    when(creditRepository.save(existing)).thenReturn(Single.just(existing));

    // Act
    TestObserver<Credit> observer = useCase.updateCredit("credit-1", incoming).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(existing);
    observer.assertComplete();
    assertThat(existing.getCurrency()).isEqualTo("USD");
    assertThat(existing.getInterestRate()).isEqualByComparingTo("0.20");
    assertThat(existing.getPaymentDueDay()).isEqualTo(10);
    assertThat(existing.getStatus()).isEqualTo(CreditStatus.BLOCKED);
    assertThat(existing.getUpdatedAt()).isNotNull();
    verify(creditRepository).save(existing);
  }

  @Test
  @DisplayName("Should keep existing values when incoming fields are null")
  void keepsExistingValuesWhenIncomingFieldsAreNull() {
    // Arrange
    Credit incoming = Credit.builder().build();
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(existing));
    when(creditRepository.save(existing)).thenReturn(Single.just(existing));

    // Act
    TestObserver<Credit> observer = useCase.updateCredit("credit-1", incoming).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(existing);
    observer.assertComplete();
    assertThat(existing.getCurrency()).isEqualTo("PEN");
    assertThat(existing.getInterestRate()).isEqualByComparingTo("0.10");
    assertThat(existing.getPaymentDueDay()).isEqualTo(5);
    assertThat(existing.getStatus()).isEqualTo(CreditStatus.ACTIVE);
  }

  @Test
  @DisplayName("Should reject the update when the credit is closed")
  void rejectsUpdateWhenCreditIsClosed() {
    // Arrange
    existing.setStatus(CreditStatus.CLOSED);
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(existing));

    // Act
    TestObserver<Credit> observer = useCase.updateCredit("credit-1", Credit.builder().build())
            .test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should reject the update when the credit is paid off")
  void rejectsUpdateWhenCreditIsPaidOff() {
    // Arrange
    existing.setStatus(CreditStatus.PAID_OFF);
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(existing));

    // Act
    TestObserver<Credit> observer = useCase.updateCredit("credit-1", Credit.builder().build())
            .test();

    // Assert
    observer.assertNoValues();
    observer.assertError(BusinessRuleException.class);
    verify(creditRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should propagate the credit not found error")
  void propagatesCreditNotFoundError() {
    // Arrange
    when(findCreditByIdUseCase.findById("credit-404"))
            .thenReturn(Single.error(new CreditNotFoundException("Credit not found")));

    // Act
    TestObserver<Credit> observer = useCase.updateCredit("credit-404", Credit.builder().build())
            .test();

    // Assert
    observer.assertNoValues();
    observer.assertError(CreditNotFoundException.class);
    verify(creditRepository, never()).save(any());
  }
}
