package ui;

import auth.AuthManager;
import auth.PasswordUtils;
import recovery.SecurityQuestions;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * LoginScreen — Premium redesign matching the HTML demo.
 * Features a split-panel white card with decorative left side
 * and form on the right. Clean, modern, light-themed design.
 */
public class LoginScreen {

    private static final String RECOVERY_DIR = "data";

    // Colors matching HTML demo
    private static final String BG_DARK = "#020617";
    private static final String CARD_BG = "#ffffff";
    private static final String INPUT_BG = "#f8fafc";
    private static final String INPUT_BORDER = "#e2e8f0";
    private static final String INPUT_FOCUS = "#0f172a";
    private static final String TEXT_PRIMARY = "#0f172a";
    private static final String TEXT_SECONDARY = "#64748b";
    private static final String BTN_PRIMARY = "#000000";
    private static final String ACCENT_BLUE = "#0ea5e9";
    private static final String PURPLE_ACCENT = "#6366f1";

    private Scene scene;
    private BiConsumer<String, String> onLoginSuccess;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private VBox confirmPasswordBox;
    private Button primaryBtn;
    private Label toggleLabel;
    private Hyperlink toggleLink;
    private Hyperlink forgotLink;
    private Text headingText;
    private Text subheadingText;
    private Text strengthText;
    private HBox loginOptionsBox;
    private boolean isRegisterMode = false;

    public LoginScreen(BiConsumer<String, String> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        scene = createScene();
    }

    private Scene createScene() {
        // Root with animated gradient background
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #020617, #0f172a, #172554, #1e1b4b);");

        // Ambient orbs (decorative blurred circles)
        Circle orb1 = new Circle(250);
        orb1.setFill(Color.rgb(56, 189, 248, 0.15));
        orb1.setEffect(new GaussianBlur(80));
        orb1.setTranslateX(-300);
        orb1.setTranslateY(-200);
        orb1.setMouseTransparent(true);

        Circle orb2 = new Circle(300);
        orb2.setFill(Color.rgb(129, 140, 248, 0.12));
        orb2.setEffect(new GaussianBlur(80));
        orb2.setTranslateX(300);
        orb2.setTranslateY(200);
        orb2.setMouseTransparent(true);

        // Main white card
        HBox card = new HBox(0);
        card.setMaxWidth(900);
        card.setMaxHeight(620);
        card.setPrefWidth(900);
        card.setPrefHeight(620);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 24;" +
            "-fx-border-radius: 24;"
        );
        DropShadow cardShadow = new DropShadow();
        cardShadow.setRadius(40);
        cardShadow.setColor(Color.rgb(0, 0, 0, 0.3));
        cardShadow.setOffsetY(10);
        card.setEffect(cardShadow);

        // ===== LEFT SIDE — Decorative Panel =====
        StackPane leftPanel = createLeftPanel();
        HBox.setHgrow(leftPanel, Priority.ALWAYS);

        // ===== RIGHT SIDE — Form =====
        VBox rightPanel = createRightPanel();
        rightPanel.setPrefWidth(420);
        rightPanel.setMinWidth(420);

        card.getChildren().addAll(leftPanel, rightPanel);

        root.getChildren().addAll(orb1, orb2, card);
        StackPane.setAlignment(card, Pos.CENTER);

        Scene scene = new Scene(root, 1000, 720);
        scene.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                handleAction();
            }
        });

        return scene;
    }

    private StackPane createLeftPanel() {
        StackPane leftPanel = new StackPane();
        leftPanel.setStyle(
            "-fx-background-color: linear-gradient(to top, #0f172a, #1e1b4b, #312e81);" +
            "-fx-background-radius: 20 0 0 20;" +
            "-fx-padding: 40;"
        );
        leftPanel.setMinWidth(400);

        // Content overlay
        VBox leftContent = new VBox(20);
        leftContent.setAlignment(Pos.BOTTOM_LEFT);
        leftContent.setPadding(new Insets(0, 0, 40, 10));

        // Small label at top
        Text wiseQuote = new Text("A WISE QUOTE");
        wiseQuote.setStyle("-fx-font-size: 10; -fx-fill: rgba(255,255,255,0.5); -fx-font-weight: bold;");

        // Main heading
        Text heading = new Text("Get\nEverything\nYou Want");
        heading.setStyle("-fx-font-size: 44; -fx-fill: white; -fx-font-weight: normal;");
        heading.setFont(Font.font("Georgia", 44));
        heading.setLineSpacing(2);

        // subtitle
        Text subtitle = new Text("You can get everything you want if you work\nhard, trust the process, and stick to the plan.");
        subtitle.setStyle("-fx-font-size: 13; -fx-fill: rgba(255,255,255,0.6);");
        subtitle.setLineSpacing(3);

        // Decorative circles
        Circle deco1 = new Circle(120);
        deco1.setFill(Color.rgb(99, 102, 241, 0.2));
        deco1.setEffect(new GaussianBlur(40));
        deco1.setTranslateX(80);
        deco1.setTranslateY(-100);
        deco1.setMouseTransparent(true);

        Circle deco2 = new Circle(80);
        deco2.setFill(Color.rgb(56, 189, 248, 0.15));
        deco2.setEffect(new GaussianBlur(50));
        deco2.setTranslateX(-60);
        deco2.setTranslateY(50);
        deco2.setMouseTransparent(true);

        VBox topLabel = new VBox(wiseQuote);
        topLabel.setAlignment(Pos.TOP_LEFT);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        leftContent.getChildren().addAll(topLabel, spacer, heading, subtitle);

        leftPanel.getChildren().addAll(deco1, deco2, leftContent);

        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(0);
        rightPanel.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 0 24 24 0;" +
            "-fx-padding: 40 50 30 50;"
        );
        rightPanel.setAlignment(Pos.TOP_CENTER);

        // StealthVault logo at top
        HBox logoBox = new HBox(8);
        logoBox.setAlignment(Pos.CENTER);
        Text shieldIcon = new Text("\uD83D\uDEE1");
        shieldIcon.setStyle("-fx-font-size: 18;");
        Text logoText = new Text("StealthVault");
        logoText.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        logoBox.getChildren().addAll(shieldIcon, logoText);
        logoBox.setPadding(new Insets(0, 0, 20, 0));

        // Heading
        headingText = new Text("Welcome Back");
        headingText.setStyle("-fx-font-size: 30; -fx-fill: " + TEXT_PRIMARY + ";");
        headingText.setFont(Font.font("Georgia", 30));

        // Subheading
        subheadingText = new Text("Enter your username and password to access your vault");
        subheadingText.setStyle("-fx-font-size: 13; -fx-fill: " + TEXT_SECONDARY + "; -fx-font-weight: normal;");
        subheadingText.setTextAlignment(TextAlignment.CENTER);
        subheadingText.setWrappingWidth(300);

        VBox headingBox = new VBox(8);
        headingBox.setAlignment(Pos.CENTER);
        headingBox.setPadding(new Insets(10, 0, 25, 0));
        headingBox.getChildren().addAll(headingText, subheadingText);

        // Form fields
        VBox formBox = new VBox(18);
        formBox.setAlignment(Pos.CENTER_LEFT);

        // Username
        VBox usernameBox = createFieldGroup("Username");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        styleModernField(usernameField);
        usernameBox.getChildren().add(usernameField);

        // Password
        VBox passwordBox = createFieldGroup("Password");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        styleModernField(passwordField);
        passwordBox.getChildren().add(passwordField);

        // Password strength (register mode)
        strengthText = new Text("");
        strengthText.setStyle("-fx-font-size: 11; -fx-fill: " + TEXT_SECONDARY + ";");
        strengthText.setVisible(false);
        strengthText.setManaged(false);

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isRegisterMode && newVal != null) {
                int strength = Math.min(PasswordUtils.checkPasswordStrength(newVal), 4);
                String[] labels = {"Very Weak", "Weak", "Fair", "Good", "Strong"};
                String[] colors = {"#ef4444", "#f59e0b", "#f59e0b", "#10b981", "#10b981"};
                strengthText.setText("Strength: " + labels[strength]);
                strengthText.setStyle("-fx-font-size: 11; -fx-fill: " + colors[strength] + "; -fx-font-weight: bold;");
            }
        });

        // Confirm password
        confirmPasswordBox = new VBox(6);
        Label confirmLabel = new Label("Confirm Password");
        confirmLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        styleModernField(confirmPasswordField);
        confirmPasswordBox.getChildren().addAll(confirmLabel, confirmPasswordField);
        confirmPasswordBox.setVisible(false);
        confirmPasswordBox.setManaged(false);

        // Login options row (remember me + forgot)
        loginOptionsBox = new HBox();
        loginOptionsBox.setAlignment(Pos.CENTER_LEFT);
        loginOptionsBox.setPadding(new Insets(4, 0, 0, 0));

        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setStyle("-fx-font-size: 11; -fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-weight: bold;");

        Region optionSpacer = new Region();
        HBox.setHgrow(optionSpacer, Priority.ALWAYS);

        forgotLink = new Hyperlink("Forgot Password?");
        forgotLink.setStyle("-fx-font-size: 11; -fx-text-fill: " + TEXT_SECONDARY + "; -fx-border-color: transparent; -fx-font-weight: bold;");
        forgotLink.setOnAction(e -> handleForgotPassword());

        loginOptionsBox.getChildren().addAll(rememberMe, optionSpacer, forgotLink);

        // Primary button
        primaryBtn = new Button("Sign In");
        primaryBtn.setPrefWidth(Double.MAX_VALUE);
        primaryBtn.setPrefHeight(48);
        primaryBtn.setStyle(
            "-fx-font-size: 14;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: " + BTN_PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-cursor: hand;"
        );
        primaryBtn.setOnAction(e -> handleAction());

        // Hover effect on button
        primaryBtn.setOnMouseEntered(e -> primaryBtn.setStyle(
            "-fx-font-size: 14; -fx-font-weight: bold; -fx-background-color: #1e293b; -fx-text-fill: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-cursor: hand;"
        ));
        primaryBtn.setOnMouseExited(e -> primaryBtn.setStyle(
            "-fx-font-size: 14; -fx-font-weight: bold; -fx-background-color: " + BTN_PRIMARY + "; -fx-text-fill: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-cursor: hand;"
        ));

        formBox.getChildren().addAll(
            usernameBox, passwordBox, strengthText, confirmPasswordBox,
            loginOptionsBox, primaryBtn
        );

        // Toggle link at bottom
        HBox toggleBox = new HBox(4);
        toggleBox.setAlignment(Pos.CENTER);
        toggleLabel = new Label("Don't have an account?");
        toggleLabel.setStyle("-fx-font-size: 13; -fx-text-fill: " + TEXT_SECONDARY + ";");
        toggleLink = new Hyperlink("Sign Up");
        toggleLink.setStyle("-fx-font-size: 13; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-weight: 900; -fx-border-color: transparent;");
        toggleLink.setOnAction(e -> toggleMode());
        toggleBox.getChildren().addAll(toggleLabel, toggleLink);

        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        rightPanel.getChildren().addAll(logoBox, headingBox, formBox, bottomSpacer, toggleBox);

        return rightPanel;
    }

    private VBox createFieldGroup(String label) {
        VBox group = new VBox(6);
        Label fieldLabel = new Label(label);
        fieldLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        group.getChildren().add(fieldLabel);
        return group;
    }

    private void styleModernField(TextField field) {
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
        field.setPrefHeight(48);

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-font-size: 13;" +
                    "-fx-padding: 12 16;" +
                    "-fx-background-color: " + INPUT_BG + ";" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-prompt-text-fill: #94a3b8;" +
                    "-fx-border-color: #334155;" +
                    "-fx-border-width: 1.5;" +
                    "-fx-border-radius: 12;" +
                    "-fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.08), 6, 0, 0, 0);"
                );
            } else {
                field.setStyle(baseStyle);
            }
        });
    }

    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        if (isRegisterMode) {
            headingText.setText("Create Account");
            subheadingText.setText("Provide your details to initiate a secure vault");
            primaryBtn.setText("Sign Up");
            toggleLabel.setText("Already have an account?");
            toggleLink.setText("Sign In");
            confirmPasswordBox.setVisible(true);
            confirmPasswordBox.setManaged(true);
            strengthText.setVisible(true);
            strengthText.setManaged(true);
            loginOptionsBox.setVisible(false);
            loginOptionsBox.setManaged(false);
        } else {
            headingText.setText("Welcome Back");
            subheadingText.setText("Enter your username and password to access your vault");
            primaryBtn.setText("Sign In");
            toggleLabel.setText("Don't have an account?");
            toggleLink.setText("Sign Up");
            confirmPasswordBox.setVisible(false);
            confirmPasswordBox.setManaged(false);
            strengthText.setVisible(false);
            strengthText.setManaged(false);
            loginOptionsBox.setVisible(true);
            loginOptionsBox.setManaged(true);
        }
    }

    private void handleAction() {
        if (isRegisterMode) {
            handleRegister();
        } else {
            handleLogin();
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) { showError("Please enter a username"); return; }
        if (password.isEmpty()) { showError("Please enter a password"); return; }

        AuthManager am = new AuthManager();
        if (am.login(username, password)) {
            onLoginSuccess.accept(username, password);
        } else {
            showError("Invalid username or password.\nIf you're new, click 'Sign Up' below.");
        }
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty()) { showError("Please enter a username"); return; }
        if (password.isEmpty()) { showError("Please enter a password"); return; }
        if (password.length() < 6) { showError("Password must be at least 6 characters"); return; }
        if (!password.equals(confirm)) { showError("Passwords do not match"); return; }

        AuthManager am = new AuthManager();
        if (am.userExists(username)) {
            showError("Username already exists. Please choose another.");
            return;
        }

        if (am.register(username, password)) {
            setupSecurityQuestions(username);
            showInfo("Account created successfully!\nYou can now login.");
            isRegisterMode = true;
            toggleMode();
        } else {
            showError("Registration failed. Please try again.");
        }
    }

    private void setupSecurityQuestions(String username) {
        String[] questions = {
            "What is your pet's name?",
            "What city were you born in?",
            "What is your mother's maiden name?",
            "What was the name of your first school?",
            "What is your favorite movie?"
        };

        ChoiceDialog<String> q1Dialog = new ChoiceDialog<>(questions[0], questions);
        q1Dialog.setTitle("Security Setup (1/4)");
        q1Dialog.setHeaderText("Set up recovery questions");
        q1Dialog.setContentText("Choose Question 1:");
        Optional<String> q1Result = q1Dialog.showAndWait();
        if (!q1Result.isPresent()) return;
        String question1 = q1Result.get();

        TextInputDialog a1Dialog = new TextInputDialog();
        a1Dialog.setTitle("Security Setup (2/4)");
        a1Dialog.setHeaderText("Answer for: " + question1);
        a1Dialog.setContentText("Your Answer:");
        Optional<String> a1Result = a1Dialog.showAndWait();
        if (!a1Result.isPresent() || a1Result.get().trim().isEmpty()) return;
        String answer1 = a1Result.get();

        ChoiceDialog<String> q2Dialog = new ChoiceDialog<>(questions[1], questions);
        q2Dialog.setTitle("Security Setup (3/4)");
        q2Dialog.setHeaderText("Choose a DIFFERENT question");
        q2Dialog.setContentText("Choose Question 2:");
        Optional<String> q2Result = q2Dialog.showAndWait();
        if (!q2Result.isPresent()) return;
        String question2 = q2Result.get();

        if (question1.equals(question2)) {
            showError("Please choose two different questions.");
            return;
        }

        TextInputDialog a2Dialog = new TextInputDialog();
        a2Dialog.setTitle("Security Setup (4/4)");
        a2Dialog.setHeaderText("Answer for: " + question2);
        a2Dialog.setContentText("Your Answer:");
        Optional<String> a2Result = a2Dialog.showAndWait();
        if (!a2Result.isPresent() || a2Result.get().trim().isEmpty()) return;
        String answer2 = a2Result.get();

        SecurityQuestions sq = new SecurityQuestions(RECOVERY_DIR, username);
        if (sq.setupSecurityQuestions(question1, answer1, question2, answer2)) {
            System.out.println("[LoginScreen] Security questions saved for user: " + username);
        } else {
            showError("Failed to save security questions. You can still login normally.");
        }
    }

    private void handleForgotPassword() {
        TextInputDialog userDialog = new TextInputDialog();
        userDialog.setTitle("Account Recovery");
        userDialog.setHeaderText("Forgot your master password?");
        userDialog.setContentText("Enter your username:");
        Optional<String> userResult = userDialog.showAndWait();
        if (!userResult.isPresent() || userResult.get().trim().isEmpty()) return;
        String username = userResult.get().trim();

        AuthManager am = new AuthManager();
        if (!am.userExists(username)) {
            showError("No account found with username: " + username);
            return;
        }

        SecurityQuestions sq = new SecurityQuestions(RECOVERY_DIR, username);
        if (!sq.recoveryFileExists()) {
            showError("No security questions set up for this account.\nPassword recovery is not available.");
            return;
        }

        String[] questions = sq.getQuestions();
        if (questions == null || questions.length < 2) {
            showError("Security questions file is corrupt.");
            return;
        }

        TextInputDialog a1Dialog = new TextInputDialog();
        a1Dialog.setTitle("Security Verification (1/2)");
        a1Dialog.setHeaderText(questions[0]);
        a1Dialog.setContentText("Your Answer:");
        Optional<String> a1Result = a1Dialog.showAndWait();
        if (!a1Result.isPresent()) return;

        TextInputDialog a2Dialog = new TextInputDialog();
        a2Dialog.setTitle("Security Verification (2/2)");
        a2Dialog.setHeaderText(questions[1]);
        a2Dialog.setContentText("Your Answer:");
        Optional<String> a2Result = a2Dialog.showAndWait();
        if (!a2Result.isPresent()) return;

        if (!sq.verifyAnswers(a1Result.get(), a2Result.get())) {
            showError("Security answers do not match.\nPassword recovery denied.");
            return;
        }

        TextInputDialog newPwdDialog = new TextInputDialog();
        newPwdDialog.setTitle("Password Reset");
        newPwdDialog.setHeaderText("Security verification successful!");
        newPwdDialog.setContentText("Enter new master password:");
        Optional<String> newPwdResult = newPwdDialog.showAndWait();
        if (!newPwdResult.isPresent() || newPwdResult.get().trim().isEmpty()) return;

        String newPassword = newPwdResult.get();
        if (newPassword.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        if (am.resetPassword(username, newPassword)) {
            showInfo("Password reset successful!\nYou can now login with your new password.");
        } else {
            showError("Failed to reset password. Please try again.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }
}