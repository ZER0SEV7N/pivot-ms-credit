package com.pe.pivot.credit.application.usecases;

import com.pe.pivot.credit.domain.exception.CreditNotFoundException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.port.in.FindCreditByIdUseCase;
import com.pe.pivot.credit.domain.port.out.CreditRepositoryPort;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Single Responsibility Use Case for finding a credit product by its unique ID.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FindCreditsByIdUseCaseImpl implements FindCreditByIdUseCase {

  private final CreditRepositoryPort creditRepository;

  /**
   * Finds a credit product by its ID.
   *
   * @param id - The ID of the credit product to find.
   * @return - A Single emitting the found Credit product or an error if not found.
   */
  @Override
  public Single<Credit> findById(String id) {
     log.debug("Searching credit product by ID: {}", id);
     return creditRepository.findById(id)
             .switchIfEmpty(Single.error(new CreditNotFoundException("Credit product not found with ID: " + id)));
   }


}
