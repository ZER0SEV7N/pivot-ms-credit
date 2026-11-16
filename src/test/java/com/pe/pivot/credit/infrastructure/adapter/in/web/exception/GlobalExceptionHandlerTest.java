package com.pe.pivot.credit.infrastructure.adapter.in.web.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pe.pivot.credit.domain.exception.BusinessRuleException;
import com.pe.pivot.credit.domain.exception.CreditNotFoundException;
import com.pe.pivot.credit.domain.exception.CustomerNotFoundException;
import com.pe.pivot.credit.domain.exception.DuplicateCreditException;
import com.pe.pivot.credit.infrastructure.adapter.in.web.dto.ErrorResponse;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 */
@DisplayName("GlobalExceptionHandler error responses")
class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  private ServerWebExchange exchange;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
    exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/credits/1").build());
  }

  @Test
  @DisplayName("Handles credit not found with a 404 status")
  void handlesCreditNotFoundWithNotFoundStatus() {
    // Arrange
    CreditNotFoundException exception = new CreditNotFoundException("Credit not found");

    // Act
    TestObserver<ErrorResponse> observer = handler.handleNotFound(exception, exchange).test();

    // Assert
    observer.assertNoErrors();
    observer.assertValueCount(1);
    ErrorResponse response = observer.values().get(0);
    assertThat(response.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(response.error()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    assertThat(response.message()).isEqualTo("Credit not found");
    assertThat(response.path()).isEqualTo("/api/v1/credits/1");
    assertThat(response.timestamp()).isNotNull();
    observer.assertComplete();
  }

  @Test
  @DisplayName("Handles customer not found with a 404 status")
  void handlesCustomerNotFoundWithNotFoundStatus() {
    // Arrange
    CustomerNotFoundException exception = new CustomerNotFoundException("Customer not found");

    // Act
    TestObserver<ErrorResponse> observer = handler.handleNotFound(exception, exchange).test();

    // Assert
    observer.assertNoErrors();
    ErrorResponse response = observer.values().get(0);
    assertThat(response.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(response.message()).isEqualTo("Customer not found");
    observer.assertComplete();
  }

  @Test
  @DisplayName("Handles a duplicate credit with a 409 status")
  void handlesDuplicateCreditWithConflictStatus() {
    // Arrange
    DuplicateCreditException exception =
            new DuplicateCreditException("Credit number already exists");

    // Act
    TestObserver<ErrorResponse> observer = handler.handleDuplicate(exception, exchange).test();

    // Assert
    observer.assertNoErrors();
    ErrorResponse response = observer.values().get(0);
    assertThat(response.status()).isEqualTo(HttpStatus.CONFLICT.value());
    assertThat(response.error()).isEqualTo(HttpStatus.CONFLICT.getReasonPhrase());
    assertThat(response.message()).isEqualTo("Credit number already exists");
    observer.assertComplete();
  }

  @Test
  @DisplayName("Handles a business rule violation with a 400 status")
  void handlesBusinessRuleViolationWithBadRequestStatus() {
    // Arrange
    BusinessRuleException exception = new BusinessRuleException("Insufficient available limit");

    // Act
    TestObserver<ErrorResponse> observer = handler.handleBusinessRule(exception, exchange).test();

    // Assert
    observer.assertNoErrors();
    ErrorResponse response = observer.values().get(0);
    assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(response.message()).isEqualTo("Insufficient available limit");
    observer.assertComplete();
  }

  @Test
  @DisplayName("Handles validation errors joining their messages")
  void handlesValidationErrorsJoiningMessages() {
    // Arrange
    BeanPropertyBindingResult bindingResult =
            new BeanPropertyBindingResult(new Object(), "creditRequest");
    bindingResult.reject("invalid.customerId", "customerId is required");
    bindingResult.reject("invalid.currency", "currency is required");
    WebExchangeBindException exception = mock(WebExchangeBindException.class);
    when(exception.getBindingResult()).thenReturn(bindingResult);

    // Act
    TestObserver<ErrorResponse> observer =
            handler.handleValidationErrors(exception, exchange).test();

    // Assert
    observer.assertNoErrors();
    ErrorResponse response = observer.values().get(0);
    assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(response.message()).isEqualTo("customerId is required; currency is required");
    observer.assertComplete();
  }

  @Test
  @DisplayName("Handles an unexpected exception with a 500 status")
  void handlesUnexpectedExceptionWithInternalServerErrorStatus() {
    // Arrange
    Exception exception = new IllegalStateException("Unexpected failure");

    // Act
    TestObserver<ErrorResponse> observer =
            handler.handleGenericException(exception, exchange).test();

    // Assert
    observer.assertNoErrors();
    ErrorResponse response = observer.values().get(0);
    assertThat(response.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(response.message()).isEqualTo("An unexpected internal server error occurred.");
    observer.assertComplete();
  }
}
