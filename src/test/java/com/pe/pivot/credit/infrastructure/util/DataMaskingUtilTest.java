package com.pe.pivot.credit.infrastructure.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DataMaskingUtil}.
 */
@DisplayName("DataMaskingUtil credit number masking")
class DataMaskingUtilTest {

  @Test
  @DisplayName("Masks a null credit number")
  void masksNullCreditNumber() {
    // Act
    String masked = DataMaskingUtil.maskCreditNumber(null);

    // Assert
    assertThat(masked).isEqualTo("****");
  }

  @Test
  @DisplayName("Masks a credit number shorter than four characters")
  void masksCreditNumberShorterThanFourCharacters() {
    // Act
    String masked = DataMaskingUtil.maskCreditNumber("123");

    // Assert
    assertThat(masked).isEqualTo("****");
  }

  @Test
  @DisplayName("Keeps a four character credit number visible")
  void keepsCreditNumberWithExactlyFourCharactersVisible() {
    // Act
    String masked = DataMaskingUtil.maskCreditNumber("5678");

    // Assert
    assertThat(masked).isEqualTo("5678");
  }

  @Test
  @DisplayName("Masks all but the last four characters")
  void masksAllButLastFourCharacters() {
    // Act
    String masked = DataMaskingUtil.maskCreditNumber("LN-1234567890");

    // Assert
    assertThat(masked).isEqualTo("*********7890");
    assertThat(masked).hasSize(13);
  }
}
