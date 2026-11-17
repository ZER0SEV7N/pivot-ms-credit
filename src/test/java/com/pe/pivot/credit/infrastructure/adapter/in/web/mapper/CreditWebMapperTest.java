package com.pe.pivot.credit.infrastructure.adapter.in.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.model.CreditType;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditBalanceResponse;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditRequest;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.CreditResponse;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CreditWebMapper}.
 */
@DisplayName("Should map credit web requests and responses")
class CreditWebMapperTest {

  private CreditWebMapper mapper;

  private Credit credit;

  @BeforeEach
  void setUp() {
    mapper = new CreditWebMapper();
    credit = Credit.builder()
            .id("credit-1")
            .creditNumber("LN-1234567890")
            .customerId("cust-1")
            .creditType(CreditType.PERSONAL_LOAN)
            .currency("PEN")
            .creditLimit(new BigDecimal("1000.00"))
            .usedAmount(new BigDecimal("250.00"))
            .availableAmount(new BigDecimal("750.00"))
            .interestRate(new BigDecimal("0.15"))
            .paymentDueDay(15)
            .status(CreditStatus.ACTIVE)
            .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
            .updatedAt(Instant.parse("2026-01-02T00:00:00Z"))
            .build();
  }

  @Test
  @DisplayName("Should map a request to the domain credit")
  void mapsRequestToDomainCredit() {
    // Arrange
    CreditRequest request = CreditRequest.builder()
            .creditNumber("LN-0000000001")
            .customerId("cust-9")
            .creditType(CreditType.BUSINESS_CREDIT_CARD)
            .currency("USD")
            .creditLimit(new BigDecimal("5000.00"))
            .interestRate(new BigDecimal("0.22"))
            .paymentDueDay(20)
            .status(CreditStatus.BLOCKED)
            .build();

    // Act
    Credit domain = mapper.toDomain(request);

    // Assert
    assertThat(domain.getCreditNumber()).isEqualTo("LN-0000000001");
    assertThat(domain.getCustomerId()).isEqualTo("cust-9");
    assertThat(domain.getCreditType()).isEqualTo(CreditType.BUSINESS_CREDIT_CARD);
    assertThat(domain.getCurrency()).isEqualTo("USD");
    assertThat(domain.getCreditLimit()).isEqualByComparingTo("5000.00");
    assertThat(domain.getInterestRate()).isEqualByComparingTo("0.22");
    assertThat(domain.getPaymentDueDay()).isEqualTo(20);
    assertThat(domain.getStatus()).isEqualTo(CreditStatus.BLOCKED);
  }

  @Test
  @DisplayName("Should map an unmasked response keeping the full credit number")
  void mapsUnmaskedResponseKeepingFullCreditNumber() {
    // Act
    CreditResponse response = mapper.toUnmaskedResponse(credit);

    // Assert
    assertThat(response.id()).isEqualTo("credit-1");
    assertThat(response.creditNumber()).isEqualTo("LN-1234567890");
    assertThat(response.customerId()).isEqualTo("cust-1");
    assertThat(response.creditType()).isEqualTo(CreditType.PERSONAL_LOAN);
    assertThat(response.currency()).isEqualTo("PEN");
    assertThat(response.creditLimit()).isEqualByComparingTo("1000.00");
    assertThat(response.usedAmount()).isEqualByComparingTo("250.00");
    assertThat(response.availableAmount()).isEqualByComparingTo("750.00");
    assertThat(response.interestRate()).isEqualByComparingTo("0.15");
    assertThat(response.paymentDueDay()).isEqualTo(15);
    assertThat(response.status()).isEqualTo(CreditStatus.ACTIVE);
    assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    assertThat(response.updatedAt()).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"));
  }

  @Test
  @DisplayName("Should map a masked response hiding the credit number")
  void mapsMaskedResponseHidingCreditNumber() {
    // Act
    CreditResponse response = mapper.toResponse(credit);

    // Assert
    assertThat(response.creditNumber()).isEqualTo("*********7890");
    assertThat(response.id()).isEqualTo("credit-1");
  }

  @Test
  @DisplayName("Should map a balance response with a masked number and timestamp")
  void mapsBalanceResponseWithMaskedNumberAndQueryTimestamp() {
    // Act
    CreditBalanceResponse response = mapper.toBalanceResponse(credit);

    // Assert
    assertThat(response.creditId()).isEqualTo("credit-1");
    assertThat(response.creditNumber()).isEqualTo("*********7890");
    assertThat(response.creditType()).isEqualTo(CreditType.PERSONAL_LOAN);
    assertThat(response.currency()).isEqualTo("PEN");
    assertThat(response.creditLimit()).isEqualByComparingTo("1000.00");
    assertThat(response.usedAmount()).isEqualByComparingTo("250.00");
    assertThat(response.availableAmount()).isEqualByComparingTo("750.00");
    assertThat(response.status()).isEqualTo(CreditStatus.ACTIVE);
    assertThat(response.queriedAt()).isNotNull();
  }
}
