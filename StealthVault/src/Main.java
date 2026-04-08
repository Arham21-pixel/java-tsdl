import javafx.application.Application;
import javafx.stage.Stage;
import ui.LoginScreen;
import ui.VaultDashboard;

/**
 * Main entry point for StealthVault.
 * 
 * Flow:
 *   LoginScreen → (username, password) → VaultDashboard
 *   VaultDashboard uses password to derive AES key via VaultService
 *   VaultDashboard → logout → LoginScreen
 */
public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("StealthVault - Secure Password Manager");
        stage.setWidth(900);
        stage.setHeight(700);
        stage.setMinWidth(750);
        stage.setMinHeight(550);

        // Show login screen
        showLoginScreen();

        stage.show();
    }

    /**
     * Called when login succeeds. Receives both username and raw password
     * so the dashboard can derive the encryption key.
     */
    public static void showDashboard(String username, String password) {
        VaultDashboard dashboard = new VaultDashboard(username, password, Main::showLoginScreen);
        var scene = dashboard.getScene();

        // Try to load CSS
        try {
            String css = Main.class.getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("[Main] Could not load styles.css: " + e.getMessage());
        }

        primaryStage.setScene(scene);
    }

    public static void showLoginScreen() {
        LoginScreen loginScreen = new LoginScreen(Main::showDashboard);
        var scene = loginScreen.getScene();

        // Try to load CSS
        try {
            String css = Main.class.getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("[Main] Could not load styles.css: " + e.getMessage());
        }

        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
