package com.pe.pivot.credit.application.usecases;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.credit.domain.exception.CreditNotFoundException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.port.in.FindCreditByIdUseCase;
import com.pe.pivot.credit.domain.port.out.CreditRepositoryPort;
import com.pe.pivot.credit.infrastructure.util.CryptoUtil;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link RetrieveCreditUseCaseImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RetrieveCreditUseCaseImpl read operations")
class RetrieveCreditUseCaseImplTest {

  @Mock
  private CreditRepositoryPort creditRepository;

  @Mock
  private FindCreditByIdUseCase findCreditByIdUseCase;

  @Mock
  private CryptoUtil cryptoUtil;

  private RetrieveCreditUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    useCase = new RetrieveCreditUseCaseImpl(creditRepository, findCreditByIdUseCase, cryptoUtil);
  }

  @Test
  @DisplayName("Returns all credits")
  void returnsAllCredits() {
    // Arrange
    Credit first = Credit.builder().id("credit-1").build();
    Credit second = Credit.builder().id("credit-2").build();
    when(creditRepository.findAll()).thenReturn(Flowable.just(first, second));

    // Act
    TestSubscriber<Credit> observer = useCase.findAll().test();

    // Assert
    observer.assertNoErrors();
    observer.assertValues(first, second);
    observer.assertComplete();
    verify(creditRepository).findAll();
  }

  @Test
  @DisplayName("Delegates find by id to the find credit use case")
  void delegatesFindByIdToFindCreditUseCase() {
    // Arrange
    Credit credit = Credit.builder().id("credit-1").build();
    when(findCreditByIdUseCase.findById("credit-1")).thenReturn(Single.just(credit));

    // Act
    TestObserver<Credit> observer = useCase.findById("credit-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(credit);
    observer.assertComplete();
    verify(findCreditByIdUseCase).findById("credit-1");
  }

  @Test
  @DisplayName("Finds a credit by its hashed credit number")
  void findsCreditByHashedCreditNumber() {
    // Arrange
    Credit credit = Credit.builder().id("credit-1").creditNumber("LN-1234567890").build();
    when(cryptoUtil.hashSha256("LN-1234567890")).thenReturn("hashed-number");
    when(creditRepository.findByCreditNumberHash("hashed-number")).thenReturn(Maybe.just(credit));

    // Act
    TestObserver<Credit> observer = useCase.findByCreditNumber("LN-1234567890").test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(credit);
    observer.assertComplete();
    verify(cryptoUtil).hashSha256("LN-1234567890");
  }

  @Test
  @DisplayName("Fails when the credit number does not exist")
  void failsWhenCreditNumberDoesNotExist() {
    // Arrange
    when(cryptoUtil.hashSha256("LN-1234567890")).thenReturn("hashed-number");
    when(creditRepository.findByCreditNumberHash("hashed-number")).thenReturn(Maybe.empty());

    // Act
    TestObserver<Credit> observer = useCase.findByCreditNumber("LN-1234567890").test();

    // Assert
    observer.assertNoValues();
    observer.assertError(CreditNotFoundException.class);
    observer.assertError(error -> error.getMessage().contains("*********7890"));
  }

  @Test
  @DisplayName("Returns the credits of a customer")
  void returnsCreditsForCustomer() {
    // Arrange
    Credit credit = Credit.builder().id("credit-1").customerId("cust-1").build();
    when(creditRepository.findByCustomerId("cust-1")).thenReturn(Flowable.just(credit));

    // Act
    TestSubscriber<Credit> observer = useCase.findByCustomerId("cust-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(credit);
    observer.assertComplete();
    verify(creditRepository).findByCustomerId("cust-1");
  }

  @Test
  @DisplayName("Propagates repository errors when searching by credit number")
  void propagatesRepositoryErrorWhenSearchingByCreditNumber() {
    // Arrange
    when(cryptoUtil.hashSha256("LN-1234567890")).thenReturn("hashed-number");
    when(creditRepository.findByCreditNumberHash("hashed-number"))
            .thenReturn(Maybe.error(new IllegalStateException("Database unavailable")));

    // Act
    TestObserver<Credit> observer = useCase.findByCreditNumber("LN-1234567890").test();

    // Assert
    observer.assertNoValues();
    observer.assertError(IllegalStateException.class);
  }
}
