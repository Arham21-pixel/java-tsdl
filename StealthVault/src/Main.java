import javafx.application.Application;
import javafx.stage.Stage;
import ui.LoginScreen;
import ui.VaultDashboard;

public class Main extends Application {
    
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("StealthVault - Secure Password Manager");
        stage.setWidth(900);
        stage.setHeight(700);
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        
        // Load CSS
        String css = getClass().getResource("/styles.css").toExternalForm();
        
        // Show login screen
        LoginScreen loginScreen = new LoginScreen(Main::showDashboard);
        var scene = loginScreen.getScene();
        scene.getStylesheets().add(css);
        
        stage.setScene(scene);
        stage.show();
    }
    
    public static void showDashboard(String username) {
        VaultDashboard dashboard = new VaultDashboard(username, Main::showLoginScreen);
        var scene = dashboard.getScene();
        scene.getStylesheets().add(Main.class.getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
    }
    
    public static void showLoginScreen() {
        LoginScreen loginScreen = new LoginScreen(Main::showDashboard);
        var scene = loginScreen.getScene();
        scene.getStylesheets().add(Main.class.getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
