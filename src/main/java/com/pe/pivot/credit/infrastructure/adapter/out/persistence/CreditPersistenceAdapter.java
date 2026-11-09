package com.pe.pivot.credit.infrastructure.adapter.out.persistence;

import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.port.out.CreditRepositoryPort;
import com.pe.pivot.credit.infrastructure.adapter.out.persistence.mapper.CreditPersistenceMapper;
import com.pe.pivot.credit.infrastructure.adapter.out.persistence.repository.ReactiveCreditMongoRepository;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava3Adapter;

/**
 * Persistence Adapter bridging Spring Data Reactive MongoDB (Reactor) to RxJava 3 Domain Ports.
 */
@Component
@RequiredArgsConstructor
public class CreditPersistenceAdapter implements CreditRepositoryPort {

  private final ReactiveCreditMongoRepository repository;
  private final CreditPersistenceMapper mapper;

  /**
   * Saves a Credit entity to the database.
   * The Credit object is first mapped to its corresponding persistence document, saved, and then mapped back to the domain model.
   *
   * @param credit - the Credit entity to be saved
   * @return - a Single emitting the saved Credit entity
   */
  @Override
  public Single<Credit> save(Credit credit) {
    return RxJava3Adapter.monoToSingle(
            repository.save(mapper.toDocument(credit))
                    .map(mapper::toDomain)
    );
  }

  /**
   * Retrieves all Credit entities from the database as a Flowable stream.
   *
   * @return - a Flowable emitting all Credit entities
   */
  @Override
  public Flowable<Credit> findAll() {
    return RxJava3Adapter.fluxToFlowable(
            repository.findAll()
                    .map(mapper::toDomain)
    );
  }

  /**
   * Finds a Credit entity by its unique ID. If the entity is not found, an empty Maybe is returned.
   *
   * @param id - the unique identifier of the Credit entity to find
   * @return - a Maybe emitting the found Credit entity or empty if not found
   */
  @Override
  public Maybe<Credit> findById(String id) {
    return RxJava3Adapter.monoToMaybe(
            repository.findById(id)
                    .map(mapper::toDomain)
    );
  }

  /**
   * Finds a Credit entity by its hashed credit number. If the entity is not found, an empty Maybe is returned.
   *
   * @param creditNumberHash - the SHA-256 hash of the credit number to search for
   * @return - a Maybe emitting the found Credit entity or empty if not found
   */
  @Override
  public Maybe<Credit> findByCreditNumberHash(String creditNumberHash) {
    return RxJava3Adapter.monoToMaybe(
            repository.findByCreditNumberHash(creditNumberHash)
                    .map(mapper::toDomain)
    );
  }

  /**
   * Finds all Credit entities associated with a specific customer ID. Returns a Flowable stream of Credit entities.
   *
   * @param customerId - the unique identifier of the customer whose Credit entities are to be retrieved
   * @return - a Flowable emitting all Credit entities associated with the given customer ID
   */
  @Override
  public Flowable<Credit> findByCustomerId(String customerId) {
    return RxJava3Adapter.fluxToFlowable(
            repository.findByCustomerId(customerId)
                    .map(mapper::toDomain)
    );
  }

  /**
   * Checks if a Credit entity exists with the given hashed credit number. Returns a Single emitting true if it exists, false otherwise.
   *
   * @param creditNumberHash - the SHA-256 hash of the credit number to check for existence
   * @return - a Single emitting true if a Credit entity with the given hash exists, false otherwise
   */
  @Override
  public Single<Boolean> existsByCreditNumberHash(String creditNumberHash) {
    return RxJava3Adapter.monoToSingle(
            repository.existsByCreditNumberHash(creditNumberHash)
    );
  }

  @Override
  public Completable deleteById(String id) {
    return RxJava3Adapter.monoToCompletable(
            repository.deleteById(id)
    );
  }
}
