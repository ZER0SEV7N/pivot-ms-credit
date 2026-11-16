package com.pe.pivot.credit.application.usecases;

import com.pe.pivot.credit.application.support.CreditDomainInitializer;
import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.exception.CustomerNotFoundException;
import com.pe.pivot.credit.domain.exception.DuplicateCreditException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CustomerSummary;
import com.pe.pivot.credit.domain.port.in.CreateCreditUseCase;
import com.pe.pivot.credit.domain.port.out.CreditRepositoryPort;
import com.pe.pivot.credit.domain.port.out.CustomerClientPort;
import com.pe.pivot.credit.domain.strategy.validation.CreditValidationStrategy;
import com.pe.pivot.credit.infrastructure.config.CreditRulesProperties;
import com.pe.pivot.credit.infrastructure.util.CryptoUtil;
import com.pe.pivot.credit.infrastructure.util.DataMaskingUtil;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Single Responsibility Use Case for granting loans and issuing credit cards.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateCreditUseCaseImpl implements CreateCreditUseCase {

  private final CreditRepositoryPort creditRepository;
  private final CustomerClientPort customerClient;
  private final List<CreditValidationStrategy> validationStrategies;
  private final CreditRulesProperties rulesProperties;
  private final CreditDomainInitializer creditInitializer;
  private final CryptoUtil cryptoUtil;

  /**
   * Creates a new credit product for a customer, ensuring that the customer is active and
   * that the credit product adheres to business rules and validation strategies.
   *
   * @param credit - the Credit object containing the details of the credit product to be created
   * @return - a Single emitting the created Credit object upon successful creation, or an error
   *     if the operation fails
   */
  @Override
  public Single<Credit> create(Credit credit) {
    log.debug("Initiating credit creation for customerId: {} and type: {}",
            credit.getCustomerId(), credit.getCreditType());

    // Validate that the customer exists and is active, then validate the credit product against
    // business rules
    return fetchActiveCustomer(credit.getCustomerId())
            .flatMap(customer -> creditRepository.findByCustomerId(customer.getId())
                    .toList()
                    // Validate the new credit product against existing credits and business rules
                    .flatMap(existingCredits ->
                            validateWithStrategy(customer, credit, existingCredits)
                                    .andThen(Single.defer(() -> {
                                      creditInitializer.initializeDefaults(credit);
                                      return saveUniqueCredit(credit);
                                    }))))
            .doOnSuccess(saved -> log.info(
                    "Credit product created successfully. ID: {}, MaskedNumber: {}",
                    saved.getId(), DataMaskingUtil.maskCreditNumber(saved.getCreditNumber())));
  }

  /**
   * Fetches the customer summary for the given customer ID and ensures that the customer is
   * active.
   *
   * @param customerId - The unique identifier of the customer to fetch.
   * @return - A Single emitting the CustomerSummary if the customer is found and active, or an
   *     error if not.
   */
  private Single<CustomerSummary> fetchActiveCustomer(String customerId) {
    // Validate that the customer exists and is active before proceeding with credit creation
    return customerClient.findCustomerById(customerId)
            .switchIfEmpty(Single.error(new CustomerNotFoundException(
                    "Customer not found in ms-customer with ID: " + customerId)))
            .flatMap(customer -> customer.isActive()
                    ? Single.just(customer)
                    : Single.error(new BusinessRuleException(
                            "Cannot grant credit products to an inactive or blocked customer.")));
  }

  /**
   * Validates the new credit product against existing credits
   * and business rules using the appropriate validation strategy.
   *
   * @param customer - The customer summary for whom the credit product is being created.
   * @param newCredit - The new credit product being requested.
   * @param existingCredits - The list of existing credit products the customer has.
   * @return - A Completable that completes if the validation passes, or emits an error if it
   *     fails.
   */
  private Completable validateWithStrategy(
          CustomerSummary customer, Credit newCredit, List<Credit> existingCredits) {
    // Use the appropriate validation strategy based on the customer type to validate the new
    // credit product
    return validationStrategies.stream()
            .filter(strategy -> strategy.supports(customer.getCustomerType()))
            .findFirst()
            .map(strategy -> strategy.validate(customer, newCredit, existingCredits))
            .orElseGet(() -> Completable.error(new BusinessRuleException(
                    "Unsupported customer type: " + customer.getCustomerType())));
  }

  /**
   * Saves the new credit product to the repository, ensuring that the credit number is unique.
   *
   * @param credit - The credit product to be saved.
   * @return - A Single emitting the saved Credit product, or an error if a duplicate credit
   *     number exists.
   */
  private Single<Credit> saveUniqueCredit(Credit credit) {
    return creditRepository.existsByCreditNumberHash(credit.getCreditNumberHash())
            .flatMap(exists -> Boolean.TRUE.equals(exists)
                    ? Single.error(new DuplicateCreditException(
                            "A credit product with the specified number already exists."))
                    : creditRepository.save(credit));
  }
}
