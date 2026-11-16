package com.pe.pivot.credit.infrastructure.config;

import com.pe.pivot.credit.domain.model.CreditType;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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

  // Business Logic Methods
  /**
   * Resolves the default interest rate for the given credit type.
   *
   * @param type - the credit type whose default rate is required
   * @return the configured default rate for the credit type
   */
  public BigDecimal resolveDefaultRate(CreditType type) {
    if (type == CreditType.PERSONAL_LOAN) {
      return defaultPersonalLoanRate;
    }
    if (type == CreditType.BUSINESS_LOAN) {
      return defaultBusinessLoanRate;
    }
    return defaultCreditCardRate;
  }
}
