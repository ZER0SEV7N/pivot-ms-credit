package com.pe.pivot.credit.domain.port.in;

import com.pe.pivot.credit.domain.model.Credit;
import io.reactivex.rxjava3.core.Single;

/**
 * Input port for creating a new credit product.
 */
public interface CreateCreditUseCase {
  Single<Credit> create(Credit credit);
}
