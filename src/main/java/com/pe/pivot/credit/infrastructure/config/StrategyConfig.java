package com.pe.pivot.credit.infrastructure.config;

import com.pe.pivot.credit.domain.strategy.BusinessCreditStrategy;
import com.pe.pivot.credit.domain.strategy.CreditValidationStrategy;
import com.pe.pivot.credit.domain.strategy.PersonalCreditStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StrategyConfig {

  @Bean
  public CreditValidationStrategy personalCreditStrategy() {
    return new PersonalCreditStrategy();
  }

  @Bean
  public CreditValidationStrategy businessCreditStrategy() {
    return new BusinessCreditStrategy();
  }
}
