package com.pe.pivot.credit.infrastructure.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

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

  /**
   * Injects the AES-256 secret key directly from HashiCorp Vault (secret/pivot-bank).
   *
   * @param aesSecretKey - secret key stored in Vault
   * @throws IllegalStateException if the secret key is missing or invalid
   */
  public CryptoUtil(@Value("${aes-secret-key}") String aesSecretKey) {
    if (aesSecretKey == null || aesSecretKey.isBlank()) {
      throw new IllegalStateException("The AES secret key is missing. Configure aes-secret-key in HashiCorp Vault.");
    }
    this.secretKeySpec = buildAes256Key(aesSecretKey);
  }

  public String hashSha256(String input) {
    // Hashes the input using SHA-256 and returns the hex representation
    if(input == null) {
      return null;
    }

    try{
      // Create a SHA-256 MessageDigest instance
      MessageDigest digest = MessageDigest.getInstance(SHA_256_ALGORITHM);
      byte[] encodedHash = digest.digest(input.trim().getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(encodedHash);
    } catch (Exception ex) {
      throw new IllegalStateException("Error hashing data with SHA-256", ex);
    }
  }

  /**
   * Encrypts the given plain text using AES-256-GCM and returns the Base64-encoded result.
   *
   * @param plainText - the sensitive data to encrypt
   * @return - the AES-256-GCM encrypted string in Base64 format
   * @throws IllegalStateException if encryption fails
   */
  public String encryptAes256(String plainText) {
    // Encrypts the input using AES-256-GCM and returns the Base64 representation
    if(plainText == null ) {
      return null;
    }

    // Encrypt the input using AES-256-GCM and return the Base64 representation
    try{
      byte[] iv = new byte[GCM_IV_LENGTH];
      SECURE_RANDOM.nextBytes(iv);

      // Create a Cipher instance for AES-256-GCM encryption
      Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
      cipher.init(Cipher.ENCRYPT_MODE, this.secretKeySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

      // Encrypt the plain text and concatenate the IV with the encrypted bytes
      byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
      byte[] payload = new byte[iv.length + encryptedBytes.length];

      // Copy the IV and encrypted bytes into the payload
      System.arraycopy(iv, 0, payload, 0, iv.length);
      System.arraycopy(encryptedBytes, 0, payload, iv.length, encryptedBytes.length);

      // Return the Base64-encoded payload
      return Base64.getEncoder().encodeToString(payload);
    } catch (Exception ex) {
      throw new IllegalStateException("Error encrypting data with AES-256-GCM", ex);
    }
  }

  /**
   * Decrypts the given Base64-encoded AES-256-GCM encrypted string and returns the original plain text.
   *
   * @param cipherText - the Base64-encoded AES-256-GCM encrypted string
   * @return - the original plain text
   * @throws IllegalStateException if decryption fails
   */
  public String decryptAes256(String cipherText) {
    // Decrypts the input using AES-256-GCM and returns the original plain text
    if (cipherText == null) {
      return null;
    }

    try{
      // Decode the Base64-encoded cipher text to get the payload
      byte[] payload = Base64.getDecoder().decode(cipherText);
      if (payload.length <= GCM_IV_LENGTH) {
        throw new IllegalArgumentException("Invalid encrypted payload");
      }

      // Extract the IV and encrypted bytes from the payload
      byte[] iv = Arrays.copyOfRange(payload, 0, GCM_IV_LENGTH);
      byte[] encryptedBytes = Arrays.copyOfRange(payload, GCM_IV_LENGTH, payload.length);

      // Create a Cipher instance for AES-256-GCM decryption
      Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
      cipher.init(Cipher.DECRYPT_MODE, this.secretKeySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

      // Decrypt the encrypted bytes and return the original plain text
      byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
      return new String(decryptedBytes, StandardCharsets.UTF_8);
    } catch (Exception ex) {
      throw new IllegalStateException("Error decrypting data with AES-256-GCM", ex);
    }
  }

  /**
   * Builds a SecretKeySpec for AES-256 encryption using the provided raw key.
   *
   * @param rawKey - the raw key string
   * @return - a SecretKeySpec suitable for AES-256 encryption
   * @throws IllegalStateException if the key cannot be generated
   */
  private SecretKeySpec buildAes256Key(String rawKey) {
    try{
      MessageDigest sha = MessageDigest.getInstance(SHA_256_ALGORITHM);
      byte[] keyBytes = sha.digest(rawKey.getBytes(StandardCharsets.UTF_8));
      keyBytes = Arrays.copyOf(keyBytes, 32); // Ensure the key is 32 bytes for AES-256
      return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("Error initializing AES-256 key", ex);
    }
  }

}
