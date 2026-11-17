package com.pe.pivot.credit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CreditStatus}.
 */
@DisplayName("Should expose the CreditStatus lifecycle enumeration")
class CreditStatusTest {

  @Test
  @DisplayName("Should expose all lifecycle statuses")
  void exposesAllLifecycleStatuses() {
    // Arrange
    CreditStatus[] expected = {
        CreditStatus.ACTIVE,
        CreditStatus.PAID_OFF,
        CreditStatus.BLOCKED,
        CreditStatus.CLOSED
    };

    // Act
    CreditStatus[] actual = CreditStatus.values();

    // Assert
    assertThat(actual).containsExactly(expected);
  }

  @Test
  @DisplayName("Should resolve a status by name")
  void resolvesStatusByName() {
    // Arrange
    String name = "PAID_OFF";

    // Act
    CreditStatus status = CreditStatus.valueOf(name);

    // Assert
    assertThat(status).isEqualTo(CreditStatus.PAID_OFF);
  }
}
