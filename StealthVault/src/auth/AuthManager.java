package auth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * AuthManager handles user registration, login, and session state.
 * Users are stored in data/users.dat as username:hashedPassword per line.
 */
public class AuthManager {

    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.dat";
    private boolean loggedIn = false;
    private String currentUser = null;

    public boolean login(String username, String password) {
        if (username == null || password == null ||
                username.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }

        String hashedPassword = PasswordUtils.hashPassword(password);
        File file = new File(USERS_FILE);

        if (!file.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String storedUser = parts[0];
                    String storedHash = parts[1];
                    if (storedUser.equals(username) && storedHash.equals(hashedPassword)) {
                        loggedIn = true;
                        currentUser = username;
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[AuthManager] Error reading users file: " + e.getMessage());
        }

        return false;
    }

    public void logout() {
        loggedIn = false;
        currentUser = null;
    }

    public boolean register(String username, String password) {
        if (username == null || password == null ||
                username.trim().isEmpty() || password.trim().isEmpty() ||
                username.contains(":")) {
            return false;
        }

        // Ensure data directory exists
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        File file = new File(USERS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length >= 1 && parts[0].equals(username)) {
                        return false; // User already exists
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        String hashedPassword = PasswordUtils.hashPassword(password);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(username + ":" + hashedPassword);
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean userExists(String username) {
        if (username == null || username.trim().isEmpty())
            return false;

        File file = new File(USERS_FILE);
        if (!file.exists())
            return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 1 && parts[0].equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            // ignore
        }
        return false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    /**
     * Resets a user's password by rewriting users.dat with the new hash.
     * Used during security-question recovery flow.
     *
     * @param username    the user whose password to reset
     * @param newPassword the new plaintext password
     * @return true if the password was updated successfully
     */
    public boolean resetPassword(String username, String newPassword) {
        if (username == null || newPassword == null ||
                username.trim().isEmpty() || newPassword.trim().isEmpty()) {
            return false;
        }

        File file = new File(USERS_FILE);
        if (!file.exists())
            return false;

        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(username)) {
                    String newHash = PasswordUtils.hashPassword(newPassword);
                    lines.add(username + ":" + newHash);
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("[AuthManager] Error reading users file: " + e.getMessage());
            return false;
        }

        if (!found)
            return false;

        // Write back all lines with updated hash
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }
            System.out.println("[AuthManager] Password reset for user: " + username);
            return true;
        } catch (IOException e) {
            System.err.println("[AuthManager] Error writing users file: " + e.getMessage());
            return false;
        }
    }
}
