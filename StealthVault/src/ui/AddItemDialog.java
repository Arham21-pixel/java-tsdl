package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;

/**
 * AddItemDialog — Premium redesign matching the HTML demo.
 * Light theme modal with clean inputs, blue accent save button,
 * and soft shadows.
 */
public class AddItemDialog {
    
    private static final String TEXT_PRIMARY = "#0f172a";
    private static final String TEXT_SECONDARY = "#64748b";
    private static final String INPUT_BG = "#ffffff";
    private static final String INPUT_BORDER = "#e2e8f0";
    private static final String BLUE_600 = "#2563eb";
    private static final String BLUE_700 = "#1d4ed8";
    
    private Stage dialogStage;
    private Consumer<VaultItem> onItemAdded;
    private boolean confirmed = false;
    
    public AddItemDialog(Consumer<VaultItem> onItemAdded) {
        this.onItemAdded = onItemAdded;
        this.dialogStage = new Stage();
        // Use transparent style if possible, or utility
        dialogStage.initStyle(StageStyle.DECORATED); 
        initializeDialog();
        dialogStage.showAndWait();
    }
    
    private void initializeDialog() {
        dialogStage.setTitle("Add New Password");
        dialogStage.setWidth(450);
        dialogStage.setHeight(480);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        
        VBox root = new VBox(0);
        root.setStyle(
            "-fx-background-color: white;"
        );
        
        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.setStyle("-fx-border-color: " + INPUT_BORDER + "; -fx-border-width: 0 0 1 0;");
        
        StackPane iconBox = new StackPane();
        iconBox.setStyle(
            "-fx-background-color: #eff6ff;" +
            "-fx-background-radius: 12;" +
            "-fx-min-width: 40; -fx-min-height: 40;"
        );
        Text lockIcon = new Text("\uD83D\uDD12");
        lockIcon.setStyle("-fx-font-size: 16;");
        iconBox.getChildren().add(lockIcon);
        
        Text titleText = new Text("Create Secure Entry");
        titleText.setStyle(
            "-fx-font-size: 18;" +
            "-fx-font-weight: bold;" +
            "-fx-fill: " + TEXT_PRIMARY + ";"
        );
        
        header.getChildren().addAll(iconBox, titleText);
        
        // Form Body
        VBox formContent = new VBox(16);
        formContent.setPadding(new Insets(24));
        
        // App Name
        VBox appNameBox = createFormGroup("Application Name");
        TextField titleField = new TextField();
        styleInputField(titleField, "e.g. AWS Console, GitHub, bank");
        appNameBox.getChildren().add(titleField);
        
        // Identity / Category Row
        HBox rowBox = new HBox(16);
        
        VBox userBox = createFormGroup("Username / Email");
        TextField usernameField = new TextField();
        styleInputField(usernameField, "");
        userBox.getChildren().add(usernameField);
        HBox.setHgrow(userBox, Priority.ALWAYS);
        
        VBox catBox = createFormGroup("Classification");
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Websites", "Email", "Banking", "Social", "Other");
        categoryBox.setValue("Websites");
        categoryBox.setStyle(
            "-fx-font-size: 13;" +
            "-fx-padding: 4 8;" +
            "-fx-background-color: " + INPUT_BG + ";" +
            "-fx-border-color: " + INPUT_BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );
        categoryBox.setPrefHeight(42);
        categoryBox.setMaxWidth(Double.MAX_VALUE);
        catBox.getChildren().add(categoryBox);
        HBox.setHgrow(catBox, Priority.ALWAYS);
        
        rowBox.getChildren().addAll(userBox, catBox);
        
        // Password
        VBox passGroup = new VBox(6);
        
        HBox passHeader = new HBox();
        passHeader.setAlignment(Pos.CENTER_LEFT);
        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button generateBtn = new Button("GENERATE");
        generateBtn.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: " + BLUE_600 + "; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0;");
        
        passHeader.getChildren().addAll(passLabel, spacer, generateBtn);
        
        HBox passInputBox = new HBox(0);
        passInputBox.setAlignment(Pos.CENTER_LEFT);
        passInputBox.setStyle(
            "-fx-background-color: " + INPUT_BG + ";" +
            "-fx-border-color: " + INPUT_BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );
        
        TextField passwordField = new TextField(); // Using TextField to keep it visible by default like HTML demo
        passwordField.setStyle("-fx-background-color: transparent; -fx-font-size: 13; -fx-font-family: monospace; -fx-font-weight: bold; -fx-padding: 12 16;");
        passwordField.setPrefHeight(42);
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        
        generateBtn.setOnAction(e -> {
            passwordField.setText(generateRandomPassword());
        });
        
        passInputBox.getChildren().addAll(passwordField);
        passGroup.getChildren().addAll(passHeader, passInputBox);
        
        formContent.getChildren().addAll(appNameBox, rowBox, passGroup);
        VBox.setVgrow(formContent, Priority.ALWAYS);
        
        // Footer Buttons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(20, 24, 20, 24));
        footer.setStyle("-fx-background-color: #f8fafc; -fx-border-color: " + INPUT_BORDER + "; -fx-border-width: 1 0 0 0;");
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 8 16;");
        cancelBtn.setOnAction(e -> dialogStage.close());
        
        Button saveBtn = new Button("Save to Vault");
        saveBtn.setStyle(
            "-fx-font-size: 13;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;" +
            "-fx-background-color: " + BLUE_600 + ";" +
            "-fx-text-fill: white;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(
            "-fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-color: " + BLUE_700 + "; -fx-text-fill: white; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand;"
        ));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle(
            "-fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-color: " + BLUE_600 + "; -fx-text-fill: white; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand;"
        ));
        
        saveBtn.setOnAction(e -> {
            if (validateForm(titleField, usernameField, passwordField)) {
                VaultItem newItem = new VaultItem(
                    titleField.getText().trim(),
                    usernameField.getText().trim(),
                    passwordField.getText().trim(),
                    categoryBox.getValue()
                );
                onItemAdded.accept(newItem);
                confirmed = true;
                dialogStage.close();
            }
        });
        
        footer.getChildren().addAll(cancelBtn, saveBtn);
        
        root.getChildren().addAll(header, formContent, footer);
        
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
    }
    
    private VBox createFormGroup(String labelText) {
        VBox vbox = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        vbox.getChildren().add(lbl);
        return vbox;
    }
    
    private void styleInputField(TextField field, String prompt) {
        if (!prompt.isEmpty()) {
            field.setPromptText(prompt);
        }
        String baseStyle =
            "-fx-font-size: 13;" +
            "-fx-padding: 12 16;" +
            "-fx-background-color: " + INPUT_BG + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-prompt-text-fill: #94a3b8;" +
            "-fx-border-color: " + INPUT_BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;";
            
        field.setStyle(baseStyle);
        field.setPrefHeight(42);
        
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-font-size: 13;" +
                    "-fx-padding: 12 16;" +
                    "-fx-background-color: " + INPUT_BG + ";" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-prompt-text-fill: #94a3b8;" +
                    "-fx-border-color: #cbd5e1;" +
                    "-fx-border-width: 1.5;" +
                    "-fx-border-radius: 12;" +
                    "-fx-background-radius: 12;"
                );
            } else {
                field.setStyle(baseStyle);
            }
        });
    }
    
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+";
        StringBuilder pass = new StringBuilder();
        for(int i=0; i<16; i++) {
            pass.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        return pass.toString();
    }
    
    private boolean validateForm(TextField title, TextField username, TextField password) {
        if (title.getText().trim().isEmpty()) {
            showError("Please enter an Application Name");
            return false;
        }
        if (username.getText().trim().isEmpty()) {
            showError("Please enter a Username/Email");
            return false;
        }
        if (password.getText().trim().isEmpty()) {
            showError("Please enter a Password");
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
