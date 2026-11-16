package com.pe.pivot.credit.domain.exception;

/**
 * Exception thrown when a credit product with the same credit number already exists.
 */
public class DuplicateCreditException extends RuntimeException {
  public DuplicateCreditException(String message) {
    super(message);
  }
}
