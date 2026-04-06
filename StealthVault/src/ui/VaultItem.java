package ui;

import java.io.Serializable;

public class VaultItem implements Serializable {
    private String id;
    private String title;
    private String username;
    private String password;
    private String category;
    private long createdAt;
    
    public VaultItem(String title, String username, String password, String category) {
        this.id = System.currentTimeMillis() + "";
        this.title = title;
        this.username = username;
        this.password = password;
        this.category = category;
        this.createdAt = System.currentTimeMillis();
    }
    
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getCategory() { return category; }
    public long getCreatedAt() { return createdAt; }
    
    public void setTitle(String title) { this.title = title; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setCategory(String category) { this.category = category; }
    
    public String getMaskedPassword() {
        if (password == null || password.isEmpty()) return "••••••••";
        return "•".repeat(Math.min(8, password.length()));
    }
}
