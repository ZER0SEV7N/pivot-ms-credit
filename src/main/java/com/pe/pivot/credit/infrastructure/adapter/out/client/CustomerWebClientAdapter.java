package com.pe.pivot.credit.infrastructure.adapter.out.client;

import com.pe.pivot.account.infrastructure.adapter.out.client.dto.CustomerClientResponse;
import com.pe.pivot.credit.domain.model.CustomerSummary;
import com.pe.pivot.credit.domain.port.out.CustomerClientPort;
import io.reactivex.rxjava3.core.Maybe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

/**
 * Output Adapter communicating reactively with ms-customer via non-blocking WebClient.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerWebClientAdapter implements CustomerClientPort {

  private final WebClient customerWebClient;

  @Override
  public Maybe<CustomerSummary> findCustomerById(String customerId){
    log.debug("Calling ms-customer GET /{} for customerId: {}", customerId, customerId);

    // Use WebClient to make a non-blocking GET request to ms-customer
    Mono<CustomerSummary> mono = customerWebClient.get()
            .uri("/{id}", customerId)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
            .bodyToMono(CustomerClientResponse.class)
            .map(this::toDomain)
            .onErrorResume(ex -> {
              log.warn("Customer lookup returned empty or error for ID {}: {}", customerId, ex.getMessage());
              return Mono.empty();
            });

    // Convert the Reactor Mono to RxJava Maybe for compatibility with the domain layer
    return RxJava3Adapter.monoToMaybe(mono);
  }

  /**
   * Maps the CustomerClientResponse DTO to the domain model CustomerSummary.
   *
   * @param dto - the CustomerClientResponse received from ms-customer
   * @return - the corresponding CustomerSummary domain object
   */
  private CustomerSummary toDomain(CustomerClientResponse dto) {
    return CustomerSummary.builder()
            .id(dto.id())
            .customerType(dto.customerType())
            .documentNumber(dto.documentNumber())
            .status(dto.status())
            .build();
  }
}
