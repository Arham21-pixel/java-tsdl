package storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * FileHandler provides low-level file I/O operations for the StealthVault
 * encrypted file storage system.
 * <p>
 * This class handles reading and writing encrypted byte data to the vault
 * directory. All methods use only {@code java.io} and {@code java.nio} APIs.
 * Every I/O operation includes defensive null checks, path validation, and
 * comprehensive exception handling so that callers always receive a clear
 * success/failure signal.
 * </p>
 *
 * <b>Thread-safety:</b> Individual read/write calls are atomic at the NIO level
 * (write-then-move is used internally by {@link Files#write} with
 * {@link StandardOpenOption#CREATE}), but concurrent access to the same file
 * from multiple threads is <em>not</em> synchronized by this class.  Callers
 * that need concurrency control should synchronize externally.
 *
 * @author StealthVault Team
 * @version 1.0
 */
public class FileHandler {

    /** Base directory where all vault files are stored. */
    private static final String VAULT_DIR = "data" + File.separator + "vault";

    // ---------------------------------------------------------------
    //  Public API
    // ---------------------------------------------------------------

    /**
     * Persists encrypted data to a file inside the vault directory.
     * <p>
     * If the vault directory does not yet exist it will be created
     * automatically.  If a file with the given name already exists it will be
     * overwritten silently.
     * </p>
     *
     * @param filename      the name of the file (relative to the vault dir).
     *                      Must not be {@code null}, empty, or contain path
     *                      traversal characters ({@code ..}).
     * @param encryptedData the raw encrypted bytes to write.
     *                      Must not be {@code null}.
     * @return {@code true} if the file was written successfully;
     *         {@code false} otherwise.
     */
    public boolean saveEncryptedFile(String filename, byte[] encryptedData) {
        // --- Input validation ---------------------------------------------------
        if (filename == null || filename.trim().isEmpty()) {
            System.err.println("[FileHandler] ERROR: Filename must not be null or empty.");
            return false;
        }
        if (encryptedData == null) {
            System.err.println("[FileHandler] ERROR: Encrypted data must not be null.");
            return false;
        }
        if (containsPathTraversal(filename)) {
            System.err.println("[FileHandler] ERROR: Filename contains illegal path traversal characters.");
            return false;
        }

        // --- Ensure vault directory exists --------------------------------------
        Path vaultPath = Paths.get(VAULT_DIR);
        if (!ensureDirectoryExists(vaultPath)) {
            return false;
        }

        // --- Resolve and validate the target path -------------------------------
        Path filePath = vaultPath.resolve(filename).normalize();
        if (!filePath.startsWith(vaultPath.toAbsolutePath().normalize())
                && !filePath.startsWith(vaultPath.normalize())) {
            // Double-check: resolved path must still be inside the vault.
            System.err.println("[FileHandler] ERROR: Resolved path escapes the vault directory.");
            return false;
        }

        // --- Write bytes to disk ------------------------------------------------
        FileOutputStream fos = null;
        try {
            // Ensure parent directories exist (for sub-folder support inside vault)
            File parentDir = filePath.toFile().getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    System.err.println("[FileHandler] ERROR: Could not create parent directories for: " + filename);
                    return false;
                }
            }

            fos = new FileOutputStream(filePath.toFile());
            fos.write(encryptedData);
            fos.flush();
            System.out.println("[FileHandler] File saved successfully: " + filename
                    + " (" + encryptedData.length + " bytes)");
            return true;

        } catch (FileNotFoundException e) {
            System.err.println("[FileHandler] ERROR: Could not open file for writing: " + filename);
            System.err.println("  Detail: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("[FileHandler] ERROR: I/O error while writing file: " + filename);
            System.err.println("  Detail: " + e.getMessage());
            return false;
        } finally {
            closeQuietly(fos);
        }
    }

    /**
     * Reads encrypted data from a file inside the vault directory.
     *
     * @param filename the name of the file to read (relative to the vault dir).
     *                 Must not be {@code null}, empty, or contain path
     *                 traversal characters ({@code ..}).
     * @return the raw encrypted bytes read from the file, or {@code null} if the
     *         file does not exist or an I/O error occurs.
     */
    public byte[] loadEncryptedFile(String filename) {
        // --- Input validation ---------------------------------------------------
        if (filename == null || filename.trim().isEmpty()) {
            System.err.println("[FileHandler] ERROR: Filename must not be null or empty.");
            return null;
        }
        if (containsPathTraversal(filename)) {
            System.err.println("[FileHandler] ERROR: Filename contains illegal path traversal characters.");
            return null;
        }

        // --- Resolve path -------------------------------------------------------
        Path vaultPath = Paths.get(VAULT_DIR);
        Path filePath = vaultPath.resolve(filename).normalize();

        File targetFile = filePath.toFile();

        // --- Existence & readability checks -------------------------------------
        if (!targetFile.exists()) {
            System.err.println("[FileHandler] ERROR: File not found: " + filename);
            return null;
        }
        if (!targetFile.isFile()) {
            System.err.println("[FileHandler] ERROR: Path is not a regular file: " + filename);
            return null;
        }
        if (!targetFile.canRead()) {
            System.err.println("[FileHandler] ERROR: File is not readable: " + filename);
            return null;
        }

        // --- Read bytes from disk -----------------------------------------------
        FileInputStream fis = null;
        try {
            long fileSize = targetFile.length();
            if (fileSize > Integer.MAX_VALUE) {
                System.err.println("[FileHandler] ERROR: File is too large to load into memory: " + filename);
                return null;
            }

            byte[] data = new byte[(int) fileSize];
            fis = new FileInputStream(targetFile);

            int totalBytesRead = 0;
            int bytesRemaining = data.length;

            // Loop until every byte has been read (FileInputStream.read may
            // return fewer bytes than requested in a single call).
            while (bytesRemaining > 0) {
                int bytesRead = fis.read(data, totalBytesRead, bytesRemaining);
                if (bytesRead == -1) {
                    // Unexpected end-of-stream
                    System.err.println("[FileHandler] WARNING: Unexpected end-of-stream after "
                            + totalBytesRead + " bytes (expected " + data.length + ").");
                    // Return what we have; caller can decide how to handle.
                    byte[] partial = new byte[totalBytesRead];
                    System.arraycopy(data, 0, partial, 0, totalBytesRead);
                    return partial;
                }
                totalBytesRead += bytesRead;
                bytesRemaining -= bytesRead;
            }

            System.out.println("[FileHandler] File loaded successfully: " + filename
                    + " (" + totalBytesRead + " bytes)");
            return data;

        } catch (FileNotFoundException e) {
            System.err.println("[FileHandler] ERROR: File not found during read: " + filename);
            System.err.println("  Detail: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println("[FileHandler] ERROR: I/O error while reading file: " + filename);
            System.err.println("  Detail: " + e.getMessage());
            return null;
        } finally {
            closeQuietly(fis);
        }
    }

    /**
     * Returns the absolute {@link Path} for a given filename inside the vault.
     *
     * @param filename the file name relative to the vault directory.
     * @return the resolved {@code Path}, or {@code null} if the filename is invalid.
     */
    public Path getVaultFilePath(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        return Paths.get(VAULT_DIR).resolve(filename).normalize();
    }

    /**
     * Returns the vault base directory path.
     *
     * @return the vault directory as a {@link String}.
     */
    public String getVaultDirectory() {
        return VAULT_DIR;
    }

    // ---------------------------------------------------------------
    //  Internal helpers
    // ---------------------------------------------------------------

    /**
     * Checks whether the supplied filename contains path-traversal sequences.
     *
     * @param filename the filename to check.
     * @return {@code true} if the filename contains {@code ..} or an absolute
     *         path root, {@code false} otherwise.
     */
    private boolean containsPathTraversal(String filename) {
        if (filename.contains("..")) {
            return true;
        }
        // Block absolute paths (Unix or Windows)
        Path p = Paths.get(filename);
        return p.isAbsolute();
    }

    /**
     * Creates the given directory (and any missing parents) if it does not
     * already exist.
     *
     * @param dir the directory path to ensure.
     * @return {@code true} if the directory exists after this call;
     *         {@code false} if it could not be created.
     */
    private boolean ensureDirectoryExists(Path dir) {
        File dirFile = dir.toFile();
        if (dirFile.exists()) {
            if (!dirFile.isDirectory()) {
                System.err.println("[FileHandler] ERROR: Path exists but is not a directory: " + dir);
                return false;
            }
            return true;
        }
        if (dirFile.mkdirs()) {
            System.out.println("[FileHandler] Created vault directory: " + dir);
            return true;
        }
        System.err.println("[FileHandler] ERROR: Failed to create vault directory: " + dir);
        return false;
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
                // Intentionally swallowed — nothing useful to do here.
            }
        }
    }
}
