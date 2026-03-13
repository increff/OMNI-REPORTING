package com.increff.omni.reporting.util;

import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Log4j2
@Component
public class EncryptionDecryptionUtil {

    @Value("${encryption.algorithm}")
    private String algorithm;

    @Value("${encryption.gcm.iv.length}")
    private int gcmIvLength;

    @Value("${encryption.gcm.tag.length}")
    private int gcmTagLength;

    @Value("${encryption.key}")
    private String encryptionKeyString;

    private final SecureRandom secureRandom = new SecureRandom();


    public String encrypt(String plainText) throws ApiException {
        try {
            byte[] iv = new byte[gcmIvLength];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(algorithm);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(gcmTagLength, iv);
            SecretKey secretKey = getSecretKey();
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);
            String encryptedString = Base64.getUrlEncoder().withoutPadding().encodeToString(byteBuffer.array());

            log.debug("Data encrypted successfully. Original length: {}, Encrypted length: {}",
                plainText.length(), encryptedString.length());

            return encryptedString;

        } catch (Exception e) {
            log.error("Failed to encrypt data", e);
            throw new ApiException(ApiStatus.UNKNOWN_ERROR,
                "Failed to encrypt data: " + e.getMessage());
        }
    }

    public String decrypt(String encryptedData) throws ApiException {
        try {
            byte[] decodedData = Base64.getUrlDecoder().decode(encryptedData);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedData);
            byte[] iv = new byte[gcmIvLength];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);
            Cipher cipher = Cipher.getInstance(algorithm);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(gcmTagLength, iv);
            SecretKey secretKey = getSecretKey();
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            byte[] decryptedData = cipher.doFinal(cipherText);

            String plainText = new String(decryptedData, StandardCharsets.UTF_8);

            log.debug("Data decrypted successfully. Encrypted length: {}, Decrypted length: {}",
                encryptedData.length(), plainText.length());

            return plainText;

        } catch (Exception e) {
            log.error("Failed to decrypt data", e);
            throw new ApiException(ApiStatus.BAD_DATA,
                "Failed to decrypt data. Data may be invalid or tampered with.");
        }
    }

    private SecretKey getSecretKey() throws Exception {
        byte[] keyBytes = encryptionKeyString.getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32];

        System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 32));

        return new SecretKeySpec(key, "AES");
    }
}