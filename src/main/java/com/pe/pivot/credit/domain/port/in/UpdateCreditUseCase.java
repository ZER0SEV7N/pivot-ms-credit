package com.pe.pivot.credit.domain.port.in;

import com.pe.pivot.credit.domain.model.Credit;
import io.reactivex.rxjava3.core.Single;

/**
 * Input port for updating the administrative terms of a credit product.
 */
public interface UpdateCreditUseCase {
  Single<Credit> updateCredit(String id, Credit credit);
}
