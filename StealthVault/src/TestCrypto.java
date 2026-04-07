import crypto.KeyManager;
import crypto.AESEncryption;

import javax.crypto.SecretKey;
import java.util.Base64;

public class TestCrypto {
    public static void main(String[] args) {
        try {
            System.out.println("--- Testing KeyManager & AESEncryption ---\n");
            
            // 1. Define a master password
            String masterPassword = "MySuperSecretPassword123!";
            System.out.println("1. Master Password: " + masterPassword);
            
            // 2. Generate a secure salt (simulating generating it for a new user)
            String base64Salt = KeyManager.generateBase64Salt();
            byte[] salt = Base64.getDecoder().decode(base64Salt);
            System.out.println("2. User Salt (Base64): " + base64Salt);
            
            // 3. Derive the AES-256 Key
            SecretKey secretKey = KeyManager.generateKeyFromPassword(masterPassword, salt);
            System.out.println("3. Derived AES Key (Base64): " + KeyManager.keyToBase64(secretKey));
            
            // 4. Define data to encrypt
            String originalMessage = "This is highly classified vault data.";
            System.out.println("\n[-] Original Message: " + originalMessage);
            
            // 5. Encrypt data
            // Note: AESEncryption returns a Base64-encoded string containing both IV and ciphertext.
            String encryptedBase64 = AESEncryption.encrypt(originalMessage, secretKey);
            System.out.println("[-] Encrypted Data (Base64): " + encryptedBase64);
            
            // 6. Decrypt data
            // Note: Our decrypt method expects the raw bytes of the encrypted data
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
            String decryptedMessage = AESEncryption.decrypt(encryptedBytes, secretKey);
            System.out.println("[-] Decrypted Message: " + decryptedMessage);
            
            // Validate
            if (originalMessage.equals(decryptedMessage)) {
                System.out.println("\nSUCCESS: The decrypted message matches the original!");
            } else {
                System.out.println("\nERROR: Decryption mismatch.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
