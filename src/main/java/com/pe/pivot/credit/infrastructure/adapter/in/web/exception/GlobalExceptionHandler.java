package com.pe.pivot.credit.infrastructure.adapter.in.web.exception;

import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.exception.CreditNotFoundException;
import com.pe.pivot.credit.domain.exception.CustomerNotFoundException;
import com.pe.pivot.credit.domain.exception.DuplicateCreditException;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.ErrorResponse;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global Exception Handler translating domain and validation exceptions into clean OpenAPI ErrorResponse.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
    * Handles exceptions when a requested resource is not found.
    *
    * @param ex - The exception thrown when a resource is not found.
    * @param exchange - The current server exchange.
    * @return - A Single emitting an ErrorResponse with NOT_FOUND status.
  */
  @ExceptionHandler({CreditNotFoundException.class, CustomerNotFoundException.class})
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Single<ErrorResponse> handleNotFound(RuntimeException ex, ServerWebExchange exchange) {
    log.warn("Resource not found: {}", ex.getMessage());
    return Single.just(buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), exchange));
  }

  /**
    * Handles exceptions when a duplicate resource is detected.
    *
    * @param ex - The exception thrown when a duplicate resource is detected.
    * @param exchange - The current server exchange.
    * @return - A Single emitting an ErrorResponse with CONFLICT status.
  */
  @ExceptionHandler(DuplicateCreditException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Single<ErrorResponse> handleDuplicate(DuplicateCreditException ex, ServerWebExchange exchange) {
    log.warn("Conflict error: {}", ex.getMessage());
    return Single.just(buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), exchange));
  }

  /**
   * Handles business rule violations and translates them into a BAD_REQUEST response.
   *
   * @param ex - The exception thrown when a business rule is violated.
   * @param exchange - The current server exchange.
   * @return - A Single emitting an ErrorResponse with BAD_REQUEST status.
   */
  @ExceptionHandler(BusinessRuleException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Single<ErrorResponse> handleBusinessRule(BusinessRuleException ex, ServerWebExchange exchange) {
    log.warn("Business rule violation: {}", ex.getMessage());
    return Single.just(buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange));
  }

  /**
    * Handles validation errors in the request payload.
    *
    * @param ex - The exception thrown when validation fails.
    * @param exchange - The current server exchange.
    * @return - A Single emitting an ErrorResponse with BAD_REQUEST status.
    */
  @ExceptionHandler(WebExchangeBindException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Single<ErrorResponse> handleValidationErrors(WebExchangeBindException ex, ServerWebExchange exchange) {
    String errors = ex.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining("; "));
    log.warn("Payload validation error: {}", errors);
    return Single.just(buildErrorResponse(HttpStatus.BAD_REQUEST, errors, exchange));
  }

  /**
   * Handles any unexpected exceptions that are not explicitly handled by other methods.
   *
   * @param ex - The unexpected exception thrown during request processing.
   * @param exchange - The current server exchange.
   * @return - A Single emitting an ErrorResponse with INTERNAL_SERVER_ERROR status.
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Single<ErrorResponse> handleGenericException(Exception ex, ServerWebExchange exchange) {
    log.error("Unexpected error occurred: ", ex);
    return Single.just(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected internal server error occurred.", exchange));
  }

  /**
   * Builds a standardized ErrorResponse object for the given HTTP status, message, and server exchange.
   *
   * @param status - The HTTP status to be included in the error response.
   * @param message - The error message to be included in the error response.
   * @param exchange - The current server exchange, used to extract the request path.
   * @return -  An ErrorResponse object containing the timestamp, status code, error reason, message, and request path.
   */
  private ErrorResponse buildErrorResponse(HttpStatus status, String message, ServerWebExchange exchange) {
    return ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(exchange.getRequest().getPath().value())
            .build();
  }
}