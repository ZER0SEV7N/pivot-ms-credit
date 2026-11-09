package com.pe.pivot.credit.infrastructure.adapter.out.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MongoDB Document representing an active credit product in 'db_pivot_credit'.
 * Sensitive creditNumber is stored encrypted with AES-256-GCM.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "credits")
public class CreditDocument {

  @Id
  private String id;

  private String creditNumber;

  @Indexed(unique = true)
  private String creditNumberHash;

  @Indexed
  private String customerId;

  private String creditType;
  private String currency;
  private BigDecimal creditLimit;
  private BigDecimal usedAmount;
  private BigDecimal availableAmount;
  private BigDecimal interestRate;
  private Integer paymentDueDay;
  private String status;
  private Instant createdAt;
  private Instant updatedAt;
}
