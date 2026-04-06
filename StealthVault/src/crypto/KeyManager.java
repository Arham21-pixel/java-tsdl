package crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class KeyManager {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH_BITS = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH_BYTES = 16;

    /**
     * Generates a 256-bit AES SecretKey from a master password and salt.
     * @param password Master password
     * @param salt Salt byte array
     * @return SecretKey suitable for AES encryption
     * @throws GeneralSecurityException On cryptographic errors
     */
    public static SecretKey generateKeyFromPassword(String password, byte[] salt) throws GeneralSecurityException {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        if (salt == null || salt.length == 0) {
            throw new IllegalArgumentException("Salt cannot be null or empty.");
        }

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
            SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (GeneralSecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralSecurityException("Failed to generate key from password.", e);
        }
    }

    /**
     * Generates a random cryptographic salt.
     * @return Base64 encoded string representing the generated salt.
     */
    public static String generateBase64Salt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Helper to encode a SecretKey to a Base64 string.
     * @param key The SecretKey
     * @return Base64 encoded key
     */
    public static String keyToBase64(SecretKey key) {
        if (key == null) return null;
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Helper to decode a Base64 string to a SecretKey.
     * @param base64Key Base64 encoded key
     * @return The AES SecretKey
     */
    public static SecretKey base64ToKey(String base64Key) {
        if (base64Key == null || base64Key.isEmpty()) return null;
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}
