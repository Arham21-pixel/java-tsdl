import recovery.SecurityQuestions;
import recovery.ExportManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Comprehensive test harness for the StealthVault Recovery Subsystem.
 * Tests SecurityQuestions setup and verification, and ExportManager
 * backup and restoration.
 */
public class RecoveryTest {

    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("  StealthVault Recovery Subsystem - Tests");
        System.out.println("============================================\n");

        int passed = 0;
        int failed = 0;

        // -------------------------------------------------------------
        // PART 1: SecurityQuestions Testing
        // -------------------------------------------------------------
        System.out.println("--- Part 1: Security Questions ---");
        SecurityQuestions sq = new SecurityQuestions();
        
        // Reset state first
        sq.resetSecurityQuestions();

        boolean setup = sq.setupSecurityQuestions(
            "What is your favorite color?", "Blue",
            "What is your pet's name?", "Fluffy"
        );
        if(setup) { System.out.println("  [PASS] Setup security questions."); passed++; } 
        else { System.out.println("  [FAIL] Setup security questions."); failed++; }

        boolean verifyCorrect = sq.verifyAnswers("blue", "fluffy"); // Testing case-insensitivity
        if(verifyCorrect) { System.out.println("  [PASS] Verified CORRECT answers."); passed++; } 
        else { System.out.println("  [FAIL] Verified CORRECT answers."); failed++; }

        boolean verifyIncorrect = sq.verifyAnswers("red", "fluffy");
        if(!verifyIncorrect) { System.out.println("  [PASS] Rejected INCORRECT answers."); passed++; } 
        else { System.out.println("  [FAIL] Rejected INCORRECT answers."); failed++; }

        // -------------------------------------------------------------
        // PART 2: ExportManager Testing
        // -------------------------------------------------------------
        System.out.println("\n--- Part 2: Export/Import Manager ---");
        ExportManager em = new ExportManager();
        
        // Let's create a dummy file in the vault to export
        File vaultDir = new File("data/vault");
        vaultDir.mkdirs();
        try {
            FileOutputStream fos = new FileOutputStream(new File(vaultDir, "dummy_secret.enc"));
            fos.write("Super secret password data".getBytes());
            fos.close();
            System.out.println("  [INFO] Created dummy file in vault for backup.");
        } catch(IOException e) {
            System.out.println("  [FAIL] Could not create dummy file.");
        }

        // Test Export
        String backupPath = em.exportVault("my_super_secure_passphrase");
        if(backupPath != null) { System.out.println("  [PASS] Exported vault successfully."); passed++; } 
        else { System.out.println("  [FAIL] Exported vault successfully."); failed++; }

        // Break the vault by clearing it
        new File("data/vault/dummy_secret.enc").delete();

        // Test Import
        if (backupPath != null) {
            int restoredCount = em.importVault(backupPath, "my_super_secure_passphrase");
            if(restoredCount > 0) { System.out.println("  [PASS] Imported vault correctly (" + restoredCount + " files)."); passed++; } 
            else { System.out.println("  [FAIL] Imported vault correctly."); failed++; }
            
            // Wrong passphrase test
            int wrongImport = em.importVault(backupPath, "wrong_password");
            if(wrongImport == -1) { System.out.println("  [PASS] Rejected import with wrong password."); passed++; } 
            else { System.out.println("  [FAIL] Rejected import with wrong password."); failed++; }
        }

        // --- Report ---
        System.out.println("\n============================================");
        System.out.println("  RESULTS: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("============================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
