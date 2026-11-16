package com.pe.pivot.credit.domain.exception;

/**
 * Exception thrown when a customer referenced by a credit request cannot be found.
 */
public class CustomerNotFoundException extends RuntimeException {
  public CustomerNotFoundException(String message) {
    super(message);
  }
}
