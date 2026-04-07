package ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import java.util.function.Consumer;

public class LoginScreen {
    
    private Scene scene;
    private Consumer<String> onLoginSuccess;
    private TextField usernameField;
    private PasswordField passwordField;
    
    public LoginScreen(Consumer<String> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        scene = createScene();
    }
    
    private Scene createScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a1a2e;");
        
        // Card container
        VBox cardContainer = new VBox(30);
        cardContainer.setStyle(
            "-fx-background-color: #16213e;" +
            "-fx-padding: 40;" +
            "-fx-border-radius: 15;" +
            "-fx-background-radius: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 5);"
        );
        cardContainer.setPrefWidth(350);
        cardContainer.setAlignment(Pos.TOP_CENTER);
        
        // Header
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        
        Text titleText = new Text("STEALTH");
        titleText.setStyle(
            "-fx-font-size: 32;" +
            "-fx-font-weight: bold;" +
            "-fx-fill: #7c3aed;"
        );
        
        Text subtitleText = new Text("Secure Password Vault");
        subtitleText.setStyle(
            "-fx-font-size: 14;" +
            "-fx-fill: #b0a5d4;"
        );
        
        headerBox.getChildren().addAll(titleText, subtitleText);
        cardContainer.getChildren().add(headerBox);
        
        // Username field
        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle(
            "-fx-font-size: 13;" +
            "-fx-padding: 12;" +
            "-fx-background-color: #0f3460;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-prompt-text-fill: #8b7db8;" +
            "-fx-padding: 12;" +
            "-fx-border-color: #7c3aed;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;"
        );
        usernameField.setPrefHeight(40);
        
        // Password field
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle(
            "-fx-font-size: 13;" +
            "-fx-padding: 12;" +
            "-fx-background-color: #0f3460;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-prompt-text-fill: #8b7db8;" +
            "-fx-padding: 12;" +
            "-fx-border-color: #7c3aed;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;"
        );
        passwordField.setPrefHeight(40);
        
        // Login button
        Button loginBtn = new Button("LOGIN");
        loginBtn.setStyle(
            "-fx-font-size: 14;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12 40 12 40;" +
            "-fx-background-color: #7c3aed;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        );
        loginBtn.setPrefHeight(40);
        loginBtn.setPrefWidth(200);
        loginBtn.setOnAction(e -> handleLogin());
        
        // Focus styles
        applyFieldFocusStyle(usernameField);
        applyFieldFocusStyle(passwordField);
        
        cardContainer.getChildren().addAll(
            usernameField,
            passwordField,
            loginBtn
        );
        
        // Add card to center
        root.getChildren().add(cardContainer);
        StackPane.setAlignment(cardContainer, Pos.CENTER);
        
        Scene scene = new Scene(root, 900, 700);
        return scene;
    }
    
    private void applyFieldFocusStyle(TextField field) {
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-font-size: 13;" +
                    "-fx-padding: 12;" +
                    "-fx-background-color: #0f3460;" +
                    "-fx-text-fill: #ffffff;" +
                    "-fx-prompt-text-fill: #8b7db8;" +
                    "-fx-border-color: #a855f7;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-radius: 5;" +
                    "-fx-background-radius: 5;" +
                    "-fx-effect: dropshadow(gaussian, rgba(124, 58, 237, 0.3), 8, 0, 0, 0);"
                );
            } else {
                field.setStyle(
                    "-fx-font-size: 13;" +
                    "-fx-padding: 12;" +
                    "-fx-background-color: #0f3460;" +
                    "-fx-text-fill: #ffffff;" +
                    "-fx-prompt-text-fill: #8b7db8;" +
                    "-fx-border-color: #7c3aed;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 5;" +
                    "-fx-background-radius: 5;"
                );
            }
        });
    }
    
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        if (username.isEmpty()) {
            showError("Please enter a username");
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter a password");
            return;
        }
        
        // Simple demo validation (any non-empty is accepted)
        // In production, this would verify against real credentials
        onLoginSuccess.accept(username);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Scene getScene() {
        return scene;
    }
}
 