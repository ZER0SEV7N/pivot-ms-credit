package com.pe.pivot.credit.domain.port.in;

import io.reactivex.rxjava3.core.Completable;

public interface DeleteCreditUseCase {
  Completable delete(String id);
}