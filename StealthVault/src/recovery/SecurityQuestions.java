package recovery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * SecurityQuestions manages the setup and verification of security questions
 * used for account recovery in the StealthVault password vault.
 * <p>
 * During signup, users configure exactly 2 security questions and their
 * corresponding answers. The answers are salted and hashed using SHA-256
 * before being persisted to a {@code recovery.dat} file, so raw answers
 * are never stored on disk.
 * </p>
 * <p>
 * On a recovery attempt the user is presented with the same questions and
 * must provide matching answers. The supplied answers are hashed with the
 * stored salt and compared to the persisted digests.
 * </p>
 * <p>
 * <b>File format of {@code recovery.dat}:</b>
 * <pre>
 *   STEALTH_VAULT_RECOVERY_V1
 *   SALT:&lt;hex-encoded-salt&gt;
 *   Q1:&lt;question text&gt;
 *   A1:&lt;hex-encoded SHA-256 hash of salt+answer&gt;
 *   Q2:&lt;question text&gt;
 *   A2:&lt;hex-encoded SHA-256 hash of salt+answer&gt;
 * </pre>
 * </p>
 * <p>
 * <b>Pure Java</b> — only {@code java.io}, {@code java.nio}, and
 * {@code java.security} APIs are used. No external libraries.
 * </p>
 *
 * @author StealthVault Team
 * @version 1.0
 */
public class SecurityQuestions {

    // ---------------------------------------------------------------
    //  Constants
    // ---------------------------------------------------------------

    /** Number of security questions required. */
    private static final int REQUIRED_QUESTION_COUNT = 2;

    /** Directory that holds the recovery file. */
    private static final String DATA_DIR = "data";

    /** Name of the recovery data file. */
    private static final String RECOVERY_FILE = "recovery.dat";

    /** File-format header / magic string. */
    private static final String FILE_HEADER = "STEALTH_VAULT_RECOVERY_V1";

    /** Salt length in bytes. */
    private static final int SALT_LENGTH = 16;

    // ---------------------------------------------------------------
    //  Instance state
    // ---------------------------------------------------------------

    /** Resolved path to the recovery data file. */
    private final Path recoveryFilePath;

    // ---------------------------------------------------------------
    //  Constructors
    // ---------------------------------------------------------------

    /**
     * Creates a {@code SecurityQuestions} instance using the default data
     * directory ({@value #DATA_DIR}).
     */
    public SecurityQuestions() {
        this.recoveryFilePath = Paths.get(DATA_DIR, RECOVERY_FILE);
    }

    /**
     * Creates a {@code SecurityQuestions} instance with a custom directory
     * for storing the recovery file.  Useful for testing.
     *
     * @param customDataDir the directory in which {@code recovery.dat} will
     *                      be stored; must not be {@code null}.
     * @throws IllegalArgumentException if {@code customDataDir} is
     *                                  {@code null} or empty.
     */
    public SecurityQuestions(String customDataDir) {
        if (customDataDir == null || customDataDir.trim().isEmpty()) {
            throw new IllegalArgumentException("Custom data directory must not be null or empty.");
        }
        this.recoveryFilePath = Paths.get(customDataDir, RECOVERY_FILE);
    }

    /**
     * Creates a per-user {@code SecurityQuestions} instance.
     * Recovery data is stored as {@code {username}_recovery.dat} in the
     * specified directory, allowing multi-user recovery support.
     *
     * @param dataDir  the directory to store recovery files; must not be null.
     * @param username the username for whom recovery is configured.
     * @throws IllegalArgumentException if either argument is null or empty.
     */
    public SecurityQuestions(String dataDir, String username) {
        if (dataDir == null || dataDir.trim().isEmpty()) {
            throw new IllegalArgumentException("Data directory must not be null or empty.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username must not be null or empty.");
        }
        this.recoveryFilePath = Paths.get(dataDir, username + "_recovery.dat");
    }

    // ---------------------------------------------------------------
    //  Setup (Signup Flow)
    // ---------------------------------------------------------------

    /**
     * Configures the two security questions and answers during signup.
     * <p>
     * The answers are normalised (trimmed, lower-cased), salted, and hashed
     * with SHA-256 before being written to {@code recovery.dat}.
     * </p>
     *
     * @param question1 the first security question text.
     * @param answer1   the answer to the first question.
     * @param question2 the second security question text.
     * @param answer2   the answer to the second question.
     * @return {@code true} if the Q&amp;A were persisted successfully;
     *         {@code false} on validation failure or I/O error.
     */
    public boolean setupSecurityQuestions(String question1, String answer1,
                                          String question2, String answer2) {
        // --- Input validation ---------------------------------------------------
        if (!isValidInput(question1, "Question 1") || !isValidInput(answer1, "Answer 1") ||
            !isValidInput(question2, "Question 2") || !isValidInput(answer2, "Answer 2")) {
            return false;
        }

        // Prevent duplicate questions
        if (question1.trim().equalsIgnoreCase(question2.trim())) {
            System.err.println("[SecurityQuestions] ERROR: Both questions must be different.");
            return false;
        }

        // --- Generate a cryptographic salt --------------------------------------
        byte[] salt = generateSalt();
        if (salt == null) {
            return false;
        }
        String saltHex = bytesToHex(salt);

        // --- Hash the answers ---------------------------------------------------
        String hash1 = hashAnswer(salt, answer1);
        String hash2 = hashAnswer(salt, answer2);
        if (hash1 == null || hash2 == null) {
            System.err.println("[SecurityQuestions] ERROR: Failed to hash answers.");
            return false;
        }

        // --- Ensure data directory exists ---------------------------------------
        File parentDir = recoveryFilePath.toFile().getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.err.println("[SecurityQuestions] ERROR: Could not create data directory: "
                        + parentDir.getAbsolutePath());
                return false;
            }
        }

        // --- Write recovery.dat -------------------------------------------------
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(recoveryFilePath.toFile()), "UTF-8"));
            writer.write(FILE_HEADER);
            writer.newLine();
            writer.write("SALT:" + saltHex);
            writer.newLine();
            writer.write("Q1:" + question1.trim());
            writer.newLine();
            writer.write("A1:" + hash1);
            writer.newLine();
            writer.write("Q2:" + question2.trim());
            writer.newLine();
            writer.write("A2:" + hash2);
            writer.newLine();

            writer.flush();
            System.out.println("[SecurityQuestions] Security questions saved successfully.");
            return true;

        } catch (FileNotFoundException e) {
            System.err.println("[SecurityQuestions] ERROR: Cannot create recovery file.");
            System.err.println("  Detail: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("[SecurityQuestions] ERROR: I/O error writing recovery file.");
            System.err.println("  Detail: " + e.getMessage());
            return false;
        } finally {
            closeQuietly(writer);
        }
    }

    // ---------------------------------------------------------------
    //  Verification (Recovery Flow)
    // ---------------------------------------------------------------

    /**
     * Verifies the user's answers against the stored hashes.
     *
     * @param answer1 the answer to the first security question.
     * @param answer2 the answer to the second security question.
     * @return {@code true} if both answers match the stored hashes;
     *         {@code false} otherwise.
     */
    public boolean verifyAnswers(String answer1, String answer2) {
        if (!isValidInput(answer1, "Answer 1") || !isValidInput(answer2, "Answer 2")) {
            return false;
        }

        if (!recoveryFileExists()) {
            System.err.println("[SecurityQuestions] ERROR: No recovery file found. "
                    + "Security questions have not been set up.");
            return false;
        }

        // --- Parse recovery.dat -------------------------------------------------
        String[] parsed = parseRecoveryFile();
        if (parsed == null) {
            return false;
        }
        // parsed: [saltHex, question1, hash1, question2, hash2]
        String saltHex = parsed[0];
        String storedHash1 = parsed[2];
        String storedHash2 = parsed[4];

        byte[] salt = hexToBytes(saltHex);
        if (salt == null) {
            System.err.println("[SecurityQuestions] ERROR: Corrupt salt in recovery file.");
            return false;
        }

        // --- Hash the provided answers and compare ------------------------------
        String computedHash1 = hashAnswer(salt, answer1);
        String computedHash2 = hashAnswer(salt, answer2);

        if (computedHash1 == null || computedHash2 == null) {
            System.err.println("[SecurityQuestions] ERROR: Failed to hash provided answers.");
            return false;
        }

        boolean match1 = constantTimeEquals(storedHash1, computedHash1);
        boolean match2 = constantTimeEquals(storedHash2, computedHash2);

        if (match1 && match2) {
            System.out.println("[SecurityQuestions] Recovery verification SUCCESSFUL.");
            return true;
        } else {
            System.err.println("[SecurityQuestions] Recovery verification FAILED. "
                    + "One or more answers are incorrect.");
            return false;
        }
    }

    // ---------------------------------------------------------------
    //  Queries
    // ---------------------------------------------------------------

    /**
     * Retrieves the stored security questions (without answers).
     *
     * @return a {@code String[]} of length 2 containing the two questions,
     *         or {@code null} if the recovery file does not exist or is
     *         corrupt.
     */
    public String[] getQuestions() {
        if (!recoveryFileExists()) {
            System.err.println("[SecurityQuestions] No recovery file found.");
            return null;
        }

        String[] parsed = parseRecoveryFile();
        if (parsed == null) {
            return null;
        }
        return new String[]{ parsed[1], parsed[3] };
    }

    /**
     * Checks whether the recovery file has been created (i.e. security
     * questions have been configured).
     *
     * @return {@code true} if {@code recovery.dat} exists and is a regular
     *         file; {@code false} otherwise.
     */
    public boolean recoveryFileExists() {
        File f = recoveryFilePath.toFile();
        return f.exists() && f.isFile();
    }

    /**
     * Deletes the recovery file.  Useful for resetting security questions.
     *
     * @return {@code true} if the file was deleted or did not exist;
     *         {@code false} if deletion failed.
     */
    public boolean resetSecurityQuestions() {
        File f = recoveryFilePath.toFile();
        if (!f.exists()) {
            System.out.println("[SecurityQuestions] No recovery file to reset.");
            return true;
        }
        if (f.delete()) {
            System.out.println("[SecurityQuestions] Recovery file deleted. Security questions reset.");
            return true;
        } else {
            System.err.println("[SecurityQuestions] ERROR: Failed to delete recovery file.");
            return false;
        }
    }

    /**
     * Returns the path to the recovery file.
     *
     * @return the recovery file {@link Path}.
     */
    public Path getRecoveryFilePath() {
        return recoveryFilePath;
    }

    // ---------------------------------------------------------------
    //  Internal — Parsing
    // ---------------------------------------------------------------

    /**
     * Parses {@code recovery.dat} and returns its contents as an array.
     *
     * @return {@code String[5]}: {saltHex, question1, hash1, question2, hash2},
     *         or {@code null} if the file is missing or corrupt.
     */
    private String[] parseRecoveryFile() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(recoveryFilePath.toFile()), "UTF-8"));

            // Line 1: header
            String header = reader.readLine();
            if (header == null || !header.trim().equals(FILE_HEADER)) {
                System.err.println("[SecurityQuestions] ERROR: Invalid recovery file header.");
                return null;
            }

            // Line 2: SALT:<hex>
            String saltLine = reader.readLine();
            if (saltLine == null || !saltLine.startsWith("SALT:")) {
                System.err.println("[SecurityQuestions] ERROR: Missing or corrupt salt line.");
                return null;
            }
            String saltHex = saltLine.substring(5);

            // Line 3: Q1:<text>
            String q1Line = reader.readLine();
            if (q1Line == null || !q1Line.startsWith("Q1:")) {
                System.err.println("[SecurityQuestions] ERROR: Missing question 1.");
                return null;
            }
            String question1 = q1Line.substring(3);

            // Line 4: A1:<hash>
            String a1Line = reader.readLine();
            if (a1Line == null || !a1Line.startsWith("A1:")) {
                System.err.println("[SecurityQuestions] ERROR: Missing answer hash 1.");
                return null;
            }
            String hash1 = a1Line.substring(3);

            // Line 5: Q2:<text>
            String q2Line = reader.readLine();
            if (q2Line == null || !q2Line.startsWith("Q2:")) {
                System.err.println("[SecurityQuestions] ERROR: Missing question 2.");
                return null;
            }
            String question2 = q2Line.substring(3);

            // Line 6: A2:<hash>
            String a2Line = reader.readLine();
            if (a2Line == null || !a2Line.startsWith("A2:")) {
                System.err.println("[SecurityQuestions] ERROR: Missing answer hash 2.");
                return null;
            }
            String hash2 = a2Line.substring(3);

            return new String[]{ saltHex, question1, hash1, question2, hash2 };

        } catch (FileNotFoundException e) {
            System.err.println("[SecurityQuestions] ERROR: Recovery file not found.");
            System.err.println("  Detail: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println("[SecurityQuestions] ERROR: I/O error reading recovery file.");
            System.err.println("  Detail: " + e.getMessage());
            return null;
        } finally {
            closeQuietly(reader);
        }
    }

    // ---------------------------------------------------------------
    //  Internal — Cryptographic helpers
    // ---------------------------------------------------------------

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
            // Fallback to default SecureRandom
            try {
                SecureRandom sr = new SecureRandom();
                byte[] salt = new byte[SALT_LENGTH];
                sr.nextBytes(salt);
                return salt;
            } catch (Exception ex) {
                System.err.println("[SecurityQuestions] ERROR: Cannot generate secure salt.");
                System.err.println("  Detail: " + ex.getMessage());
                return null;
            }
        }
    }

    /**
     * Hashes an answer by normalising it, prepending the salt, and computing
     * SHA-256.
     *
     * @param salt   the salt bytes.
     * @param answer the raw answer text.
     * @return the hex-encoded SHA-256 digest, or {@code null} on failure.
     */
    private String hashAnswer(byte[] salt, String answer) {
        try {
            String normalised = answer.trim().toLowerCase();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            md.update(normalised.getBytes("UTF-8"));
            byte[] digest = md.digest();
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("[SecurityQuestions] ERROR: SHA-256 algorithm not available.");
            return null;
        } catch (IOException e) {
            System.err.println("[SecurityQuestions] ERROR: Encoding error during hashing.");
            return null;
        }
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     *
     * @param a first string.
     * @param b second string.
     * @return {@code true} if both are equal.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    // ---------------------------------------------------------------
    //  Internal — Hex utilities
    // ---------------------------------------------------------------

    /**
     * Converts a byte array to a lowercase hexadecimal string.
     *
     * @param bytes the bytes to encode.
     * @return the hex-encoded string.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }

    /**
     * Converts a hexadecimal string back to a byte array.
     *
     * @param hex the hex string to decode.
     * @return the decoded bytes, or {@code null} if the string is invalid.
     */
    private byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            return null;
        }
        try {
            int len = hex.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                        + Character.digit(hex.charAt(i + 1), 16));
            }
            return data;
        } catch (Exception e) {
            System.err.println("[SecurityQuestions] ERROR: Invalid hex string.");
            return null;
        }
    }

    // ---------------------------------------------------------------
    //  Internal — I/O helpers
    // ---------------------------------------------------------------

    /**
     * Validates that a user-supplied input string is non-null and non-blank.
     *
     * @param input the value to check.
     * @param label a human-readable label for error messages.
     * @return {@code true} if valid; {@code false} with an error message otherwise.
     */
    private boolean isValidInput(String input, String label) {
        if (input == null || input.trim().isEmpty()) {
            System.err.println("[SecurityQuestions] ERROR: " + label
                    + " must not be null or empty.");
            return false;
        }
        return true;
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
