package com.pe.pivot.credit.domain.model;

/**
 * Lifecycle states of a credit product.
 * CLOSED represents a logical Soft Delete when outstanding debt is 0.00.
 */
public enum CreditStatus {
  ACTIVE,
  PAID_OFF,
  BLOCKED,
  CLOSED
}