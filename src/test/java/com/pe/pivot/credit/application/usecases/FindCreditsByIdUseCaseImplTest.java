package com.pe.pivot.credit.application.usecases;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.credit.domain.exception.CreditNotFoundException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.port.out.CreditRepositoryPort;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link FindCreditsByIdUseCaseImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Should cover credit lookup by id")
class FindCreditsByIdUseCaseImplTest {

  @Mock
  private CreditRepositoryPort creditRepository;

  private FindCreditsByIdUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    useCase = new FindCreditsByIdUseCaseImpl(creditRepository);
  }

  @Test
  @DisplayName("Should return the credit when it exists")
  void returnsCreditWhenItExists() {
    // Arrange
    Credit credit = Credit.builder().id("credit-1").build();
    when(creditRepository.findById("credit-1")).thenReturn(Maybe.just(credit));

    // Act
    TestObserver<Credit> observer = useCase.findById("credit-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(credit);
    observer.assertComplete();
    verify(creditRepository).findById("credit-1");
  }

  @Test
  @DisplayName("Should fail when the credit does not exist")
  void failsWhenCreditDoesNotExist() {
    // Arrange
    when(creditRepository.findById("credit-404")).thenReturn(Maybe.empty());

    // Act
    TestObserver<Credit> observer = useCase.findById("credit-404").test();

    // Assert
    observer.assertNoValues();
    observer.assertError(CreditNotFoundException.class);
    observer.assertError(error -> error.getMessage().contains("credit-404"));
  }

  @Test
  @DisplayName("Should propagate repository errors")
  void propagatesRepositoryError() {
    // Arrange
    when(creditRepository.findById("credit-1"))
            .thenReturn(Maybe.error(new IllegalStateException("Database unavailable")));

    // Act
    TestObserver<Credit> observer = useCase.findById("credit-1").test();

    // Assert
    observer.assertNoValues();
    observer.assertError(IllegalStateException.class);
    observer.assertNotComplete();
  }
}
