package com.pe.pivot.credit.infrastructure.config;

import com.pe.pivot.credit.domain.strategy.operation.ConsumptionOperationStrategy;
import com.pe.pivot.credit.domain.strategy.operation.CreditOperationStrategy;
import com.pe.pivot.credit.domain.strategy.operation.PaymentOperationStrategy;
import com.pe.pivot.credit.domain.strategy.validation.BusinessCreditStrategy;
import com.pe.pivot.credit.domain.strategy.validation.CreditValidationStrategy;
import com.pe.pivot.credit.domain.strategy.validation.PersonalCreditStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for defining beans related to credit validation and operation strategies.
 * This class provides beans for different credit validation strategies (personal and business) and
 * credit operation strategies (consumption and payment). These strategies encapsulate the business
 * rules and logic for validating credit products and performing operations on them.
 */
@Configuration
public class StrategyConfig {

  @Bean
  public CreditValidationStrategy personalCreditStrategy() {
    return new PersonalCreditStrategy();
  }

  @Bean
  public CreditValidationStrategy businessCreditStrategy() {return new BusinessCreditStrategy();}

  @Bean
  public CreditOperationStrategy consumptionOperationStrategy() {
    return new ConsumptionOperationStrategy();
  }

  @Bean
  public CreditOperationStrategy paymentOperationStrategy() {
    return new PaymentOperationStrategy();
  }
}
