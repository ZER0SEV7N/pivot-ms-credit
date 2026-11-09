package com.pe.pivot.credit.infrastructure.adapter.out.persistence.repository;

import com.pe.pivot.credit.infrastructure.adapter.out.persistence.entity.CreditDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data Reactive MongoDB repository using strictly derived query methods (no @Query).
 */
@Repository
public interface ReactiveCreditMongoRepository extends ReactiveMongoRepository<CreditDocument, String> {
  Mono<CreditDocument> findByCreditNumberHash(String creditNumberHash);

  Flux<CreditDocument> findByCustomerId(String customerId);

  Mono<Boolean> existsByCreditNumberHash(String creditNumberHash);
}
