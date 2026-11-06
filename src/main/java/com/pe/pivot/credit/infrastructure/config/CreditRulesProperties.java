package com.pe.pivot.credit.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Externalized business rule parameters for credit products fetched from ms-config.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.credit-rules")
public class CreditRulesProperties {

  private BigDecimal defaultPersonalLoanRate;
  private BigDecimal defaultBusinessLoanRate;
  private BigDecimal defaultCreditCardRate;
  private Integer defaultPaymentDueDay;
}
