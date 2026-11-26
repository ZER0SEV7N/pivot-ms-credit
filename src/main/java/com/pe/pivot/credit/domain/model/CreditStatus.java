package com.pe.pivot.credit.domain.model;

/**
 * Lifecycle states of a credit product.
 * CLOSED represents a logical Soft Delete when outstanding debt is 0.00.
 * OVERDUE represents a credit product that is past its due date.
 */
public enum CreditStatus {
  ACTIVE,
  OVERDUE,
  PAID_OFF,
  BLOCKED,
  CLOSED
}