package com.pe.pivot.credit.domain.port.in;

import io.reactivex.rxjava3.core.Completable;

/**
 * Input port for logically closing (soft deleting) a credit product.
 */
public interface DeleteCreditUseCase {
  Completable delete(String id);
}
