package com.pe.pivot.credit.infrastructure.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * Cryptographic component responsible for SHA-256 blind indexing and AES-256-GCM encryption
 * using the secret key injected from HashiCorp Vault (SRP).
 */
@Component
public final class CryptoUtil {

  private static final String AES_ALGORITHM = "AES";
  private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
  private static final String SHA_256_ALGORITHM = "SHA-256";
  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 128;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  // 32-byte key seed for AES-256 encryption at rest
  private final SecretKeySpec secretKeySpec;

  public CryptoUtil(@Value("${aes-secret-key}") String aesSecretKey) {
    if (aesSecretKey == null || aesSecretKey.isBlank()) {
      throw new IllegalStateException(
              "The AES secret key is missing. Configure aes-secret-key in HashiCorp Vault.");
    }
    this.secretKeySpec = buildAes256Key(aesSecretKey);
  }

  // TODO: In Working
  private SecretKeySpec buildAes256Key(String aesSecretKey) {
    return null;
  }
}
