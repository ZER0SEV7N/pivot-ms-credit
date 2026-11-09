package com.pe.pivot.credit.application.usecases;

import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.port.in.FindCreditByIdUseCase;
import com.pe.pivot.credit.domain.port.in.UpdateCreditUseCase;
import com.pe.pivot.credit.domain.port.out.CreditRepositoryPort;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Single Responsibility Use Case for updating administrative credit terms.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCreditUseCaseImpl implements UpdateCreditUseCase {

  private final CreditRepositoryPort creditRepository;
  private final FindCreditByIdUseCase findCreditByIdUseCase;

  /**
   * Updates the administrative terms of a credit product.
   *
   * @param id - the unique identifier of the credit product to be updated
   * @param incoming - the Credit object containing the new values for the administrative terms
   * @return - a Single emitting the updated Credit object upon successful update
   */
  @Override
  public Single<Credit> updateCredit(String id, Credit incoming){
    log.debug("Updating credit product with ID: {}", id);
    return findCreditByIdUseCase.findById(id)
            .flatMap(existing -> {
              // Update fields of the existing credit with incoming values
              Optional.ofNullable(incoming.getCurrency()).ifPresent(existing::setCurrency);
              Optional.ofNullable(incoming.getInterestRate()).ifPresent(existing::setInterestRate);
              Optional.ofNullable(incoming.getPaymentDueDay()).ifPresent(existing::setPaymentDueDay);
              Optional.ofNullable(incoming.getStatus()).ifPresent(existing::setStatus);
              existing.setUpdatedAt(Instant.now());
              return creditRepository.save(existing);
            })
            .doOnSuccess(updated -> log.info("Credit product updated successfully with ID: {}", updated.getId()));
  }
}
