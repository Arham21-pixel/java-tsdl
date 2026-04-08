package storage;

import crypto.AESEncryption;
import crypto.KeyManager;
import ui.VaultItem;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * VaultService is the central bridge that connects the UI layer to the
 * underlying crypto and storage layers.
 * 
 * Now fully wired to:
 *   - FileHandler  — for low-level encrypted file I/O
 *   - VaultStorage — for vault directory lifecycle and stats
 *   - KeyManager   — for PBKDF2 key derivation
 *   - AESEncryption — for AES-GCM encrypt/decrypt
 * 
 * Each user gets their own vault file: data/vault/{username}.vault
 * A per-user salt is stored in: data/vault/{username}.salt
 */
public class VaultService {

    private static final String VAULT_DIR = "data" + File.separator + "vault";
    
    private final String username;
    private final SecretKey encryptionKey;
    private final List<VaultItem> items;
    private final FileHandler fileHandler;
    private final VaultStorage vaultStorage;

    /**
     * Returns true if the vault was loaded successfully (or was empty).
     * Returns false if decryption failed (wrong password).
     */
    private boolean decryptionOk = true;

    /**
     * Creates a VaultService for the given user.
     * Derives an AES key from the password and loads existing vault items.
     * Uses VaultStorage to initialize the vault directory and FileHandler
     * for all file I/O operations.
     */
    public VaultService(String username, String masterPassword) throws GeneralSecurityException {
        this.username = username;
        this.items = new ArrayList<>();
        this.fileHandler = new FileHandler();
        this.vaultStorage = new VaultStorage();

        // Initialize vault directory via VaultStorage
        if (!vaultStorage.initializeVault()) {
            throw new GeneralSecurityException("Failed to initialize vault directory");
        }

        // Load or create the user salt
        byte[] salt = loadOrCreateSalt();

        // Derive AES key from master password via KeyManager
        this.encryptionKey = KeyManager.generateKeyFromPassword(masterPassword, salt);

        // Load existing items using FileHandler
        loadItems();
    }

    public boolean isDecryptionOk() {
        return decryptionOk;
    }

    // -------------------------------------------------------------------
    //  Public API
    // -------------------------------------------------------------------

    /** Returns all vault items. */
    public List<VaultItem> getItems() {
        return new ArrayList<>(items);
    }

    /** Adds a new item and persists the vault. */
    public void addItem(VaultItem item) throws GeneralSecurityException {
        items.add(item);
        saveItems();
    }

    /** Removes an item by ID and persists the vault. */
    public boolean removeItem(String itemId) throws GeneralSecurityException {
        boolean removed = items.removeIf(i -> i.getId().equals(itemId));
        if (removed) {
            saveItems();
        }
        return removed;
    }

    /** Updates an item in place and persists the vault. */
    public boolean updateItem(VaultItem updated) throws GeneralSecurityException {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(updated.getId())) {
                items.set(i, updated);
                saveItems();
                return true;
            }
        }
        return false;
    }

    /** Returns items filtered by category. "All" returns everything. */
    public List<VaultItem> getItemsByCategory(String category) {
        if (category == null || category.equals("All")) {
            return getItems();
        }
        List<VaultItem> filtered = new ArrayList<>();
        for (VaultItem item : items) {
            if (category.equals(item.getCategory())) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    /** Returns item count. */
    public int getItemCount() {
        return items.size();
    }

    /** Returns the VaultStorage instance for stats/status queries. */
    public VaultStorage getVaultStorage() {
        return vaultStorage;
    }

    // -------------------------------------------------------------------
    //  Persistence — Encrypted file I/O via FileHandler
    // -------------------------------------------------------------------

    /**
     * Saves all items to the encrypted vault file using FileHandler.
     * Each item is serialized to a line, all lines are joined,
     * the result is AES-GCM encrypted and saved as raw bytes.
     */
    private void saveItems() throws GeneralSecurityException {
        StringBuilder sb = new StringBuilder();
        for (VaultItem item : items) {
            sb.append(item.toStorageLine()).append("\n");
        }

        String plaintext = sb.toString();
        String encryptedBase64 = AESEncryption.encrypt(plaintext, encryptionKey);

        // Decode Base64 to get raw encrypted bytes (IV + ciphertext)
        byte[] rawEncryptedBytes = Base64.getDecoder().decode(encryptedBase64);

        // Write via FileHandler
        if (!fileHandler.saveEncryptedFile(username + ".vault", rawEncryptedBytes)) {
            throw new GeneralSecurityException("FileHandler failed to save vault file");
        }

        System.out.println("[VaultService] Vault saved for user: " + username +
                           " (" + items.size() + " items) via FileHandler");
    }

    /**
     * Loads items from the encrypted vault file using FileHandler.
     * If the file doesn't exist, starts with an empty vault.
     */
    private void loadItems() {
        // Use FileHandler to read raw encrypted bytes
        byte[] rawEncryptedBytes = fileHandler.loadEncryptedFile(username + ".vault");

        if (rawEncryptedBytes == null) {
            // File doesn't exist or couldn't be read — new user, empty vault
            System.out.println("[VaultService] No existing vault for user: " + username);
            return;
        }

        try {
            // Decrypt: raw bytes are IV + ciphertext (AES-GCM format)
            String plaintext = AESEncryption.decrypt(rawEncryptedBytes, encryptionKey);

            // Parse items
            String[] lines = plaintext.split("\n");
            for (String itemLine : lines) {
                if (itemLine.trim().isEmpty()) continue;
                VaultItem item = VaultItem.fromStorageLine(itemLine);
                if (item != null) {
                    items.add(item);
                }
            }

            System.out.println("[VaultService] Loaded " + items.size() +
                               " items for user: " + username + " via FileHandler");

        } catch (GeneralSecurityException e) {
            System.err.println("[VaultService] ERROR decrypting vault (wrong password?): " + e.getMessage());
            decryptionOk = false;
            // Don't crash — but caller should check isDecryptionOk()
        } catch (Exception e) {
            System.err.println("[VaultService] ERROR loading vault: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------
    //  Salt management
    // -------------------------------------------------------------------

    private byte[] loadOrCreateSalt() {
        File saltFile = getSaltFile();
        if (saltFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(saltFile))) {
                String saltBase64 = reader.readLine();
                if (saltBase64 != null && !saltBase64.isEmpty()) {
                    return Base64.getDecoder().decode(saltBase64);
                }
            } catch (Exception e) {
                System.err.println("[VaultService] Error reading salt, generating new one.");
            }
        }

        // Generate new salt via KeyManager
        String saltBase64 = KeyManager.generateBase64Salt();
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saltFile))) {
            writer.write(saltBase64);
        } catch (IOException e) {
            System.err.println("[VaultService] Error saving salt: " + e.getMessage());
        }

        System.out.println("[VaultService] Generated new salt for user: " + username);
        return salt;
    }

    // -------------------------------------------------------------------
    //  File path helpers
    // -------------------------------------------------------------------

    private File getSaltFile() {
        return new File(VAULT_DIR + File.separator + username + ".salt");
    }
}
