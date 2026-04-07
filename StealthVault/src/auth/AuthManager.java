package auth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AuthManager {

    private static final String USERS_FILE = "users.dat";
    private boolean loggedIn = false;
    private String currentUser = null;

    public boolean login(String username, String password) {
        if (username == null || password == null) {
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
            e.printStackTrace();
        }

        return false;
    }

    public void logout() {
        loggedIn = false;
        currentUser = null;
    }

    public boolean register(String username, String password) {
        if (username == null || password == null || username.contains(":")) {
            return false;
        }

        File file = new File(USERS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length >= 1 && parts[0].equals(username)) {
                        return false; 
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

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getCurrentUser() {
        return currentUser;
    }
}
