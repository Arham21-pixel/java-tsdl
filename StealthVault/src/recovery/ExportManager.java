package recovery;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ExportManager handles exporting the entire vault as an encrypted
 * {@code .bak} backup file and importing it back.
 * <p>
 * <b>Export flow:</b>
 * <ol>
 *   <li>Scan the vault directory for all stored items.</li>
 *   <li>Pack every file into a single binary stream using a simple
 *       custom container format (filename length, filename bytes,
 *       data length, data bytes — repeated for each file).</li>
 *   <li>XOR-encrypt the packed stream with a key derived from a
 *       random salt + a user-supplied passphrase (SHA-256 stretch).</li>
 *   <li>Write the result to a {@code .bak} file whose name contains
 *       a human-readable timestamp.</li>
 * </ol>
 * <p>
 * <b>Import flow:</b>
 * <ol>
 *   <li>Read the {@code .bak} file.</li>
 *   <li>Extract the salt, decrypt the payload with the user passphrase.</li>
 *   <li>Unpack the container and restore every file into the vault.</li>
 * </ol>
 * <p>
 * <b>Backup file binary layout:</b>
 * <pre>
 *   [8  bytes] magic: "STVBAK01"
 *   [16 bytes] random salt
 *   [4  bytes] encrypted-payload length (big-endian int)
 *   [N  bytes] encrypted payload
 * </pre>
 * <p>
 * <b>Pure Java</b> — only {@code java.io}, {@code java.nio}, and
 * {@code java.security} APIs are used. No external libraries.
 * </p>
 *
 * @author StealthVault Team
 * @version 1.0
 */
public class ExportManager {

    // ---------------------------------------------------------------
    //  Constants
    // ---------------------------------------------------------------

    /** Default vault directory. */
    private static final String VAULT_DIR = "data" + File.separator + "vault";

    /** Default backup output directory. */
    private static final String BACKUP_DIR = "data" + File.separator + "backups";

    /** Magic header written at the start of every .bak file. */
    private static final byte[] MAGIC = "STVBAK01".getBytes();

    /** Salt length in bytes. */
    private static final int SALT_LENGTH = 16;

    /** Timestamp format used in backup filenames. */
    private static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";

    // ---------------------------------------------------------------
    //  Instance state
    // ---------------------------------------------------------------

    /** Resolved vault directory path. */
    private final Path vaultPath;

    /** Resolved backup directory path. */
    private final Path backupPath;

    // ---------------------------------------------------------------
    //  Constructors
    // ---------------------------------------------------------------

    /**
     * Creates an {@code ExportManager} using default directories.
     */
    public ExportManager() {
        this.vaultPath = Paths.get(VAULT_DIR);
        this.backupPath = Paths.get(BACKUP_DIR);
    }

    /**
     * Creates an {@code ExportManager} with custom vault and backup
     * directories.  Useful for testing.
     *
     * @param customVaultDir  the vault directory path.
     * @param customBackupDir the backup output directory path.
     * @throws IllegalArgumentException if either argument is null or empty.
     */
    public ExportManager(String customVaultDir, String customBackupDir) {
        if (customVaultDir == null || customVaultDir.trim().isEmpty()) {
            throw new IllegalArgumentException("Vault directory must not be null or empty.");
        }
        if (customBackupDir == null || customBackupDir.trim().isEmpty()) {
            throw new IllegalArgumentException("Backup directory must not be null or empty.");
        }
        this.vaultPath = Paths.get(customVaultDir);
        this.backupPath = Paths.get(customBackupDir);
    }

    // ---------------------------------------------------------------
    //  Export
    // ---------------------------------------------------------------

    /**
     * Exports the entire vault to an encrypted {@code .bak} file.
     * <p>
     * The backup filename follows the pattern
     * {@code stealth_vault_backup_YYYYMMdd_HHmmss.bak}.
     * </p>
     *
     * @param passphrase the passphrase used to derive the encryption key.
     *                   Must not be {@code null} or empty.
     * @return the absolute path of the created backup file, or {@code null}
     *         if the export failed.
     */
    public String exportVault(String passphrase) {
        // --- Validation ---------------------------------------------------------
        if (passphrase == null || passphrase.trim().isEmpty()) {
            System.err.println("[ExportManager] ERROR: Passphrase must not be null or empty.");
            return null;
        }

        File vaultDir = vaultPath.toFile();
        if (!vaultDir.exists() || !vaultDir.isDirectory()) {
            System.err.println("[ExportManager] ERROR: Vault directory does not exist: " + vaultPath);
            return null;
        }

        // --- Collect all vault files --------------------------------------------
        List<File> vaultFiles = listVaultFiles();
        if (vaultFiles.isEmpty()) {
            System.err.println("[ExportManager] WARNING: Vault is empty. Nothing to export.");
            return null;
        }

        // --- Pack files into a byte stream --------------------------------------
        byte[] packed = packFiles(vaultFiles);
        if (packed == null) {
            System.err.println("[ExportManager] ERROR: Failed to pack vault files.");
            return null;
        }

        // --- Generate salt and derive key ---------------------------------------
        byte[] salt = generateSalt();
        if (salt == null) {
            return null;
        }
        byte[] key = deriveKey(passphrase, salt);
        if (key == null) {
            return null;
        }

        // --- Encrypt the packed data --------------------------------------------
        byte[] encrypted = xorEncrypt(packed, key);

        // --- Ensure backup directory exists -------------------------------------
        File backupDir = backupPath.toFile();
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                System.err.println("[ExportManager] ERROR: Cannot create backup directory: " + backupPath);
                return null;
            }
            System.out.println("[ExportManager] Created backup directory: " + backupPath);
        }

        // --- Build filename with timestamp --------------------------------------
        String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
        String fileName = "stealth_vault_backup_" + timestamp + ".bak";
        File backupFile = new File(backupDir, fileName);

        // --- Write the .bak file ------------------------------------------------
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(backupFile));

            // Magic header (8 bytes)
            bos.write(MAGIC);

            // Salt (16 bytes)
            bos.write(salt);

            // Encrypted payload length (4 bytes, big-endian)
            bos.write(intToBytes(encrypted.length));

            // Encrypted payload
            bos.write(encrypted);

            bos.flush();
            System.out.println("[ExportManager] Vault exported successfully: " + backupFile.getAbsolutePath());
            System.out.println("  Files backed up : " + vaultFiles.size());
            System.out.println("  Backup size     : " + backupFile.length() + " bytes");
            return backupFile.getAbsolutePath();

        } catch (FileNotFoundException e) {
            System.err.println("[ExportManager] ERROR: Cannot create backup file.");
            System.err.println("  Detail: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println("[ExportManager] ERROR: I/O error writing backup file.");
            System.err.println("  Detail: " + e.getMessage());
            return null;
        } finally {
            closeQuietly(bos);
        }
    }

    // ---------------------------------------------------------------
    //  Import
    // ---------------------------------------------------------------

    /**
     * Imports vault data from an encrypted {@code .bak} backup file.
     * <p>
     * Existing files in the vault with the same name will be overwritten.
     * </p>
     *
     * @param backupFilePath the path to the {@code .bak} file.
     * @param passphrase     the passphrase used when the backup was created.
     * @return the number of files successfully restored, or {@code -1} on
     *         error.
     */
    public int importVault(String backupFilePath, String passphrase) {
        // --- Validation ---------------------------------------------------------
        if (backupFilePath == null || backupFilePath.trim().isEmpty()) {
            System.err.println("[ExportManager] ERROR: Backup file path must not be null or empty.");
            return -1;
        }
        if (passphrase == null || passphrase.trim().isEmpty()) {
            System.err.println("[ExportManager] ERROR: Passphrase must not be null or empty.");
            return -1;
        }

        File bakFile = new File(backupFilePath);
        if (!bakFile.exists() || !bakFile.isFile()) {
            System.err.println("[ExportManager] ERROR: Backup file not found: " + backupFilePath);
            return -1;
        }

        // --- Read the entire .bak file ------------------------------------------
        byte[] fileBytes = readFileBytes(bakFile);
        if (fileBytes == null) {
            return -1;
        }

        // --- Validate minimum size: 8 (magic) + 16 (salt) + 4 (length) = 28 ----
        if (fileBytes.length < 28) {
            System.err.println("[ExportManager] ERROR: Backup file is too small or corrupt.");
            return -1;
        }

        // --- Verify magic header ------------------------------------------------
        for (int i = 0; i < MAGIC.length; i++) {
            if (fileBytes[i] != MAGIC[i]) {
                System.err.println("[ExportManager] ERROR: Invalid backup file (bad magic header).");
                return -1;
            }
        }

        // --- Extract salt -------------------------------------------------------
        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy(fileBytes, 8, salt, 0, SALT_LENGTH);

        // --- Extract payload length ---------------------------------------------
        int payloadLength = bytesToInt(fileBytes, 24);
        if (payloadLength < 0 || 28 + payloadLength > fileBytes.length) {
            System.err.println("[ExportManager] ERROR: Corrupt backup — payload length mismatch.");
            return -1;
        }

        // --- Extract encrypted payload ------------------------------------------
        byte[] encrypted = new byte[payloadLength];
        System.arraycopy(fileBytes, 28, encrypted, 0, payloadLength);

        // --- Derive key and decrypt ---------------------------------------------
        byte[] key = deriveKey(passphrase, salt);
        if (key == null) {
            return -1;
        }
        byte[] decrypted = xorEncrypt(encrypted, key);  // XOR is symmetric

        // --- Ensure vault directory exists --------------------------------------
        File vaultDir = vaultPath.toFile();
        if (!vaultDir.exists()) {
            if (!vaultDir.mkdirs()) {
                System.err.println("[ExportManager] ERROR: Cannot create vault directory: " + vaultPath);
                return -1;
            }
        }

        // --- Unpack and restore files -------------------------------------------
        int restoredCount = unpackFiles(decrypted, vaultDir);
        if (restoredCount >= 0) {
            System.out.println("[ExportManager] Vault imported successfully.");
            System.out.println("  Files restored: " + restoredCount);
        }
        return restoredCount;
    }

    // ---------------------------------------------------------------
    //  Listing backups
    // ---------------------------------------------------------------

    /**
     * Lists all available {@code .bak} backup files in the backup directory.
     *
     * @return a list of backup file names, sorted alphabetically; never
     *         {@code null}.
     */
    public List<String> listBackups() {
        List<String> backups = new ArrayList<String>();
        File backupDir = backupPath.toFile();
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            return backups;
        }

        File[] files = backupDir.listFiles();
        if (files == null) {
            return backups;
        }

        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".bak")) {
                backups.add(f.getName());
            }
        }
        java.util.Collections.sort(backups);
        return backups;
    }

    /**
     * Deletes a specific backup file.
     *
     * @param backupFileName the name of the backup file to delete.
     * @return {@code true} if the file was deleted; {@code false} otherwise.
     */
    public boolean deleteBackup(String backupFileName) {
        if (backupFileName == null || backupFileName.trim().isEmpty()) {
            System.err.println("[ExportManager] ERROR: Backup filename must not be null or empty.");
            return false;
        }
        if (backupFileName.contains("..") || Paths.get(backupFileName).isAbsolute()) {
            System.err.println("[ExportManager] ERROR: Invalid backup filename (path traversal blocked).");
            return false;
        }

        File target = backupPath.resolve(backupFileName).normalize().toFile();
        if (!target.exists()) {
            System.err.println("[ExportManager] ERROR: Backup not found: " + backupFileName);
            return false;
        }
        if (target.delete()) {
            System.out.println("[ExportManager] Backup deleted: " + backupFileName);
            return true;
        } else {
            System.err.println("[ExportManager] ERROR: Failed to delete backup: " + backupFileName);
            return false;
        }
    }

    // ---------------------------------------------------------------
    //  Internal — Packing / Unpacking
    // ---------------------------------------------------------------

    /**
     * Packs a list of files into a single byte array using a simple
     * length-prefixed binary container.
     * <p>
     * Format per file:
     * <pre>
     *   [4 bytes] filename length (big-endian)
     *   [N bytes] filename (UTF-8)
     *   [4 bytes] data length (big-endian)
     *   [M bytes] file data
     * </pre>
     *
     * @param files the files to pack.
     * @return the packed byte array, or {@code null} on failure.
     */
    private byte[] packFiles(List<File> files) {
        // First pass: compute total size
        long totalSize = 0;
        List<byte[]> fileDataList = new ArrayList<byte[]>();
        List<byte[]> fileNameBytesList = new ArrayList<byte[]>();

        for (File f : files) {
            byte[] data = readFileBytes(f);
            if (data == null) {
                System.err.println("[ExportManager] WARNING: Skipping unreadable file: " + f.getName());
                continue;
            }
            byte[] nameBytes;
            try {
                nameBytes = f.getName().getBytes("UTF-8");
            } catch (IOException e) {
                System.err.println("[ExportManager] WARNING: Encoding error for: " + f.getName());
                continue;
            }
            fileNameBytesList.add(nameBytes);
            fileDataList.add(data);
            totalSize += 4 + nameBytes.length + 4 + data.length;
        }

        if (totalSize == 0 || totalSize > Integer.MAX_VALUE) {
            System.err.println("[ExportManager] ERROR: Nothing to pack or total exceeds max size.");
            return null;
        }

        // Second pass: write into buffer
        byte[] packed = new byte[(int) totalSize];
        int offset = 0;
        for (int i = 0; i < fileNameBytesList.size(); i++) {
            byte[] nameBytes = fileNameBytesList.get(i);
            byte[] data = fileDataList.get(i);

            // Filename length
            System.arraycopy(intToBytes(nameBytes.length), 0, packed, offset, 4);
            offset += 4;

            // Filename
            System.arraycopy(nameBytes, 0, packed, offset, nameBytes.length);
            offset += nameBytes.length;

            // Data length
            System.arraycopy(intToBytes(data.length), 0, packed, offset, 4);
            offset += 4;

            // Data
            System.arraycopy(data, 0, packed, offset, data.length);
            offset += data.length;
        }

        return packed;
    }

    /**
     * Unpacks a binary container back into individual files and writes
     * them to the target directory.
     *
     * @param packed    the packed byte array.
     * @param targetDir the directory to write restored files to.
     * @return the number of files restored, or {@code -1} on error.
     */
    private int unpackFiles(byte[] packed, File targetDir) {
        int offset = 0;
        int restoredCount = 0;

        try {
            while (offset < packed.length) {
                // --- Read filename length ---
                if (offset + 4 > packed.length) {
                    System.err.println("[ExportManager] ERROR: Corrupt backup data (truncated filename length).");
                    return -1;
                }
                int nameLen = bytesToInt(packed, offset);
                offset += 4;

                if (nameLen <= 0 || nameLen > 1024) {
                    System.err.println("[ExportManager] ERROR: Invalid filename length: " + nameLen
                            + ". Backup may be corrupt or wrong passphrase was used.");
                    return -1;
                }

                // --- Read filename ---
                if (offset + nameLen > packed.length) {
                    System.err.println("[ExportManager] ERROR: Corrupt backup data (truncated filename).");
                    return -1;
                }
                String filename = new String(packed, offset, nameLen, "UTF-8");
                offset += nameLen;

                // --- Validate filename (path traversal check) ---
                if (filename.contains("..") || filename.contains(File.separator)
                        || filename.contains("/") || filename.contains("\\")) {
                    System.err.println("[ExportManager] ERROR: Suspicious filename in backup: " + filename);
                    return -1;
                }

                // --- Read data length ---
                if (offset + 4 > packed.length) {
                    System.err.println("[ExportManager] ERROR: Corrupt backup data (truncated data length).");
                    return -1;
                }
                int dataLen = bytesToInt(packed, offset);
                offset += 4;

                if (dataLen < 0) {
                    System.err.println("[ExportManager] ERROR: Invalid data length: " + dataLen);
                    return -1;
                }

                // --- Read data ---
                if (offset + dataLen > packed.length) {
                    System.err.println("[ExportManager] ERROR: Corrupt backup data (truncated file data).");
                    return -1;
                }
                byte[] fileData = new byte[dataLen];
                System.arraycopy(packed, offset, fileData, 0, dataLen);
                offset += dataLen;

                // --- Write file to vault ---
                File outFile = new File(targetDir, filename);
                BufferedOutputStream bos = null;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(outFile));
                    bos.write(fileData);
                    bos.flush();
                    restoredCount++;
                } catch (IOException e) {
                    System.err.println("[ExportManager] ERROR: Failed to restore file: " + filename);
                    System.err.println("  Detail: " + e.getMessage());
                } finally {
                    closeQuietly(bos);
                }
            }
        } catch (IOException e) {
            System.err.println("[ExportManager] ERROR: Decoding error during unpack.");
            System.err.println("  Detail: " + e.getMessage());
            return -1;
        }

        return restoredCount;
    }

    // ---------------------------------------------------------------
    //  Internal — Encryption helpers
    // ---------------------------------------------------------------

    /**
     * Derives a 256-bit encryption key from a passphrase and salt using
     * iterated SHA-256.
     *
     * @param passphrase the user passphrase.
     * @param salt       the random salt.
     * @return a 32-byte key, or {@code null} on failure.
     */
    private byte[] deriveKey(String passphrase, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Initial hash: salt + passphrase
            md.update(salt);
            md.update(passphrase.getBytes("UTF-8"));
            byte[] key = md.digest();

            // Iterate 10 000 rounds for key stretching
            for (int i = 0; i < 10000; i++) {
                md.reset();
                md.update(key);
                md.update(salt);
                key = md.digest();
            }

            return key;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("[ExportManager] ERROR: SHA-256 not available.");
            return null;
        } catch (IOException e) {
            System.err.println("[ExportManager] ERROR: Encoding error during key derivation.");
            return null;
        }
    }

    /**
     * XOR-encrypts (or decrypts) the data with a repeating key.
     * <p>
     * XOR encryption is symmetric — applying the same key a second time
     * decrypts the ciphertext.
     * </p>
     *
     * @param data the plaintext or ciphertext bytes.
     * @param key  the key bytes (will be cycled if shorter than data).
     * @return the transformed bytes.
     */
    private byte[] xorEncrypt(byte[] data, byte[] key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return result;
    }

    /**
     * Generates a cryptographically secure random salt.
     *
     * @return a {@code byte[]} salt, or {@code null} on failure.
     */
    private byte[] generateSalt() {
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[SALT_LENGTH];
            sr.nextBytes(salt);
            return salt;
        } catch (NoSuchAlgorithmException e) {
            try {
                SecureRandom sr = new SecureRandom();
                byte[] salt = new byte[SALT_LENGTH];
                sr.nextBytes(salt);
                return salt;
            } catch (Exception ex) {
                System.err.println("[ExportManager] ERROR: Cannot generate salt.");
                return null;
            }
        }
    }

    // ---------------------------------------------------------------
    //  Internal — File I/O helpers
    // ---------------------------------------------------------------

    /**
     * Lists all user-visible files in the vault directory.
     *
     * @return a list of {@link File} objects; never {@code null}.
     */
    private List<File> listVaultFiles() {
        List<File> result = new ArrayList<File>();
        File vaultDir = vaultPath.toFile();
        if (!vaultDir.exists() || !vaultDir.isDirectory()) {
            return result;
        }

        File[] files = vaultDir.listFiles();
        if (files == null) {
            return result;
        }

        for (File f : files) {
            if (f.isFile() && !f.getName().startsWith(".")) {
                result.add(f);
            }
        }
        return result;
    }

    /**
     * Reads the entire contents of a file into a byte array.
     *
     * @param file the file to read.
     * @return the file bytes, or {@code null} on error.
     */
    private byte[] readFileBytes(File file) {
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            System.err.println("[ExportManager] ERROR: File too large: " + file.getName());
            return null;
        }

        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            byte[] data = new byte[(int) length];
            int totalRead = 0;
            int remaining = data.length;

            while (remaining > 0) {
                int bytesRead = bis.read(data, totalRead, remaining);
                if (bytesRead == -1) {
                    break;
                }
                totalRead += bytesRead;
                remaining -= bytesRead;
            }

            if (totalRead != data.length) {
                // Partial read — return what we got
                byte[] partial = new byte[totalRead];
                System.arraycopy(data, 0, partial, 0, totalRead);
                return partial;
            }

            return data;

        } catch (FileNotFoundException e) {
            System.err.println("[ExportManager] ERROR: File not found: " + file.getName());
            return null;
        } catch (IOException e) {
            System.err.println("[ExportManager] ERROR: I/O error reading: " + file.getName());
            return null;
        } finally {
            closeQuietly(bis);
        }
    }

    // ---------------------------------------------------------------
    //  Internal — Byte conversion helpers
    // ---------------------------------------------------------------

    /**
     * Converts an {@code int} to a 4-byte big-endian array.
     *
     * @param value the integer value.
     * @return the 4-byte array.
     */
    private byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }

    /**
     * Reads a big-endian {@code int} from a byte array at the given offset.
     *
     * @param data   the byte array.
     * @param offset the starting offset.
     * @return the decoded integer.
     */
    private int bytesToInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24)
                | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8)
                | (data[offset + 3] & 0xFF);
    }

    /**
     * Closes a {@link java.io.Closeable} resource without throwing.
     *
     * @param closeable the resource to close; may be {@code null}.
     */
    private void closeQuietly(java.io.Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
                // Intentionally swallowed.
            }
        }
    }
}
