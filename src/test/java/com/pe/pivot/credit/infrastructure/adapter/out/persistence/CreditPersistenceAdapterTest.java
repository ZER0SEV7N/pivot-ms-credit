package com.pe.pivot.credit.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.infrastructure.adapter.out.persistence.entity.CreditDocument;
import com.pe.pivot.credit.infrastructure.adapter.out.persistence.mapper.CreditPersistenceMapper;
import com.pe.pivot.credit.infrastructure.adapter.out.persistence.repository.ReactiveCreditMongoRepository;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link CreditPersistenceAdapter}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Should persist credits reactively")
class CreditPersistenceAdapterTest {

  @Mock
  private ReactiveCreditMongoRepository repository;

  @Mock
  private CreditPersistenceMapper mapper;

  private CreditPersistenceAdapter adapter;

  private Credit credit;

  private CreditDocument document;

  @BeforeEach
  void setUp() {
    adapter = new CreditPersistenceAdapter(repository, mapper);
    credit = Credit.builder().id("credit-1").build();
    document = CreditDocument.builder().id("credit-1").build();
  }

  @Test
  @DisplayName("Should save the document and map it back to the domain")
  void savesDocumentAndMapsItBackToDomain() {
    // Arrange
    when(mapper.toDocument(credit)).thenReturn(document);
    when(repository.save(document)).thenReturn(Mono.just(document));
    when(mapper.toDomain(document)).thenReturn(credit);

    // Act
    TestObserver<Credit> observer = adapter.save(credit).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(credit);
    observer.assertComplete();
    verify(repository).save(document);
  }

  @Test
  @DisplayName("Should propagate the error when saving fails")
  void propagatesErrorWhenSavingFails() {
    // Arrange
    when(mapper.toDocument(credit)).thenReturn(document);
    when(repository.save(document))
            .thenReturn(Mono.error(new IllegalStateException("Database unavailable")));

    // Act
    TestObserver<Credit> observer = adapter.save(credit).test();

    // Assert
    observer.assertNoValues();
    observer.assertError(IllegalStateException.class);
  }

  @Test
  @DisplayName("Should return all credits as a Flowable")
  void returnsAllCreditsAsFlowable() {
    // Arrange
    CreditDocument secondDocument = CreditDocument.builder().id("credit-2").build();
    Credit secondCredit = Credit.builder().id("credit-2").build();
    when(repository.findAll()).thenReturn(Flux.just(document, secondDocument));
    when(mapper.toDomain(document)).thenReturn(credit);
    when(mapper.toDomain(secondDocument)).thenReturn(secondCredit);

    // Act
    TestSubscriber<Credit> observer = adapter.findAll().test();

    // Assert
    observer.assertNoErrors();
    observer.assertValues(credit, secondCredit);
    observer.assertComplete();
  }

  @Test
  @DisplayName("Should find a credit by id")
  void findsCreditById() {
    // Arrange
    when(repository.findById("credit-1")).thenReturn(Mono.just(document));
    when(mapper.toDomain(document)).thenReturn(credit);

    // Act
    TestObserver<Credit> observer = adapter.findById("credit-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(credit);
    observer.assertComplete();
  }

  @Test
  @DisplayName("Should return empty when the credit id does not exist")
  void returnsEmptyWhenCreditIdDoesNotExist() {
    // Arrange
    when(repository.findById("credit-404")).thenReturn(Mono.empty());

    // Act
    TestObserver<Credit> observer = adapter.findById("credit-404").test();

    // Assert
    observer.assertNoErrors();
    observer.assertNoValues();
    observer.assertComplete();
  }

  @Test
  @DisplayName("Should find a credit by its credit number hash")
  void findsCreditByCreditNumberHash() {
    // Arrange
    when(repository.findByCreditNumberHash("hashed"))
            .thenReturn(Mono.just(document));
    when(mapper.toDomain(document)).thenReturn(credit);

    // Act
    TestObserver<Credit> observer = adapter.findByCreditNumberHash("hashed").test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(credit);
    observer.assertComplete();
  }

  @Test
  @DisplayName("Should find the credits of a customer")
  void findsCreditsByCustomerId() {
    // Arrange
    when(repository.findByCustomerId("cust-1")).thenReturn(Flux.just(document));
    when(mapper.toDomain(document)).thenReturn(credit);

    // Act
    TestSubscriber<Credit> observer = adapter.findByCustomerId("cust-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertValues(credit);
    observer.assertComplete();
  }

  @Test
  @DisplayName("Should report existence by credit number hash")
  void reportsExistenceByCreditNumberHash() {
    // Arrange
    when(repository.existsByCreditNumberHash("hashed")).thenReturn(Mono.just(true));

    // Act
    TestObserver<Boolean> observer = adapter.existsByCreditNumberHash("hashed").test();

    // Assert
    observer.assertNoErrors();
    observer.assertValue(true);
    observer.assertComplete();
  }

  @Test
  @DisplayName("Should delete a credit by id")
  void deletesCreditById() {
    // Arrange
    when(repository.deleteById("credit-1")).thenReturn(Mono.empty());

    // Act
    TestObserver<Void> observer = adapter.deleteById("credit-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertComplete();
    verify(repository).deleteById("credit-1");
  }

  @Test
  @DisplayName("Should map the saved document back to the domain")
  void mapsSavedDocumentBackToDomain() {
    // Arrange
    Credit saved = Credit.builder().id("credit-9").build();
    when(mapper.toDocument(credit)).thenReturn(document);
    when(repository.save(document)).thenReturn(Mono.just(document));
    when(mapper.toDomain(document)).thenReturn(saved);

    // Act
    TestObserver<Credit> observer = adapter.save(credit).test();

    // Assert
    observer.assertValue(saved);
    assertThat(observer.values()).containsExactly(saved);
  }
}
