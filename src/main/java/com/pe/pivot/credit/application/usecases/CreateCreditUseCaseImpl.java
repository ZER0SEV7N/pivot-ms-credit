package com.pe.pivot.credit.application.usecases;

import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.exception.CustomerNotFoundException;
import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.model.CreditType;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
  private final CryptoUtil cryptoUtil;

  @Override
  public Single<Credit> create(Credit credit) {
    log.debug("Initiating credit product creation for customerId: {} and type: {}", credit.getCustomerId(), credit.getCreditType());

    return customerClient.findCustomerById(credit.getCustomerId())
            .switchIfEmpty(Single.error(new CustomerNotFoundException("Customer not found in ms-customer with ID: " + credit.getCustomerId())))
            .flatMap(customer -> {
              if(!customer.isActive()) {
                return Single.error(new BusinessRuleException("Cannot grant credit to an inactive or blocked customer."));
              }

              return creditRepository.findByCustomerId(customer.getId())
                      .toList()
                      .flatMap(existingCredits -> applyStrategy(customer, credit, existingCredits)
                              .andThen(Single.defer(() -> {
                                configureProductsDefaults(credit);
                                return verifyUniquenessAndSave(credit);
                              })));
            })
            .doOnSuccess(saved -> log.info("Credit product created successfully. ID: {}, MaskedNumber: {}",
                    saved.getId(), DataMaskingUtil.maskCreditNumber(saved.getCreditNumber())));
  }

  /**
   * Applies the appropriate validation strategy based on the customer type.
   *
   * @param customer - The summary of the customer requesting credit.
   * @param newCredit - The new credit product being requested.
   * @param existingCredits - The list of existing credit products the customer has.
   * @return - A Completable that completes if the validation passes, or emits an error if it fails.
   */
  private Completable applyStrategy(CustomerSummary customer, Credit newCredit, List<Credit> existingCredits) {
    return validationStrategies.stream()
            .filter(strategy -> strategy.supports(customer.getCustomerType()))
            .findFirst()
            .map(strategy -> strategy.validate(customer, newCredit, existingCredits))
            .orElse(Completable.error(new BusinessRuleException("Unsupported customer type: " + customer.getCustomerType())));
  }

  /**
   * Configures default values for the credit product based on its type and business rules.
   *
   * @param credit - The credit product to configure.
   * @return - The configured credit product.
   */
  private Single<Credit> verifyUniquenessAndSave(Credit credit) {
    return creditRepository.existsByCreditNumberHash(credit.getCreditNumberHash())
            .flatMap(exists -> {
              if (exists) {
                return Single.error(new BusinessRuleException("A credit product with the specified number already exists."));
              }
              return creditRepository.save(credit);
            });
  }

  /**
   * Configures default values for the credit product based on its type and business rules.
   *
   * @param credit - The credit product to configure.
   */
  private void configureProductsDefaults(Credit credit) {
    // Set default values for credit number, currency, status, and payment due day if they are not provided
    String prefix = credit.getCreditType().isCreditCard() ? "4557-8800-" : "LN-";
    String rawCreditNumber = Optional.ofNullable(credit.getCreditNumber())
            .filter(num -> !num.isBlank())
            .orElseGet(() -> prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

    // Set the credit number and its hash
    credit.setCreditNumber(rawCreditNumber);
    credit.setCreditNumberHash(cryptoUtil.hashSha256(rawCreditNumber));
    credit.setCurrency(Optional.ofNullable(credit.getCurrency()).orElse("PEN"));
    credit.setStatus(Optional.ofNullable(credit.getStatus()).orElse(CreditStatus.ACTIVE));
    credit.setPaymentDueDay(Optional.ofNullable(credit.getPaymentDueDay()).orElse(rulesProperties.getDefaultPaymentDueDay()));

    if(credit.getCreditType().isLoan()){
      // In a Loan, the full principal is immediately disbursed (usedAmount = creditLimit, available = 0)
      credit.setUsedAmount(credit.getCreditLimit());
      credit.setAvailableAmount(BigDecimal.ZERO);

      // Set default interest rate based on credit type if not provided
      BigDecimal defaultRate = credit.getCreditType() == CreditType.PERSONAL_LOAN
              ? rulesProperties.getDefaultPersonalLoanRate()
              : rulesProperties.getDefaultBusinessLoanRate();
      credit.setInterestRate(Optional.ofNullable(credit.getInterestRate()).orElse(defaultRate));
    } else {
      // In a Credit Card, the line starts with 0 debt and full available limit
      credit.setUsedAmount(BigDecimal.ZERO);
      credit.setAvailableAmount(credit.getCreditLimit());
      credit.setInterestRate(Optional.ofNullable(credit.getInterestRate()).orElse(rulesProperties.getDefaultCreditCardRate()));
    }

    // Set timestamps for creation and last update
    Instant now = Instant.now();
    credit.setCreatedAt(now);
    credit.setUpdatedAt(now);
  }
}

