package com.pe.pivot.credit.infrastructure.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CryptoUtil}.
 */
@DisplayName("Should hash and encrypt credit data")
class CryptoUtilTest {

  private static final String SECRET_KEY = "unit-test-aes-secret-key";

  private CryptoUtil cryptoUtil;

  @BeforeEach
  void setUp() {
    cryptoUtil = new CryptoUtil(SECRET_KEY);
  }

  @Test
  @DisplayName("Should reject a null secret key")
  void rejectsNullSecretKey() {
    // Act / Assert
    assertThatThrownBy(() -> new CryptoUtil(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("secret key is missing");
  }

  @Test
  @DisplayName("Should reject a blank secret key")
  void rejectsBlankSecretKey() {
    // Act / Assert
    assertThatThrownBy(() -> new CryptoUtil("   "))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("secret key is missing");
  }

  @Test
  @DisplayName("Should return a null hash for null input")
  void returnsNullHashForNullInput() {
    // Act
    String hash = cryptoUtil.hashSha256(null);

    // Assert
    assertThat(hash).isNull();
  }

  @Test
  @DisplayName("Should compute a known SHA-256 digest")
  void computesKnownSha256Digest() {
    // Act
    String hash = cryptoUtil.hashSha256("abc");

    // Assert
    assertThat(hash)
            .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
  }

  @Test
  @DisplayName("Should trim the input before hashing")
  void trimsInputBeforeHashing() {
    // Act
    String padded = cryptoUtil.hashSha256("  abc  ");
    String trimmed = cryptoUtil.hashSha256("abc");

    // Assert
    assertThat(padded).isEqualTo(trimmed);
  }

  @Test
  @DisplayName("Should return null encryption for null input")
  void returnsNullEncryptionForNullInput() {
    // Act
    String encrypted = cryptoUtil.encryptAes256(null);

    // Assert
    assertThat(encrypted).isNull();
  }

  @Test
  @DisplayName("Should return null decryption for null input")
  void returnsNullDecryptionForNullInput() {
    // Act
    String decrypted = cryptoUtil.decryptAes256(null);

    // Assert
    assertThat(decrypted).isNull();
  }

  @Test
  @DisplayName("Should encrypt and decrypt a round trip")
  void encryptsAndDecryptsRoundTrip() {
    // Arrange
    String plainText = "LN-1234567890";

    // Act
    String encrypted = cryptoUtil.encryptAes256(plainText);
    String decrypted = cryptoUtil.decryptAes256(encrypted);

    // Assert
    assertThat(encrypted).isNotEqualTo(plainText);
    assertThat(decrypted).isEqualTo(plainText);
  }

  @Test
  @DisplayName("Should produce different cipher texts for the same plain text")
  void producesDifferentCipherTextsForSamePlainText() {
    // Arrange
    String plainText = "4557-8800-1234-5678";

    // Act
    String first = cryptoUtil.encryptAes256(plainText);
    String second = cryptoUtil.encryptAes256(plainText);

    // Assert
    assertThat(first).isNotEqualTo(second);
    assertThat(cryptoUtil.decryptAes256(first)).isEqualTo(plainText);
    assertThat(cryptoUtil.decryptAes256(second)).isEqualTo(plainText);
  }

  @Test
  @DisplayName("Should reject cipher text that is not Base64")
  void rejectsNonBase64CipherText() {
    // Act / Assert
    assertThatThrownBy(() -> cryptoUtil.decryptAes256("not-base64!!"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Error decrypting data");
  }

  @Test
  @DisplayName("Should reject a payload shorter than the initialization vector")
  void rejectsPayloadShorterThanInitializationVector() {
    // Arrange
    String shortPayload = Base64.getEncoder().encodeToString(new byte[5]);

    // Act / Assert
    assertThatThrownBy(() -> cryptoUtil.decryptAes256(shortPayload))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Error decrypting data");
  }

  @Test
  @DisplayName("Should reject a tampered cipher text")
  void rejectsTamperedCipherText() {
    // Arrange
    String encrypted = cryptoUtil.encryptAes256("sensitive-value");
    byte[] payload = Base64.getDecoder().decode(encrypted);
    payload[payload.length - 1] ^= 0x01;
    String tampered = Base64.getEncoder().encodeToString(payload);

    // Act / Assert
    assertThatThrownBy(() -> cryptoUtil.decryptAes256(tampered))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Error decrypting data");
  }
}
