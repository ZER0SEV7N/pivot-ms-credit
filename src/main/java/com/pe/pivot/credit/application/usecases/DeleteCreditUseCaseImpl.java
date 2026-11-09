package com.pe.pivot.credit.application.usecases;

import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.port.in.DeleteCreditUseCase;
import com.pe.pivot.credit.domain.port.in.FindCreditByIdUseCase;
import com.pe.pivot.credit.domain.port.out.CreditRepositoryPort;
import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Single Responsibility Use Case for logically closing (Soft Delete) a credit product.
 * Enforces the state machine guard condition: [usedAmount == 0.00].
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteCreditUseCaseImpl implements DeleteCreditUseCase {

  private final CreditRepositoryPort creditRepository;
  private final FindCreditByIdUseCase findCreditByIdUseCase;

  /**
   * Logically closes (soft deletes) a credit product by its ID.
   *
   * @param id - the unique identifier of the credit product to be logically closed
   * @return - a Completable that completes when the credit product is successfully logically closed, or emits an error if the operation fails
   */
  @Override
  public Completable delete(String id) {
    log.debug("Initiating soft delete (logical closure) for credit ID: {}", id);
    return findCreditByIdUseCase.findById(id)
            .flatMapCompletable(existing -> {
              // Guard condition: A credit product can only be logically closed if its used amount is zero (no outstanding debt).
              if (existing.getStatus() == CreditStatus.CLOSED) {
                return Completable.error(new BusinessRuleException("Credit product is already CLOSED."));
              }
              // Guard condition: A credit product can only be logically closed if its used amount is zero (no outstanding debt).
              if (!existing.canBeClosed()) {
                return Completable.error(new BusinessRuleException("Cannot close credit product with outstanding debt (" + existing.getUsedAmount() + "). Debt must be 0.00 before closing."));
              }

              // Update the status to CLOSED and set the updated timestamp
              existing.setStatus(CreditStatus.CLOSED);
              existing.setUpdatedAt(Instant.now());
              return creditRepository.save(existing).ignoreElement();
            })
            .doOnComplete(() -> log.info("Credit product logically closed (CLOSED) with ID: {}", id));
  }
}