package storage;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * VaultStorage manages the lifecycle and inventory of the encrypted vault
 * directory used by StealthVault.
 * <p>
 * Responsibilities include:
 * <ul>
 *   <li>Initialising the vault directory on first launch
 *       ({@link #initializeVault()}).</li>
 *   <li>Checking whether the vault already exists
 *       ({@link #vaultExists()}).</li>
 *   <li>Listing every stored item
 *       ({@link #listStoredItems()}).</li>
 *   <li>Deleting individual items
 *       ({@link #deleteItem(String)}).</li>
 *   <li>Clearing the entire vault
 *       ({@link #clearVault()}).</li>
 *   <li>Reporting vault statistics
 *       ({@link #getVaultSizeBytes()}, {@link #getItemCount()}).</li>
 * </ul>
 * Only {@code java.io} and {@code java.nio} APIs are used.
 * </p>
 *
 * @author StealthVault Team
 * @version 1.0
 */
public class VaultStorage {

    /** Relative path to the vault data directory. */
    private static final String VAULT_DIR = "data" + File.separator + "vault";

    /** The resolved {@link Path} to the vault directory. */
    private final Path vaultPath;

    // ---------------------------------------------------------------
    //  Constructors
    // ---------------------------------------------------------------

    /**
     * Creates a {@code VaultStorage} instance that operates on the default
     * vault directory ({@value #VAULT_DIR}).
     */
    public VaultStorage() {
        this.vaultPath = Paths.get(VAULT_DIR);
    }

    /**
     * Creates a {@code VaultStorage} instance that operates on a custom
     * vault directory.  Useful for testing or when the vault must live
     * at a non-default location.
     *
     * @param customVaultPath the directory to use as the vault root;
     *                        must not be {@code null}.
     * @throws IllegalArgumentException if {@code customVaultPath} is {@code null}.
     */
    public VaultStorage(String customVaultPath) {
        if (customVaultPath == null || customVaultPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Custom vault path must not be null or empty.");
        }
        this.vaultPath = Paths.get(customVaultPath);
    }

    // ---------------------------------------------------------------
    //  Vault lifecycle
    // ---------------------------------------------------------------

    /**
     * Checks whether the vault directory already exists on disk.
     *
     * @return {@code true} if the vault directory exists and is a directory;
     *         {@code false} otherwise.
     */
    public boolean vaultExists() {
        File vaultDir = vaultPath.toFile();
        return vaultDir.exists() && vaultDir.isDirectory();
    }

    /**
     * Initialises the vault directory structure.
     * <p>
     * If the vault already exists this method is a safe no-op and returns
     * {@code true}.  On first launch the directory (and any missing parents)
     * will be created automatically.  A hidden marker file
     * ({@code .vault_initialized}) is placed inside the directory so that
     * other components can verify the vault was properly set up.
     * </p>
     *
     * @return {@code true} if the vault is ready for use after this call;
     *         {@code false} if creation failed.
     */
    public boolean initializeVault() {
        if (vaultExists()) {
            System.out.println("[VaultStorage] Vault already exists at: " + vaultPath);
            return true;
        }

        File vaultDir = vaultPath.toFile();
        if (!vaultDir.mkdirs()) {
            System.err.println("[VaultStorage] ERROR: Failed to create vault directory at: " + vaultPath);
            return false;
        }

        // Write a small marker file so that we can distinguish a properly
        // initialised vault from a manually created empty folder.
        File marker = new File(vaultDir, ".vault_initialized");
        try {
            if (marker.createNewFile()) {
                Files.write(marker.toPath(),
                        ("Vault initialized on: " + System.currentTimeMillis() + "\n").getBytes());
            }
        } catch (IOException e) {
            // Non-fatal — the vault itself was created successfully.
            System.err.println("[VaultStorage] WARNING: Could not write vault marker file.");
            System.err.println("  Detail: " + e.getMessage());
        }

        System.out.println("[VaultStorage] Vault initialized successfully at: " + vaultPath);
        return true;
    }

    // ---------------------------------------------------------------
    //  Item inventory
    // ---------------------------------------------------------------

    /**
     * Returns an unmodifiable list of all file names stored in the vault.
     * <p>
     * Hidden files (names starting with {@code .}) are excluded from the
     * listing.  If the vault does not exist, an empty list is returned.
     * </p>
     *
     * @return a {@link List} of file name strings; never {@code null}.
     */
    public List<String> listStoredItems() {
        if (!vaultExists()) {
            System.err.println("[VaultStorage] Vault does not exist. Returning empty list.");
            return Collections.emptyList();
        }

        List<String> items = new ArrayList<>();
        DirectoryStream<Path> stream = null;

        try {
            stream = Files.newDirectoryStream(vaultPath);
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                // Skip hidden/internal files
                if (!name.startsWith(".")) {
                    items.add(name);
                }
            }
        } catch (IOException e) {
            System.err.println("[VaultStorage] ERROR: Could not list vault contents.");
            System.err.println("  Detail: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                    // Intentionally swallowed.
                }
            }
        }

        Collections.sort(items);
        return Collections.unmodifiableList(items);
    }

    /**
     * Returns the number of user-visible items currently in the vault.
     *
     * @return the item count (hidden files excluded), or {@code 0} if the
     *         vault does not exist.
     */
    public int getItemCount() {
        return listStoredItems().size();
    }

    /**
     * Checks whether a specific item exists in the vault.
     *
     * @param filename the filename to look for; must not be {@code null}.
     * @return {@code true} if the file exists and is a regular file;
     *         {@code false} otherwise.
     */
    public boolean itemExists(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        if (containsPathTraversal(filename)) {
            System.err.println("[VaultStorage] ERROR: Filename contains illegal path traversal characters.");
            return false;
        }
        File target = vaultPath.resolve(filename).normalize().toFile();
        return target.exists() && target.isFile();
    }

    // ---------------------------------------------------------------
    //  Deletion
    // ---------------------------------------------------------------

    /**
     * Deletes a single item from the vault.
     *
     * @param filename the name of the file to delete (relative to the vault
     *                 directory). Must not be {@code null}, empty, or contain
     *                 path traversal characters.
     * @return {@code true} if the file was deleted successfully;
     *         {@code false} if the file did not exist, could not be deleted,
     *         or the filename was invalid.
     */
    public boolean deleteItem(String filename) {
        // --- Input validation ---------------------------------------------------
        if (filename == null || filename.trim().isEmpty()) {
            System.err.println("[VaultStorage] ERROR: Filename must not be null or empty.");
            return false;
        }
        if (containsPathTraversal(filename)) {
            System.err.println("[VaultStorage] ERROR: Filename contains illegal path traversal characters.");
            return false;
        }

        Path targetPath = vaultPath.resolve(filename).normalize();

        // Ensure resolved path is still inside the vault
        if (!targetPath.startsWith(vaultPath.normalize())) {
            System.err.println("[VaultStorage] ERROR: Resolved path escapes the vault directory.");
            return false;
        }

        File targetFile = targetPath.toFile();

        if (!targetFile.exists()) {
            System.err.println("[VaultStorage] ERROR: File not found in vault: " + filename);
            return false;
        }
        if (!targetFile.isFile()) {
            System.err.println("[VaultStorage] ERROR: Path is not a regular file: " + filename);
            return false;
        }

        if (targetFile.delete()) {
            System.out.println("[VaultStorage] Deleted item: " + filename);
            return true;
        } else {
            System.err.println("[VaultStorage] ERROR: Failed to delete item: " + filename);
            return false;
        }
    }

    /**
     * Deletes <b>all</b> user-visible files from the vault directory.
     * <p>
     * Hidden/internal files (e.g. {@code .vault_initialized}) are preserved.
     * The vault directory itself is <em>not</em> removed.
     * </p>
     *
     * @return the number of files that were successfully deleted.
     */
    public int clearVault() {
        if (!vaultExists()) {
            System.err.println("[VaultStorage] Vault does not exist. Nothing to clear.");
            return 0;
        }

        int deletedCount = 0;
        File vaultDir = vaultPath.toFile();
        File[] files = vaultDir.listFiles();

        if (files == null) {
            System.err.println("[VaultStorage] ERROR: Could not list vault directory for clearing.");
            return 0;
        }

        for (File file : files) {
            if (file.isFile() && !file.getName().startsWith(".")) {
                if (file.delete()) {
                    deletedCount++;
                } else {
                    System.err.println("[VaultStorage] WARNING: Could not delete: " + file.getName());
                }
            }
        }

        System.out.println("[VaultStorage] Cleared vault. Deleted " + deletedCount + " item(s).");
        return deletedCount;
    }

    // ---------------------------------------------------------------
    //  Statistics
    // ---------------------------------------------------------------

    /**
     * Calculates the total size (in bytes) of all user-visible files in the
     * vault.
     *
     * @return total size in bytes, or {@code 0} if the vault is empty or does
     *         not exist.
     */
    public long getVaultSizeBytes() {
        if (!vaultExists()) {
            return 0L;
        }

        long totalSize = 0L;
        File vaultDir = vaultPath.toFile();
        File[] files = vaultDir.listFiles();

        if (files == null) {
            return 0L;
        }

        for (File file : files) {
            if (file.isFile() && !file.getName().startsWith(".")) {
                totalSize += file.length();
            }
        }
        return totalSize;
    }

    /**
     * Returns a human-readable summary of the vault's current state.
     *
     * @return a status string describing whether the vault exists, how many
     *         items it holds, and its total size.
     */
    public String getVaultStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== StealthVault Status ===\n");

        if (!vaultExists()) {
            sb.append("  Vault: NOT INITIALIZED\n");
            sb.append("  Path : ").append(vaultPath.toAbsolutePath()).append("\n");
            sb.append("===========================");
            return sb.toString();
        }

        int count = getItemCount();
        long sizeBytes = getVaultSizeBytes();

        sb.append("  Vault: ACTIVE\n");
        sb.append("  Path : ").append(vaultPath.toAbsolutePath()).append("\n");
        sb.append("  Items: ").append(count).append("\n");
        sb.append("  Size : ").append(formatBytes(sizeBytes)).append("\n");
        sb.append("===========================");
        return sb.toString();
    }

    /**
     * Returns the vault directory path.
     *
     * @return the vault {@link Path}.
     */
    public Path getVaultPath() {
        return vaultPath;
    }

    // ---------------------------------------------------------------
    //  Internal helpers
    // ---------------------------------------------------------------

    /**
     * Checks whether the supplied filename contains path-traversal sequences.
     *
     * @param filename the filename to check.
     * @return {@code true} if the filename contains {@code ..} or is absolute.
     */
    private boolean containsPathTraversal(String filename) {
        if (filename.contains("..")) {
            return true;
        }
        Path p = Paths.get(filename);
        return p.isAbsolute();
    }

    /**
     * Formats a byte count into a human-readable string
     * (e.g. {@code "1.23 KB"}, {@code "4.56 MB"}).
     *
     * @param bytes the byte count.
     * @return the formatted string.
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.2f KB", kb);
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format("%.2f MB", mb);
        }
        double gb = mb / 1024.0;
        return String.format("%.2f GB", gb);
    }
}
