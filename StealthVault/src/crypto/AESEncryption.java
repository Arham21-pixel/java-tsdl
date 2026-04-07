package crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public class AESEncryption {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * Encrypts the plaintext data.
     * @param data The plaintext data
     * @param key The AES SecretKey
     * @return Base64 encoded string containing the IV and the cipher text
     * @throws GeneralSecurityException On cryptographic errors
     */
    public static String encrypt(String data, SecretKey key) throws GeneralSecurityException {
        if (data == null) {
            throw new IllegalArgumentException("Data to encrypt cannot be null.");
        }
        if (key == null) {
            throw new IllegalArgumentException("Encryption key cannot be null.");
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);

            byte[] cipherText = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (GeneralSecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralSecurityException("Encryption process failed.", e);
        }
    }

    /**
     * Decrypts the raw byte array data containing IV + cipher text.
     * @param encryptedData The raw byte array to decrypt
     * @param key The AES SecretKey
     * @return The decrypted plaintext string
     * @throws GeneralSecurityException On cryptographic errors
     */
    public static String decrypt(byte[] encryptedData, SecretKey key) throws GeneralSecurityException {
        if (encryptedData == null || encryptedData.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("Invalid encrypted data format or length.");
        }
        if (key == null) {
            throw new IllegalArgumentException("Decryption key cannot be null.");
        }

        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralSecurityException("Decryption process failed.", e);
        }
    }
}
