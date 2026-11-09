package com.pe.pivot.credit.application.usecases;

import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditOperationType;
import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.port.in.FindCreditByIdUseCase;
import com.pe.pivot.credit.domain.port.in.UpdateCreditBalanceUseCase;
import com.pe.pivot.credit.domain.port.out.CreditRepositoryPort;
import com.pe.pivot.credit.domain.strategy.operation.ConsumptionOperationStrategy;
import com.pe.pivot.credit.domain.strategy.operation.CreditOperationStrategy;
import com.pe.pivot.credit.domain.strategy.operation.PaymentOperationStrategy;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditBalanceUseCaseImpl implements UpdateCreditBalanceUseCase {

  private final CreditRepositoryPort creditRepository;
  private final FindCreditByIdUseCase findCreditByIdUseCase;
  private final List<CreditOperationStrategy> operationStrategies;

  /**
   * Updates the balance of a credit product based on the specified operation type and amount.
   *
   * @param id - The unique identifier of the credit product to update.
   * @param operationType - The type of operation to perform (e.g., CONSUMPTION, PAYMENT).
   * @param amount - The amount to apply for the operation.
   * @return - A Single emitting the updated Credit product or an error if the operation fails.
   */
  @Override
  public Single<Credit> updateBalance(String id, CreditOperationType operationType, BigDecimal amount) {
    log.debug("Processing credit balance operation for creditId: {}, operation: {}, amount: {}", id, operationType, amount);

    // Validate the credit product exists and is in a valid state for the operation
    return findCreditByIdUseCase.findById(id)
            .flatMap(credit -> validatePreconditions(credit, amount))
            .flatMap(credit -> resolveStrategy(operationType).applyOperation(credit, amount))
            .flatMap(creditRepository::save)
            .doOnSuccess(updated -> log.info(
                    "Credit balance updated for ID: {}. Used: {}, Available: {}, Status: {}",
                    updated.getId(), updated.getUsedAmount(), updated.getAvailableAmount(), updated.getStatus()));
  }

  /**
   * Validates the preconditions for performing a credit balance operation.
   *
   * @param credit - The credit product to validate.
   * @param amount - The amount to be used in the operation.
   * @return - A Single emitting the credit product if valid, or an error if any precondition fails.
   */
  private Single<Credit> validatePreconditions(Credit credit, BigDecimal amount) {
    // Validate that the credit product is ACTIVE and the amount is positive
    if (credit.getStatus() != CreditStatus.ACTIVE) {
      return Single.error(new BusinessRuleException("Credit product is not ACTIVE. Current status: " + credit.getStatus()));
    }

    // Validate that the amount is positive
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      return Single.error(new BusinessRuleException("Operation amount must be greater than zero."));
    }
    return Single.just(credit);
  }

  /**
   * Resolves the appropriate strategy for the given credit operation type.
   *
   * @param operationType - The type of credit operation to perform (e.g., CONSUMPTION, PAYMENT).
   * @return - The corresponding CreditOperationStrategy for the specified operation type.
   */
  private CreditOperationStrategy resolveStrategy(CreditOperationType operationType) {
    return operationStrategies.stream()
            .filter(strategy -> strategy.supports(operationType))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleException("Unsupported credit operation type: " + operationType));
  }
}