package ui;

import java.io.Serializable;

/**
 * VaultItem represents a single password entry in the vault.
 * Supports serialization to a simple text format for encrypted storage.
 */
public class VaultItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String username;
    private String password;
    private String category;
    private long createdAt;

    public VaultItem(String title, String username, String password, String category) {
        this.id = System.currentTimeMillis() + "_" + Math.round(Math.random() * 10000);
        this.title = title;
        this.username = username;
        this.password = password;
        this.category = category;
        this.createdAt = System.currentTimeMillis();
    }

    /** Reconstruct from stored fields */
    public VaultItem(String id, String title, String username, String password, String category, long createdAt) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
        this.category = category;
        this.createdAt = createdAt;
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getCategory() { return category; }
    public long getCreatedAt() { return createdAt; }

    // --- Setters ---
    public void setTitle(String title) { this.title = title; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setCategory(String category) { this.category = category; }

    public String getMaskedPassword() {
        if (password == null || password.isEmpty()) return "••••••••";
        return "•".repeat(Math.min(8, password.length()));
    }

    /**
     * Serialize this item to a single-line text format.
     * Fields are separated by the "|" character.
     * Any "|" in field values is escaped to "\\|".
     */
    public String toStorageLine() {
        return escape(id) + "|" +
               escape(title) + "|" +
               escape(username) + "|" +
               escape(password) + "|" +
               escape(category) + "|" +
               createdAt;
    }

    /**
     * Deserialize a VaultItem from a single storage line.
     * Returns null if the line is malformed.
     */
    public static VaultItem fromStorageLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;

        // Split on unescaped "|"
        String[] parts = line.split("(?<!\\\\)\\|", -1);
        if (parts.length < 6) return null;

        try {
            String id = unescape(parts[0]);
            String title = unescape(parts[1]);
            String username = unescape(parts[2]);
            String password = unescape(parts[3]);
            String category = unescape(parts[4]);
            long createdAt = Long.parseLong(parts[5].trim());
            return new VaultItem(id, title, username, password, category, createdAt);
        } catch (Exception e) {
            System.err.println("[VaultItem] Failed to parse storage line: " + e.getMessage());
            return null;
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("|", "\\|");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\|", "|").replace("\\\\", "\\");
    }

    @Override
    public String toString() {
        return "VaultItem{" + title + " / " + username + " [" + category + "]}";
    }
}
