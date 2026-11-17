package com.pe.pivot.credit.application.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.model.CreditType;
import com.pe.pivot.credit.infrastructure.config.CreditRulesProperties;
import com.pe.pivot.credit.infrastructure.util.CryptoUtil;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link CreditDomainInitializer}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Should initialize credit domain defaults")
class CreditDomainInitializerTest {

  @Mock
  private CreditRulesProperties rules;

  @Mock
  private CryptoUtil cryptoUtil;

  private CreditDomainInitializer initializer;

  @BeforeEach
  void setUp() {
    initializer = new CreditDomainInitializer(rules, cryptoUtil);
  }

  @Test
  @DisplayName("Should initialize loan defaults and generate a credit number")
  void initializesLoanDefaultsAndGeneratesCreditNumber() {
    // Arrange
    Credit credit = Credit.builder()
            .customerId("cust-1")
            .creditType(CreditType.PERSONAL_LOAN)
            .creditLimit(new BigDecimal("1000.00"))
            .build();
    when(cryptoUtil.hashSha256(anyString())).thenReturn("hashed-number");
    when(rules.resolveDefaultRate(CreditType.PERSONAL_LOAN)).thenReturn(new BigDecimal("0.15"));

    // Act
    initializer.initializeDefaults(credit);

    // Assert
    assertThat(credit.getCreditNumber()).startsWith("LN-").hasSize(11);
    assertThat(credit.getCreditNumberHash()).isEqualTo("hashed-number");
    assertThat(credit.getCurrency()).isEqualTo("PEN");
    assertThat(credit.getStatus()).isEqualTo(CreditStatus.ACTIVE);
    assertThat(credit.getInterestRate()).isEqualByComparingTo("0.15");
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("1000.00");
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("0");
    assertThat(credit.getCreatedAt()).isNotNull();
    assertThat(credit.getUpdatedAt()).isNotNull();
    verify(cryptoUtil).hashSha256(credit.getCreditNumber());
    verify(rules, never()).getDefaultPaymentDueDay();
  }

  @Test
  @DisplayName("Should keep card values and apply the default payment due day")
  void keepsProvidedValuesForCreditCardAndAppliesDefaultPaymentDueDay() {
    // Arrange
    Credit credit = Credit.builder()
            .creditNumber("4557-8800-1234-5678")
            .customerId("cust-2")
            .creditType(CreditType.PERSONAL_CREDIT_CARD)
            .creditLimit(new BigDecimal("500.00"))
            .usedAmount(new BigDecimal("120.00"))
            .currency("USD")
            .status(CreditStatus.BLOCKED)
            .interestRate(new BigDecimal("0.30"))
            .build();
    when(rules.getDefaultPaymentDueDay()).thenReturn(15);

    // Act
    initializer.initializeDefaults(credit);

    // Assert
    assertThat(credit.getCreditNumber()).isEqualTo("4557-8800-1234-5678");
    assertThat(credit.getCurrency()).isEqualTo("USD");
    assertThat(credit.getStatus()).isEqualTo(CreditStatus.BLOCKED);
    assertThat(credit.getInterestRate()).isEqualByComparingTo("0.30");
    assertThat(credit.getUsedAmount()).isEqualByComparingTo("120.00");
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("380.00");
    assertThat(credit.getPaymentDueDay()).isEqualTo(15);
    verify(rules, never()).resolveDefaultRate(any());
  }

  @Test
  @DisplayName("Should preserve the provided payment due day on a credit card")
  void preservesProvidedPaymentDueDayForCreditCard() {
    // Arrange
    Credit credit = Credit.builder()
            .creditNumber("4557-8800-0000-0001")
            .creditType(CreditType.BUSINESS_CREDIT_CARD)
            .creditLimit(new BigDecimal("200.00"))
            .paymentDueDay(5)
            .build();
    when(rules.resolveDefaultRate(CreditType.BUSINESS_CREDIT_CARD))
            .thenReturn(new BigDecimal("0.25"));

    // Act
    initializer.initializeDefaults(credit);

    // Assert
    assertThat(credit.getPaymentDueDay()).isEqualTo(5);
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("200.00");
  }

  @Test
  @DisplayName("Should generate a business card number when it is blank")
  void generatesBusinessCreditCardNumberWhenExistingNumberIsBlank() {
    // Arrange
    when(cryptoUtil.hashSha256(anyString())).thenReturn("hashed");
    when(rules.resolveDefaultRate(CreditType.BUSINESS_CREDIT_CARD))
            .thenReturn(new BigDecimal("0.22"));
    when(rules.getDefaultPaymentDueDay()).thenReturn(20);

    Credit credit = Credit.builder()
            .creditNumber("   ")
            .creditType(CreditType.BUSINESS_CREDIT_CARD)
            .creditLimit(new BigDecimal("800.00"))
            .build();

    // Act
    initializer.initializeDefaults(credit);

    // Assert
    assertThat(credit.getCreditNumber()).startsWith("4557-8800-").hasSize(18);
    assertThat(credit.getAvailableAmount()).isEqualByComparingTo("800.00");
    assertThat(credit.getPaymentDueDay()).isEqualTo(20);
  }
}
