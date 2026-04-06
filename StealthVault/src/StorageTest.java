import storage.FileHandler;
import storage.VaultStorage;
import java.util.List;

/**
 * Comprehensive test harness for the StealthVault storage subsystem.
 * Verifies all operations of FileHandler and VaultStorage end-to-end.
 */
public class StorageTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("  StealthVault Storage Subsystem - Tests");
        System.out.println("============================================\n");

        FileHandler fileHandler = new FileHandler();
        VaultStorage vaultStorage = new VaultStorage();

        // --- Test 1: Vault initialization ---
        System.out.println("--- Test 1: Vault Initialization ---");
        assertResult("initializeVault returns true", vaultStorage.initializeVault(), true);
        assertResult("vaultExists after init", vaultStorage.vaultExists(), true);

        // Initialize again (should be a safe no-op)
        assertResult("re-initializeVault is no-op", vaultStorage.initializeVault(), true);

        // --- Test 2: Save encrypted file ---
        System.out.println("\n--- Test 2: Save Encrypted File ---");
        byte[] testData = "Hello StealthVault! This is encrypted content.".getBytes();
        assertResult("saveEncryptedFile (test.enc)",
                fileHandler.saveEncryptedFile("test.enc", testData), true);

        // --- Test 3: Load encrypted file ---
        System.out.println("\n--- Test 3: Load Encrypted File ---");
        byte[] loaded = fileHandler.loadEncryptedFile("test.enc");
        assertResult("loadEncryptedFile not null", loaded != null, true);
        if (loaded != null) {
            assertResult("loadEncryptedFile correct length",
                    loaded.length == testData.length, true);
            assertResult("loadEncryptedFile correct content",
                    new String(loaded).equals(new String(testData)), true);
        }

        // --- Test 4: Save multiple files ---
        System.out.println("\n--- Test 4: Save Multiple Files ---");
        assertResult("save second.enc",
                fileHandler.saveEncryptedFile("second.enc", "Second file data".getBytes()), true);
        assertResult("save third.enc",
                fileHandler.saveEncryptedFile("third.enc", "Third file data".getBytes()), true);

        // --- Test 5: List stored items ---
        System.out.println("\n--- Test 5: List Stored Items ---");
        List<String> items = vaultStorage.listStoredItems();
        System.out.println("  Items found: " + items);
        assertResult("listStoredItems count >= 3", items.size() >= 3, true);
        assertResult("listStoredItems contains test.enc", items.contains("test.enc"), true);
        assertResult("listStoredItems contains second.enc", items.contains("second.enc"), true);
        assertResult("listStoredItems contains third.enc", items.contains("third.enc"), true);

        // --- Test 6: Item count ---
        System.out.println("\n--- Test 6: Item Count ---");
        assertResult("getItemCount >= 3", vaultStorage.getItemCount() >= 3, true);

        // --- Test 7: Item exists ---
        System.out.println("\n--- Test 7: Item Exists ---");
        assertResult("itemExists test.enc", vaultStorage.itemExists("test.enc"), true);
        assertResult("itemExists nonexistent.enc (false)", vaultStorage.itemExists("nonexistent.enc"), false);

        // --- Test 8: Vault size ---
        System.out.println("\n--- Test 8: Vault Size ---");
        long size = vaultStorage.getVaultSizeBytes();
        System.out.println("  Vault size: " + size + " bytes");
        assertResult("getVaultSizeBytes > 0", size > 0, true);

        // --- Test 9: Vault status ---
        System.out.println("\n--- Test 9: Vault Status ---");
        String status = vaultStorage.getVaultStatus();
        System.out.println(status);
        assertResult("getVaultStatus contains ACTIVE", status.contains("ACTIVE"), true);

        // --- Test 10: Delete item ---
        System.out.println("\n--- Test 10: Delete Item ---");
        assertResult("deleteItem second.enc", vaultStorage.deleteItem("second.enc"), true);
        assertResult("itemExists second.enc after delete (false)",
                vaultStorage.itemExists("second.enc"), false);

        // --- Test 11: Delete nonexistent item ---
        System.out.println("\n--- Test 11: Delete Nonexistent Item ---");
        assertResult("deleteItem nonexistent (false)", vaultStorage.deleteItem("nonexistent.enc"), false);

        // --- Test 12: Null/empty input handling ---
        System.out.println("\n--- Test 12: Null/Empty Input Handling ---");
        assertResult("saveEncryptedFile null filename",
                fileHandler.saveEncryptedFile(null, testData), false);
        assertResult("saveEncryptedFile empty filename",
                fileHandler.saveEncryptedFile("", testData), false);
        assertResult("saveEncryptedFile null data",
                fileHandler.saveEncryptedFile("test2.enc", null), false);
        assertResult("loadEncryptedFile null filename",
                fileHandler.loadEncryptedFile(null) == null, true);
        assertResult("loadEncryptedFile empty filename",
                fileHandler.loadEncryptedFile("") == null, true);
        assertResult("deleteItem null", vaultStorage.deleteItem(null), false);
        assertResult("deleteItem empty", vaultStorage.deleteItem(""), false);

        // --- Test 13: Path traversal prevention ---
        System.out.println("\n--- Test 13: Path Traversal Prevention ---");
        assertResult("saveEncryptedFile with '..' blocked",
                fileHandler.saveEncryptedFile("../escape.enc", testData), false);
        assertResult("loadEncryptedFile with '..' blocked",
                fileHandler.loadEncryptedFile("../../etc/passwd") == null, true);
        assertResult("deleteItem with '..' blocked",
                vaultStorage.deleteItem("../escape.enc"), false);

        // --- Test 14: Load nonexistent file ---
        System.out.println("\n--- Test 14: Load Nonexistent File ---");
        assertResult("loadEncryptedFile nonexistent returns null",
                fileHandler.loadEncryptedFile("does_not_exist.enc") == null, true);

        // --- Test 15: Overwrite existing file ---
        System.out.println("\n--- Test 15: Overwrite Existing File ---");
        byte[] newData = "Overwritten content!".getBytes();
        assertResult("overwrite test.enc", fileHandler.saveEncryptedFile("test.enc", newData), true);
        byte[] overwritten = fileHandler.loadEncryptedFile("test.enc");
        assertResult("overwritten content correct",
                overwritten != null && new String(overwritten).equals("Overwritten content!"), true);

        // --- Test 16: Clear vault ---
        System.out.println("\n--- Test 16: Clear Vault ---");
        int deleted = vaultStorage.clearVault();
        System.out.println("  Cleared " + deleted + " item(s).");
        assertResult("clearVault deleted >= 2", deleted >= 2, true);
        assertResult("vault empty after clear", vaultStorage.getItemCount() == 0, true);
        assertResult("vault still exists after clear", vaultStorage.vaultExists(), true);

        // --- Test 17: Empty byte array ---
        System.out.println("\n--- Test 17: Empty Byte Array ---");
        assertResult("save empty byte array",
                fileHandler.saveEncryptedFile("empty.enc", new byte[0]), true);
        byte[] emptyLoaded = fileHandler.loadEncryptedFile("empty.enc");
        assertResult("load empty byte array", emptyLoaded != null && emptyLoaded.length == 0, true);

        // Cleanup
        vaultStorage.clearVault();

        // --- Report ---
        System.out.println("\n============================================");
        System.out.println("  RESULTS: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("============================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertResult(String testName, boolean actual, boolean expected) {
        if (actual == expected) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.out.println("  [FAIL] " + testName + " — expected: " + expected + ", got: " + actual);
            failed++;
        }
    }
}
