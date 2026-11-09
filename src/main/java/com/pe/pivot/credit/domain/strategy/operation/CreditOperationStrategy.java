package com.pe.pivot.credit.domain.strategy.operation;

import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditOperationType;
import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;

/**
 * Strategy interface for executing financial operations (PAYMENT or CONSUMPTION) on a Credit product.
 * Eliminates cyclomatic complexity in CreditBalanceUseCaseImpl (OCP & SRP).
 */
public interface CreditOperationStrategy {

  boolean supports(CreditOperationType operationType);

  Single<Credit> applyOperation(Credit credit, BigDecimal amount);
}
