package com.pe.pivot.credit.domain.exception;

public class DuplicateCreditException extends RuntimeException {
  public DuplicateCreditException(String message) {
    super(message);
  }
}
