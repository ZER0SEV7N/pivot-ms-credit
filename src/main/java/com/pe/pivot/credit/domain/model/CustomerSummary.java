package com.pe.pivot.credit.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain projection of Customer information retrieved reactively from ms-customer.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummary {

  private String id;
  private String customerType;
  private String documentNumber;
  private String status;

  public boolean isActive() {
    return "ACTIVE".equalsIgnoreCase(this.status);
  }

  public boolean isPersonal() {
    return "PERSONAL".equalsIgnoreCase(this.customerType);
  }

  public boolean isBusiness() {
    return "BUSINESS".equalsIgnoreCase(this.customerType);
  }
}