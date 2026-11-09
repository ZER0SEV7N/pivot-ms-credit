package com.pe.pivot.credit.application.usecases;

import com.pe.pivot.credit.domain.exception.CreditNotFoundException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.port.in.FindCreditByIdUseCase;
import com.pe.pivot.credit.domain.port.in.RetrieveCreditUseCase;
import com.pe.pivot.credit.domain.port.out.CreditRepositoryPort;
import com.pe.pivot.credit.infrastructure.util.CryptoUtil;
import com.pe.pivot.credit.infrastructure.util.DataMaskingUtil;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Single Responsibility Use Case for querying credit products.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetrieveCreditUseCaseImpl implements RetrieveCreditUseCase {

  private final CreditRepositoryPort creditRepository;
  private final FindCreditByIdUseCase findCreditByIdUseCase;
  private final CryptoUtil cryptoUtil;

  /**
   * Retrieve all credit products.
   *
   * @return - A Flowable emitting all Credit products.
   */
  @Override
  public Flowable<Credit> findAll() {
    log.debug("Retrieving all credit products");
    return creditRepository.findAll();
  }

  /**
   * Find a credit by its ID.
   *
   * @param id - The ID of the credit to find.
   * @return - A Single emitting the found Credit, or an error if not found.
   */
  @Override
  public Single<Credit> findById(String id) {
    return findCreditByIdUseCase.findById(id);
  }

  /**
   * Find a credit by its credit number. The credit number is hashed for security reasons before querying the repository.
   *
   * @param creditNumber - The credit number to search for.
   * @return - A Single emitting the found Credit, or an error if not found.
   */
  @Override
  public Single<Credit> findByCreditNumber(String creditNumber) {
    String hash = cryptoUtil.hashSha256(creditNumber);
    log.debug("Searching credit product by masked number: {}", DataMaskingUtil.maskCreditNumber(creditNumber));
    return creditRepository.findByCreditNumberHash(hash)
            .switchIfEmpty(Single.error(new CreditNotFoundException("Credit product not found with number: " + DataMaskingUtil.maskCreditNumber(creditNumber))));
  }

  /**
   * Find all credit products associated with a specific customer ID.
   *
   * @param customerId - The ID of the customer whose credit products are to be retrieved.
   * @return - A Flowable emitting all Credit products associated with the given customer ID.
   */
  @Override
  public Flowable<Credit> findByCustomerId(String customerId) {
    log.debug("Retrieving credit products for customerId: {}", customerId);
    return creditRepository.findByCustomerId(customerId);
  }
}
