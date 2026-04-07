package ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Runnable;

public class VaultDashboard {
    
    private Scene scene;
    private String username;
    private Runnable onLogout;
    private List<VaultItem> vaultItems;
    private ObservableList<VaultItem> displayedItems;
    private VBox itemsContainer;
    private String selectedCategory = "All";
    private final String[] CATEGORIES = {"All", "Websites", "Email", "Banking", "Social"};
    
    public VaultDashboard(String username, Runnable onLogout) {
        this.username = username;
        this.onLogout = onLogout;
        this.vaultItems = new ArrayList<>();
        this.displayedItems = FXCollections.observableArrayList(vaultItems);
        
        // Add demo data
        addDemoItems();
        
        scene = createScene();
    }
    
    private void addDemoItems() {
        vaultItems.add(new VaultItem("GitHub", "john.doe", "SuperSecret123!", "Websites"));
        vaultItems.add(new VaultItem("Gmail", "john.doe@gmail.com", "EmailPass456!", "Email"));
        vaultItems.add(new VaultItem("Bank Account", "jdoe123", "BankSecure789!", "Banking"));
        vaultItems.add(new VaultItem("LinkedIn", "john-doe", "LinkedInPass321!", "Social"));
        vaultItems.add(new VaultItem("Twitter", "johndoe", "TweetPass654!", "Social"));
    }
    
    private Scene createScene() {
        HBox mainLayout = new HBox();
        mainLayout.setStyle("-fx-background-color: #1a1a2e;");
        mainLayout.setSpacing(0);
        
        // Left Sidebar
        VBox sidebar = createSidebar();
        HBox.setHgrow(sidebar, Priority.NEVER);
        
        // Right Content Area
        VBox contentArea = createContentArea();
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        
        mainLayout.getChildren().addAll(sidebar, contentArea);
        
        Scene scene = new Scene(mainLayout, 900, 700);
        return scene;
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setStyle(
            "-fx-background-color: #0f3460;" +
            "-fx-padding: 20;" +
            "-fx-border-right: 1px solid #16213e;"
        );
        sidebar.setPrefWidth(200);
        sidebar.setAlignment(Pos.TOP_LEFT);
        
        // Header
        VBox headerBox = new VBox(5);
        Text titleText = new Text("STEALTH");
        titleText.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-fill: #7c3aed;");
        
        Text userText = new Text("User: " + username);
        userText.setStyle("-fx-font-size: 11; -fx-fill: #b0a5d4;");
        
        headerBox.getChildren().addAll(titleText, userText);
        sidebar.getChildren().add(headerBox);
        
        // Separator
        Separator separator = new Separator();
        separator.setStyle("-fx-text-fill: #16213e;");
        sidebar.getChildren().add(separator);
        
        // Categories
        Text categoriesLabel = new Text("CATEGORIES");
        categoriesLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-fill: #a855f7;");
        sidebar.getChildren().add(categoriesLabel);
        
        for (String category : CATEGORIES) {
            Button catBtn = new Button(category);
            catBtn.setPrefWidth(160);
            catBtn.setStyle(
                "-fx-font-size: 12;" +
                "-fx-padding: 10;" +
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #b0a5d4;" +
                "-fx-cursor: hand;"
            );
            
            String finalCategory = category;
            catBtn.setOnAction(e -> {
                selectedCategory = finalCategory;
                updateItemsDisplay();
            });
            
            catBtn.setOnMouseEntered(e -> 
                catBtn.setStyle(
                    "-fx-font-size: 12;" +
                    "-fx-padding: 10;" +
                    "-fx-background-color: #16213e;" +
                    "-fx-text-fill: #7c3aed;" +
                    "-fx-border-left: 3px solid #7c3aed;" +
                    "-fx-cursor: hand;"
                )
            );
            
            catBtn.setOnMouseExited(e -> 
                catBtn.setStyle(
                    "-fx-font-size: 12;" +
                    "-fx-padding: 10;" +
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #b0a5d4;" +
                    "-fx-cursor: hand;"
                )
            );
            
            sidebar.getChildren().add(catBtn);
        }
        
        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);
        
        // Logout button
        Button logoutBtn = new Button("LOGOUT");
        logoutBtn.setPrefWidth(160);
        logoutBtn.setStyle(
            "-fx-font-size: 12;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10;" +
            "-fx-background-color: #e11d48;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        );
        logoutBtn.setOnAction(e -> onLogout.run());
        sidebar.getChildren().add(logoutBtn);
        
        return sidebar;
    }
    
    private VBox createContentArea() {
        VBox content = new VBox(20);
        content.setStyle("-fx-padding: 30;");
        content.setAlignment(Pos.TOP_LEFT);
        
        // Top bar with title and add button
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        Text titleText = new Text("Your Vault - " + selectedCategory);
        titleText.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-fill: #ffffff;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addBtn = new Button("+ ADD PASSWORD");
        addBtn.setStyle(
            "-fx-font-size: 13;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20 10 20;" +
            "-fx-background-color: #7c3aed;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        );
        addBtn.setOnAction(e -> showAddItemDialog());
        
        topBar.getChildren().addAll(titleText, spacer, addBtn);
        content.getChildren().add(topBar);
        
        // Items container with scroll
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-control-inner-background: transparent;" +
            "-fx-padding: 0;"
        );
        
        itemsContainer = new VBox(15);
        itemsContainer.setStyle("-fx-padding: 10;");
        itemsContainer.setAlignment(Pos.TOP_LEFT);
        
        scrollPane.setContent(itemsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        content.getChildren().add(scrollPane);
        
        updateItemsDisplay();
        
        return content;
    }
    
    private void updateItemsDisplay() {
        itemsContainer.getChildren().clear();
        
        for (VaultItem item : vaultItems) {
            if (selectedCategory.equals("All") || item.getCategory().equals(selectedCategory)) {
                itemsContainer.getChildren().add(createItemCard(item));
            }
        }
        
        if (itemsContainer.getChildren().isEmpty()) {
            VBox emptyBox = new VBox();
            emptyBox.setAlignment(Pos.CENTER);
            Text emptyText = new Text("No items in this category");
            emptyText.setStyle("-fx-font-size: 16; -fx-fill: #8b7db8;");
            emptyBox.getChildren().add(emptyText);
            emptyBox.setPrefHeight(200);
            itemsContainer.getChildren().add(emptyBox);
        }
    }
    
    private HBox createItemCard(VaultItem item) {
        HBox card = new HBox(15);
        card.setStyle(
            "-fx-background-color: #16213e;" +
            "-fx-padding: 15;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #0f3460;" +
            "-fx-border-width: 1;"
        );
        card.setPrefHeight(80);
        card.setAlignment(Pos.CENTER_LEFT);
        
        // Add hover effect
        card.setOnMouseEntered(e -> 
            card.setStyle(
                "-fx-background-color: #1f4e6e;" +
                "-fx-padding: 15;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #7c3aed;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(124, 58, 237, 0.3), 8, 0, 0, 0);"
            )
        );
        
        card.setOnMouseExited(e -> 
            card.setStyle(
                "-fx-background-color: #16213e;" +
                "-fx-padding: 15;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #0f3460;" +
                "-fx-border-width: 1;"
            )
        );
        
        // Left section - Item details
        VBox detailsBox = new VBox(5);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        
        Text titleText = new Text(item.getTitle());
        titleText.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-fill: #ffffff;");
        
        HBox credentialBox = new HBox(20);
        
        Text usernameText = new Text("ID: " + item.getUsername());
        usernameText.setStyle("-fx-font-size: 12; -fx-fill: #b0a5d4;");
        
        Text passwordText = new Text("Pass: " + item.getMaskedPassword());
        passwordText.setStyle("-fx-font-size: 12; -fx-fill: #b0a5d4;");
        
        credentialBox.getChildren().addAll(usernameText, passwordText);
        detailsBox.getChildren().addAll(titleText, credentialBox);
        
        HBox.setHgrow(detailsBox, Priority.ALWAYS);
        
        // Right section - Category and buttons
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        
        Text categoryText = new Text(item.getCategory());
        categoryText.setStyle(
            "-fx-font-size: 11;" +
            "-fx-fill: #7c3aed;" +
            "-fx-font-weight: bold;"
        );
        
        Button copyBtn = new Button("COPY");
        copyBtn.setStyle(
            "-fx-font-size: 11;" +
            "-fx-padding: 6 12 6 12;" +
            "-fx-background-color: #7c3aed;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        );
        copyBtn.setOnAction(e -> copyToClipboard(item.getPassword()));
        
        Button showBtn = new Button("SHOW");
        showBtn.setStyle(
            "-fx-font-size: 11;" +
            "-fx-padding: 6 12 6 12;" +
            "-fx-background-color: #0f3460;" +
            "-fx-text-fill: #7c3aed;" +
            "-fx-border-color: #7c3aed;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        );
        showBtn.setOnAction(e -> showPassword(item));
        
        actionBox.getChildren().addAll(categoryText, copyBtn, showBtn);
        
        card.getChildren().addAll(detailsBox, actionBox);
        
        return card;
    }
    
    private void copyToClipboard(String text) {
        // In a real app, use: Toolkit.getDefaultToolkit().getSystemClipboard()
        // For demo purposes, just show a message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Copied!");
        alert.setHeaderText(null);
        alert.setContentText("Password copied to clipboard (demo mode)");
        alert.showAndWait();
    }
    
    private void showPassword(VaultItem item) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(item.getTitle());
        alert.setHeaderText("Password Details");
        
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 10;");
        
        Text usernameLabel = new Text("Username:");
        usernameLabel.setStyle("-fx-font-weight: bold;");
        Text usernameValue = new Text(item.getUsername());
        
        Text passwordLabel = new Text("Password:");
        passwordLabel.setStyle("-fx-font-weight: bold;");
        Text passwordValue = new Text(item.getPassword());
        passwordValue.setStyle("-fx-fill: #7c3aed;");
        
        content.getChildren().addAll(
            usernameLabel, usernameValue,
            new Separator(),
            passwordLabel, passwordValue
        );
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
    
    private void showAddItemDialog() {
        AddItemDialog dialog = new AddItemDialog(item -> {
            vaultItems.add(item);
            updateItemsDisplay();
        });
    }
    
    public Scene getScene() {
        return scene;
    }
}
