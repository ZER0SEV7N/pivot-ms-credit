package com.pe.pivot.credit.domain.exception;

/**
 * Exception thrown when a requested credit product cannot be found.
 */
public class CreditNotFoundException extends RuntimeException {
  public CreditNotFoundException(String message) {
    super(message);
  }
}
