package com.pe.pivot.credit.infrastructure.adapter.out.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.pe.pivot.credit.domain.model.Credit;
import com.pe.pivot.credit.domain.model.CreditStatus;
import com.pe.pivot.credit.domain.model.CreditType;
import com.pe.pivot.credit.infrastructure.adapter.out.persistence.entity.CreditDocument;
import com.pe.pivot.credit.infrastructure.util.CryptoUtil;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CreditPersistenceMapper}.
 */
@DisplayName("Should map credit documents and domain objects")
class CreditPersistenceMapperTest {

  private CryptoUtil cryptoUtil;

  private CreditPersistenceMapper mapper;

  @BeforeEach
  void setUp() {
    cryptoUtil = new CryptoUtil("unit-test-aes-secret-key");
    mapper = new CreditPersistenceMapper(cryptoUtil);
  }

  @Test
  @DisplayName("Should map the domain to a document encrypting the credit number")
  void mapsDomainToDocumentEncryptingCreditNumber() {
    // Arrange
    Credit domain = fullCredit();

    // Act
    CreditDocument document = mapper.toDocument(domain);

    // Assert
    assertThat(document.getId()).isEqualTo("credit-1");
    assertThat(document.getCreditNumber()).isNotEqualTo(domain.getCreditNumber());
    assertThat(cryptoUtil.decryptAes256(document.getCreditNumber()))
            .isEqualTo(domain.getCreditNumber());
    assertThat(document.getCreditNumberHash()).isEqualTo("hashed");
    assertThat(document.getCustomerId()).isEqualTo("cust-1");
    assertThat(document.getCreditType()).isEqualTo("PERSONAL_LOAN");
    assertThat(document.getCurrency()).isEqualTo("PEN");
    assertThat(document.getCreditLimit()).isEqualByComparingTo("1000.00");
    assertThat(document.getUsedAmount()).isEqualByComparingTo("250.00");
    assertThat(document.getAvailableAmount()).isEqualByComparingTo("750.00");
    assertThat(document.getInterestRate()).isEqualByComparingTo("0.15");
    assertThat(document.getPaymentDueDay()).isEqualTo(15);
    assertThat(document.getStatus()).isEqualTo("ACTIVE");
    assertThat(document.getCreatedAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    assertThat(document.getUpdatedAt()).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"));
  }

  @Test
  @DisplayName("Should map a domain with null optional fields to a document")
  void mapsDomainWithNullOptionalFieldsToDocument() {
    // Arrange
    Credit domain = Credit.builder().id("credit-2").build();

    // Act
    CreditDocument document = mapper.toDocument(domain);

    // Assert
    assertThat(document.getId()).isEqualTo("credit-2");
    assertThat(document.getCreditNumber()).isNull();
    assertThat(document.getCreditType()).isNull();
    assertThat(document.getStatus()).isNull();
  }

  @Test
  @DisplayName("Should map a document to the domain decrypting the credit number")
  void mapsDocumentToDomainDecryptingCreditNumber() {
    // Arrange
    CreditDocument document = CreditDocument.builder()
            .id("credit-1")
            .creditNumber(cryptoUtil.encryptAes256("LN-1234567890"))
            .creditNumberHash("hashed")
            .customerId("cust-1")
            .creditType("BUSINESS_CREDIT_CARD")
            .currency("USD")
            .creditLimit(new BigDecimal("5000.00"))
            .usedAmount(new BigDecimal("1000.00"))
            .availableAmount(new BigDecimal("4000.00"))
            .interestRate(new BigDecimal("0.22"))
            .paymentDueDay(20)
            .status("BLOCKED")
            .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
            .updatedAt(Instant.parse("2026-01-02T00:00:00Z"))
            .build();

    // Act
    Credit domain = mapper.toDomain(document);

    // Assert
    assertThat(domain.getId()).isEqualTo("credit-1");
    assertThat(domain.getCreditNumber()).isEqualTo("LN-1234567890");
    assertThat(domain.getCreditNumberHash()).isEqualTo("hashed");
    assertThat(domain.getCustomerId()).isEqualTo("cust-1");
    assertThat(domain.getCreditType()).isEqualTo(CreditType.BUSINESS_CREDIT_CARD);
    assertThat(domain.getCurrency()).isEqualTo("USD");
    assertThat(domain.getCreditLimit()).isEqualByComparingTo("5000.00");
    assertThat(domain.getUsedAmount()).isEqualByComparingTo("1000.00");
    assertThat(domain.getAvailableAmount()).isEqualByComparingTo("4000.00");
    assertThat(domain.getInterestRate()).isEqualByComparingTo("0.22");
    assertThat(domain.getPaymentDueDay()).isEqualTo(20);
    assertThat(domain.getStatus()).isEqualTo(CreditStatus.BLOCKED);
    assertThat(domain.getCreatedAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    assertThat(domain.getUpdatedAt()).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"));
  }

  @Test
  @DisplayName("Should map a document with null optional fields to the domain")
  void mapsDocumentWithNullOptionalFieldsToDomain() {
    // Arrange
    CreditDocument document = CreditDocument.builder().id("credit-3").build();

    // Act
    Credit domain = mapper.toDomain(document);

    // Assert
    assertThat(domain.getId()).isEqualTo("credit-3");
    assertThat(domain.getCreditNumber()).isNull();
    assertThat(domain.getCreditType()).isNull();
    assertThat(domain.getStatus()).isNull();
  }

  private Credit fullCredit() {
    return Credit.builder()
            .id("credit-1")
            .creditNumber("LN-1234567890")
            .creditNumberHash("hashed")
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
}
