package com.pe.pivot.credit.domain.port.in;

import com.pe.pivot.credit.domain.model.Credit;
import io.reactivex.rxjava3.core.Single;

public interface FindCreditByIdUseCase {
  Single<Credit> findById(String id);
}
