package com.pe.pivot.credit.domain.port.out;

import com.pe.pivot.credit.domain.model.Credit;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public interface CreditRepositoryPort {
  Single<Credit> save(Credit credit);
  Flowable<Credit> findAll();
  Maybe<Credit> findById(String id);
  Maybe<Credit> findByCreditNumberHash(String creditNumberHash);
  Flowable<Credit> findByCustomerId(String customerId);
  Single<Boolean> existsByCreditNumberHash(String creditNumberHash);
  Completable deleteById(String id);
}