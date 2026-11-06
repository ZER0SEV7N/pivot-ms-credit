package com.pe.pivot.credit.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for setting up WebClient instances to interact with external services.
 * This class defines a WebClient bean for the Customer Service, allowing the application to make HTTP
 */
@Configuration
public class WebClientConfig {

  /**
   * The base URL for the Customer Service, which is injected from the application properties.
   *  If not specified, it defaults to "<a href="http://localhost:8081/api/v1/customers">http://localhost:8081/api/v1/customers</a>".
   */
  @Value("${application.clients.customer-service.url:http://localhost:8081/api/v1/customers}")
  private String customerServiceUrl;

  /**
   * Creates a WebClient bean for interacting with the Customer Service.
   *
   * @return - a configured WebClient instance with the base URL set to the Customer Service endpoint.
   */
  @Bean
  public WebClient customerWebClient() {
    return WebClient.builder()
            .baseUrl(customerServiceUrl)
            .build();
  }
}
