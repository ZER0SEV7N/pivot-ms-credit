package com.pe.pivot.credit.domain.port.in;

import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditOperationType;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;

/**
 * Input port for applying balance operations (PAYMENT or CONSUMPTION) to a credit product.
 */
public interface UpdateCreditBalanceUseCase {
  Single<Credit> updateBalance(String id, CreditOperationType operationType, BigDecimal amount);
}
