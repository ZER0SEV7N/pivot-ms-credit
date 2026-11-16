package com.pe.pivot.credit.domain.port.in;

import com.pe.pivot.credit.domain.model.Credit;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Input port for querying credit products.
 */
public interface RetrieveCreditUseCase {
  Flowable<Credit> findAll();

  Single<Credit> findById(String id);

  Single<Credit> findByCreditNumber(String creditNumber);

  Flowable<Credit> findByCustomerId(String customerId);
}
