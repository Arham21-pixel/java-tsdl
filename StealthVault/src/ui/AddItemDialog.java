package ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.util.function.Consumer;

public class AddItemDialog {
    
    private Stage dialogStage;
    private Consumer<VaultItem> onItemAdded;
    private boolean confirmed = false;
    
    public AddItemDialog(Consumer<VaultItem> onItemAdded) {
        this.onItemAdded = onItemAdded;
        this.dialogStage = new Stage();
        initializeDialog();
        dialogStage.showAndWait();
    }
    
    private void initializeDialog() {
        dialogStage.setTitle("Add New Password");
        dialogStage.setWidth(450);
        dialogStage.setHeight(400);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        
        VBox root = new VBox(20);
        root.setStyle(
            "-fx-background-color: #1a1a2e;" +
            "-fx-padding: 25;"
        );
        root.setAlignment(Pos.TOP_CENTER);
        
        // Header
        Text titleText = new Text("Add New Password Entry");
        titleText.setStyle(
            "-fx-font-size: 20;" +
            "-fx-font-weight: bold;" +
            "-fx-fill: #ffffff;"
        );
        root.getChildren().add(titleText);
        
        // Form fields
        VBox formBox = new VBox(15);
        formBox.setStyle("-fx-padding: 10;");
        
        // Title field
        Text titleLabel = new Text("Service/Website Name:");
        titleLabel.setStyle("-fx-font-size: 12; -fx-fill: #b0a5d4; -fx-font-weight: bold;");
        TextField titleField = new TextField();
        styleInputField(titleField, "Enter service name (e.g., Gmail)");
        
        // Username field
        Text usernameLabel = new Text("Username or Email:");
        usernameLabel.setStyle("-fx-font-size: 12; -fx-fill: #b0a5d4; -fx-font-weight: bold;");
        TextField usernameField = new TextField();
        styleInputField(usernameField, "Enter your username");
        
        // Password field
        Text passwordLabel = new Text("Password:");
        passwordLabel.setStyle("-fx-font-size: 12; -fx-fill: #b0a5d4; -fx-font-weight: bold;");
        PasswordField passwordField = new PasswordField();
        styleInputField(passwordField, "Enter password");
        
        // Show password checkbox
        CheckBox showPasswordCheckbox = new CheckBox("Show Password");
        showPasswordCheckbox.setStyle(
            "-fx-font-size: 12;" +
            "-fx-text-fill: #b0a5d4;"
        );
        
        TextField passwordDisplay = new TextField();
        passwordDisplay.setVisible(false);
        passwordDisplay.setManaged(false);
        styleInputField(passwordDisplay, "");
        
        showPasswordCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordDisplay.setText(passwordField.getText());
                passwordDisplay.setVisible(true);
                passwordDisplay.setManaged(true);
                passwordField.setVisible(false);
                passwordField.setManaged(false);
            } else {
                passwordField.setText(passwordDisplay.getText());
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                passwordDisplay.setVisible(false);
                passwordDisplay.setManaged(false);
            }
        });
        
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (showPasswordCheckbox.isSelected()) {
                passwordDisplay.setText(newVal);
            }
        });
        
        passwordDisplay.textProperty().addListener((obs, oldVal, newVal) -> {
            if (showPasswordCheckbox.isSelected()) {
                passwordField.setText(newVal);
            }
        });
        
        // Category dropdown
        Text categoryLabel = new Text("Category:");
        categoryLabel.setStyle("-fx-font-size: 12; -fx-fill: #b0a5d4; -fx-font-weight: bold;");
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Websites", "Email", "Banking", "Social", "Other");
        categoryBox.setValue("Websites");
        categoryBox.setStyle(
            "-fx-font-size: 12;" +
            "-fx-padding: 10;" +
            "-fx-background-color: #0f3460;" +
            "-fx-text-fill: #ffffff;"
        );
        categoryBox.setPrefHeight(35);
        
        formBox.getChildren().addAll(
            titleLabel, titleField,
            usernameLabel, usernameField,
            passwordLabel, passwordField, passwordDisplay, showPasswordCheckbox,
            categoryLabel, categoryBox
        );
        
        root.getChildren().add(formBox);
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20 0 0 0;");
        
        Button cancelBtn = new Button("CANCEL");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setStyle(
            "-fx-font-size: 12;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10;" +
            "-fx-background-color: #0f3460;" +
            "-fx-text-fill: #b0a5d4;" +
            "-fx-border-color: #7c3aed;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialogStage.close());
        
        Button addBtn = new Button("ADD PASSWORD");
        addBtn.setPrefWidth(150);
        addBtn.setStyle(
            "-fx-font-size: 12;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10;" +
            "-fx-background-color: #7c3aed;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        );
        
        addBtn.setOnAction(e -> {
            if (validateForm(titleField, usernameField, passwordField, passwordDisplay, showPasswordCheckbox)) {
                String finalPassword = showPasswordCheckbox.isSelected() ? 
                    passwordDisplay.getText() : passwordField.getText();
                    
                VaultItem newItem = new VaultItem(
                    titleField.getText(),
                    usernameField.getText(),
                    finalPassword,
                    categoryBox.getValue()
                );
                onItemAdded.accept(newItem);
                confirmed = true;
                dialogStage.close();
            }
        });
        
        buttonBox.getChildren().addAll(cancelBtn, addBtn);
        root.getChildren().add(buttonBox);
        
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
    }
    
    private void styleInputField(TextField field, String prompt) {
        if (!prompt.isEmpty()) {
            field.setPromptText(prompt);
        }
        field.setStyle(
            "-fx-font-size: 12;" +
            "-fx-padding: 10;" +
            "-fx-background-color: #0f3460;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-prompt-text-fill: #8b7db8;" +
            "-fx-border-color: #7c3aed;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;"
        );
        
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-font-size: 12;" +
                    "-fx-padding: 10;" +
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
                    "-fx-font-size: 12;" +
                    "-fx-padding: 10;" +
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
    
    private boolean validateForm(TextField title, TextField username, PasswordField password, 
                                 TextField passwordDisplay, CheckBox showPassword) {
        String titleText = title.getText().trim();
        String usernameText = username.getText().trim();
        String passwordText = showPassword.isSelected() ? 
            passwordDisplay.getText().trim() : password.getText().trim();
        
        if (titleText.isEmpty()) {
            showError("Please enter a service/website name");
            return false;
        }
        if (usernameText.isEmpty()) {
            showError("Please enter a username/email");
            return false;
        }
        if (passwordText.isEmpty()) {
            showError("Please enter a password");
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
