package com.pe.pivot.credit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CreditOperationType}.
 */
@DisplayName("CreditOperationType enumeration")
class CreditOperationTypeTest {

  @Test
  @DisplayName("Exposes all supported operation types")
  void exposesAllOperationTypes() {
    // Arrange
    CreditOperationType[] expected = {
        CreditOperationType.PAYMENT,
        CreditOperationType.CONSUMPTION
    };

    // Act
    CreditOperationType[] actual = CreditOperationType.values();

    // Assert
    assertThat(actual).containsExactly(expected);
  }

  @Test
  @DisplayName("Resolves an operation type by name")
  void resolvesOperationTypeByName() {
    // Arrange
    String name = "CONSUMPTION";

    // Act
    CreditOperationType type = CreditOperationType.valueOf(name);

    // Assert
    assertThat(type).isEqualTo(CreditOperationType.CONSUMPTION);
  }
}
