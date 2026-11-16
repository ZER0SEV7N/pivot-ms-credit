package com.pe.pivot.credit.domain.exception;

/**
 * Exception thrown when a credit operation violates a business rule.
 */
public class BusinessRuleException extends RuntimeException {
  public BusinessRuleException(String message) {
    super(message);
  }
}
