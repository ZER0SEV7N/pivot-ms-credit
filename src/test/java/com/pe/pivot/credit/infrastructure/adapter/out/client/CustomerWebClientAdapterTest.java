package com.pe.pivot.credit.infrastructure.adapter.out.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.pe.pivot.credit.domain.model.CustomerSummary;
import io.reactivex.rxjava3.observers.TestObserver;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link CustomerWebClientAdapter}.
 */
@DisplayName("Should look up customers reactively")
class CustomerWebClientAdapterTest {

  private static final String BASE_URL = "http://localhost:8081/api/v1/customers";

  private static final String CUSTOMER_JSON = """
      {
        "id": "cust-1",
        "customerType": "PERSONAL",
        "profile": "RETAIL",
        "documentType": "DNI",
        "documentNumber": "12345678",
        "status": "ACTIVE"
      }
      """;

  @Test
  @DisplayName("Should map a successful response to a customer summary")
  void mapsSuccessfulResponseToCustomerSummary() {
    // Arrange
    AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
    ExchangeFunction exchangeFunction = request -> {
      capturedRequest.set(request);
      return Mono.just(jsonResponse(HttpStatus.OK, CUSTOMER_JSON));
    };
    CustomerWebClientAdapter adapter = adapterWith(exchangeFunction);

    // Act
    TestObserver<CustomerSummary> observer = adapter.findCustomerById("cust-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertValueCount(1);
    CustomerSummary summary = observer.values().get(0);
    assertThat(summary.getId()).isEqualTo("cust-1");
    assertThat(summary.getCustomerType()).isEqualTo("PERSONAL");
    assertThat(summary.getDocumentNumber()).isEqualTo("12345678");
    assertThat(summary.getStatus()).isEqualTo("ACTIVE");
    observer.assertComplete();
    assertThat(capturedRequest.get().url().toString()).isEqualTo(BASE_URL + "/cust-1");
  }

  @Test
  @DisplayName("Should return empty when the service responds with a 4xx error")
  void returnsEmptyWhenCustomerServiceRespondsWithClientError() {
    // Arrange
    ExchangeFunction exchangeFunction =
            request -> Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
    CustomerWebClientAdapter adapter = adapterWith(exchangeFunction);

    // Act
    TestObserver<CustomerSummary> observer = adapter.findCustomerById("cust-404").test();

    // Assert
    observer.assertNoErrors();
    observer.assertNoValues();
    observer.assertComplete();
  }

  @Test
  @DisplayName("Should return empty when the service responds with a 5xx error")
  void returnsEmptyWhenCustomerServiceRespondsWithServerError() {
    // Arrange
    ExchangeFunction exchangeFunction =
            request -> Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());
    CustomerWebClientAdapter adapter = adapterWith(exchangeFunction);

    // Act
    TestObserver<CustomerSummary> observer = adapter.findCustomerById("cust-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertNoValues();
    observer.assertComplete();
  }

  @Test
  @DisplayName("Should return empty when the customer service is unreachable")
  void returnsEmptyWhenCustomerServiceIsUnreachable() {
    // Arrange
    ExchangeFunction exchangeFunction =
            request -> Mono.error(new IllegalStateException("Connection refused"));
    CustomerWebClientAdapter adapter = adapterWith(exchangeFunction);

    // Act
    TestObserver<CustomerSummary> observer = adapter.findCustomerById("cust-1").test();

    // Assert
    observer.assertNoErrors();
    observer.assertNoValues();
    observer.assertComplete();
  }

  private CustomerWebClientAdapter adapterWith(ExchangeFunction exchangeFunction) {
    WebClient webClient = WebClient.builder()
            .baseUrl(BASE_URL)
            .exchangeFunction(exchangeFunction)
            .build();
    return new CustomerWebClientAdapter(webClient);
  }

  private ClientResponse jsonResponse(HttpStatus status, String body) {
    return ClientResponse.create(status)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(body)
            .build();
  }
}
