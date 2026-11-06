package com.pe.pivot.credit.domain.exception;

public class CreditNotFoundException extends RuntimeException {
  public CreditNotFoundException(String message) {
    super(message);
  }
}
