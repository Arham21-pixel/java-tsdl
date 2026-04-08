package ui;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;
import storage.VaultService;
import recovery.ExportManager;

import java.security.GeneralSecurityException;
import java.util.List;

/**
 * VaultDashboard — Ultra-Premium SaaS Redesign
 */
public class VaultDashboard {

    private static final String APP_BACKGROUND = "-fx-background-color: #f8fafc;";
    private static final String TEXT_PRIMARY = "#0f172a";
    private static final String TEXT_SECONDARY = "#64748b";
    private static final String TEXT_MUTED = "#cbd5e1";
    private static final String BLUE_600 = "#2563eb";
    private static final String BORDER_COLOR = "#f1f5f9";
    
    private Scene scene;
    private String username;
    private Runnable onLogout;
    private VaultService vaultService;
    private VBox itemsContainer;
    private String selectedCategory = "All";
    
    private Text titleText;
    private Text statTotalText;
    private Text statStrongText;
    private VBox categoryNav;
    
    private final String[] CATEGORIES = {"All", "Websites", "Email", "Banking", "Social", "Other"};
    private final String[] CATEGORY_ICONS = {"\uD83D\uDCDD", "\uD83C\uDF10", "\u2709\uFE0F", "\uD83D\uDCB3", "\uD83D\uDCAC", "\uD83D\uDCC2"};
    private final String[] CATEGORY_LABELS = {"Overview", "Services & Apps", "Mail Accounts", "Banking & Finance", "Social Networks", "Miscellaneous Files"};

    public VaultDashboard(String username, String masterPassword, Runnable onLogout) {
        this.username = username;
        this.onLogout = onLogout;

        try {
            this.vaultService = new VaultService(username, masterPassword);
        } catch (GeneralSecurityException e) {
            showError("Could not initialize the encrypted vault.\n" + e.getMessage());
            this.vaultService = null;
        }

        scene = createScene();
    }

    private Scene createScene() {
        StackPane root = new StackPane();
        root.setStyle(APP_BACKGROUND);
        
        // Ambient Top Gradient
        Rectangle topGradient = new Rectangle(2000, 300);
        topGradient.setStyle("-fx-fill: linear-gradient(to bottom, #eff6ff 0%, #f8fafc 100%);");
        StackPane.setAlignment(topGradient, Pos.TOP_CENTER);

        HBox mainLayout = new HBox(0);
        mainLayout.setPickOnBounds(false);

        // Sidebar
        VBox sidebar = createSidebar();
        HBox.setHgrow(sidebar, Priority.NEVER);

        // Content Area 
        VBox contentArea = createContentArea();
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        mainLayout.getChildren().addAll(sidebar, contentArea);
        root.getChildren().addAll(topGradient, mainLayout);

        Scene finalScene = new Scene(root, 1200, 800);
        
        // Initial animation
        FadeTransition ft = new FadeTransition(Duration.millis(800), mainLayout);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(800), mainLayout);
        tt.setFromY(20);
        tt.setToY(0);
        tt.play();

        return finalScene;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.8);" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-width: 0 1 0 0;"
        );
        sidebar.setPrefWidth(280);
        sidebar.setMinWidth(280);

        // Logo Area
        HBox logoBox = new HBox(12);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPadding(new Insets(32, 28, 40, 28));

        StackPane iconPane = new StackPane();
        iconPane.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #3b82f6, #1d4ed8);" +
            "-fx-background-radius: 12;" +
            "-fx-min-width: 44; -fx-min-height: 44;"
        );
        iconPane.setEffect(new DropShadow(10, Color.rgb(37, 99, 235, 0.3)));
        Text shield = new Text("\u2728"); // Sparkle/shield
        shield.setStyle("-fx-fill: white; -fx-font-size: 18;");
        iconPane.getChildren().add(shield);

        VBox brandBox = new VBox(-2);
        Text brandName = new Text("StealthVault");
        brandName.setStyle("-fx-font-size: 20; -fx-font-weight: 800; -fx-fill: " + TEXT_PRIMARY + ";");
        Text brandSub = new Text("ENTERPRISE EDITION");
        brandSub.setStyle("-fx-font-size: 10; -fx-font-weight: 700; -fx-fill: #94a3b8; -fx-letter-spacing: 0.1em;");
        brandBox.getChildren().addAll(brandName, brandSub);

        logoBox.getChildren().addAll(iconPane, brandBox);

        // Navigation
        categoryNav = new VBox(4);
        categoryNav.setPadding(new Insets(0, 16, 0, 16));
        renderCategories();

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // User Profile Footer
        VBox footer = new VBox(0);
        footer.setPadding(new Insets(24));
        
        HBox userCard = new HBox(12);
        userCard.setAlignment(Pos.CENTER_LEFT);
        userCard.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: #f1f5f9; -fx-border-radius: 16; -fx-border-width: 1;" +
            "-fx-padding: 12;"
        );
        userCard.setEffect(new DropShadow(15, Color.rgb(0,0,0,0.03)));

        Circle avatar = new Circle(20, Color.web("#e0e7ff"));
        Text initial = new Text(username.substring(0, 1).toUpperCase());
        initial.setStyle("-fx-font-weight: 800; -fx-fill: #4f46e5;");
        StackPane avatarPane = new StackPane(avatar, initial);

        VBox userDetails = new VBox(2);
        Text userName = new Text(username);
        userName.setStyle("-fx-font-weight: 700; -fx-fill: " + TEXT_PRIMARY + ";");
        Text userRole = new Text("Vault Owner");
        userRole.setStyle("-fx-font-size: 11; -fx-fill: " + TEXT_SECONDARY + ";");
        userDetails.getChildren().addAll(userName, userRole);
        
        Region userSpacer = new Region();
        HBox.setHgrow(userSpacer, Priority.ALWAYS);

        Button logoutBtn = new Button("\u23FB");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 16; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> {
            new auth.AuthManager().logout();
            onLogout.run();
        });

        userCard.getChildren().addAll(avatarPane, userDetails, userSpacer, logoutBtn);
        footer.getChildren().add(userCard);

        sidebar.getChildren().addAll(logoBox, categoryNav, spacer, footer);
        return sidebar;
    }

    private void renderCategories() {
        categoryNav.getChildren().clear();
        
        Text menuLabel = new Text("MAIN MENU");
        menuLabel.setStyle("-fx-font-size: 11; -fx-font-weight: 800; -fx-fill: #94a3b8; -fx-letter-spacing: 0.1em;");
        VBox.setMargin(menuLabel, new Insets(0, 0, 8, 12));
        categoryNav.getChildren().add(menuLabel);

        for (int i = 0; i < CATEGORIES.length; i++) {
            String cat = CATEGORIES[i];
            boolean isActive = cat.equals(selectedCategory);

            HBox btnBox = new HBox(12);
            btnBox.setAlignment(Pos.CENTER_LEFT);
            btnBox.setPadding(new Insets(12, 16, 12, 16));
            btnBox.getStyleClass().add("sidebar-nav-btn");
            btnBox.setStyle("-fx-background-radius: 12;");
            
            if (isActive) {
                btnBox.getStyleClass().add("sidebar-nav-btn-active");
            }

            Text icon = new Text(CATEGORY_ICONS[i]);
            icon.setStyle("-fx-font-size: 16; " + (isActive ? "-fx-opacity: 1;" : "-fx-opacity: 0.5;"));
            
            Text label = new Text(CATEGORY_LABELS[i]);
            label.setStyle(
                "-fx-font-size: 13.5; -fx-font-weight: " + (isActive ? "700" : "600") + ";" +
                "-fx-fill: " + (isActive ? BLUE_600 : TEXT_SECONDARY) + ";"
            );

            btnBox.getChildren().addAll(icon, label);

            if (isActive && vaultService != null) {
                Region s = new Region();
                HBox.setHgrow(s, Priority.ALWAYS);
                Label badge = new Label(String.valueOf(vaultService.getItemsByCategory(cat).size()));
                badge.setStyle(
                    "-fx-background-color: " + BLUE_600 + ";" +
                    "-fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: 800;" +
                    "-fx-padding: 3 8; -fx-background-radius: 10;"
                );
                btnBox.getChildren().addAll(s, badge);
            }

            btnBox.setOnMouseClicked(e -> {
                selectedCategory = cat;
                titleText.setText(CATEGORY_LABELS[java.util.Arrays.asList(CATEGORIES).indexOf(cat)]);
                renderCategories();
                updateItemsDisplay();
            });

            categoryNav.getChildren().add(btnBox);
        }
    }

    private VBox createContentArea() {
        VBox content = new VBox(0);

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(32, 40, 20, 40));

        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search vaults & keys...");
        searchField.setStyle(
            "-fx-background-color: white; -fx-background-radius: 30; -fx-border-color: #e2e8f0; -fx-border-radius: 30;" +
            "-fx-padding: 12 20; -fx-font-size: 14; -fx-prompt-text-fill: #94a3b8;"
        );
        searchField.setPrefWidth(350);
        searchField.textProperty().addListener((obs, old, val) -> updateItemsDisplay());
        searchField.getStyleClass().add("text-input");

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Button exportBtn = new Button("\u2B07\uFE0F Download Backup");
        exportBtn.setStyle(
            "-fx-background-color: white; -fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-font-weight: 700; -fx-font-size: 13; -fx-padding: 10 20;" +
            "-fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;" +
            "-fx-cursor: hand;"
        );
        exportBtn.setEffect(new DropShadow(8, Color.rgb(0,0,0,0.02)));
        exportBtn.setOnAction(e -> handleExport());

        Button addBtn = new Button("\u2795 Create Entry");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setStyle(
            "-fx-background-color: " + BLUE_600 + "; -fx-text-fill: white;" +
            "-fx-font-weight: 800; -fx-font-size: 13; -fx-padding: 10 24;"
        );
        addBtn.setOnAction(e -> showAddItemDialog());

        header.getChildren().addAll(searchField, hSpacer, exportBtn, addBtn);

        // Scrollable Area
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox scrollContent = new VBox(32);
        scrollContent.setPadding(new Insets(20, 40, 60, 40));

        // Title Sequence
        VBox titleBox = new VBox(4);
        titleText = new Text("Overview");
        titleText.setStyle("-fx-font-size: 36; -fx-font-weight: 800; -fx-fill: " + TEXT_PRIMARY + "; -fx-font-family: 'Playfair Display', serif;");
        Text subTitle = new Text("Manage your encrypted credentials and security keys in one place.");
        subTitle.setStyle("-fx-font-size: 14; -fx-fill: " + TEXT_SECONDARY + ";");
        titleBox.getChildren().addAll(titleText, subTitle);

        // Stats Row
        HBox statsRow = new HBox(24);
        
        VBox totalCard = createPremiumStatCard("Total Keys Secured", "\uD83D\uDD11");
        statTotalText = (Text) totalCard.getChildren().get(1);

        VBox strongCard = createPremiumStatCard("High-Entropy Keys", "\uD83D\uDEE1\uFE0F");
        statStrongText = (Text) strongCard.getChildren().get(1);

        statsRow.getChildren().addAll(totalCard, strongCard);

        // Main Data Table
        VBox tableContainer = new VBox(0);
        tableContainer.getStyleClass().add("glass-card");
        tableContainer.setStyle("-fx-background-color: white;");

        HBox tableHeader = new HBox();
        tableHeader.setPadding(new Insets(24));
        tableHeader.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        Text tHTxt = new Text("Encrypted Vault Records");
        tHTxt.setStyle("-fx-font-size: 16; -fx-font-weight: 800; -fx-fill: " + TEXT_PRIMARY + ";");
        tableHeader.getChildren().add(tHTxt);

        HBox colHeaders = new HBox(0);
        colHeaders.setPadding(new Insets(16, 24, 16, 24));
        colHeaders.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        colHeaders.getChildren().addAll(
            createColHead("IDENTIFIER", 280),
            createColHead("ACCOUNT", 200),
            createColHead("SECRET KEY", 180),
            createColHead("SECURITY", 120),
            createColHeadRight("ACTIONS", 100)
        );

        itemsContainer = new VBox(0);
        
        tableContainer.getChildren().addAll(tableHeader, colHeaders, itemsContainer);
        
        scrollContent.getChildren().addAll(titleBox, statsRow, tableContainer);
        scrollPane.setContent(scrollContent);
        content.getChildren().addAll(header, scrollPane);

        updateItemsDisplay();
        return content;
    }

    private VBox createPremiumStatCard(String title, String icon) {
        VBox card = new VBox(16);
        card.getStyleClass().add("glass-card");
        card.setPadding(new Insets(24));
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: white;");

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        Text t = new Text(title);
        t.setStyle("-fx-font-size: 13; -fx-font-weight: 700; -fx-fill: " + TEXT_SECONDARY + ";");
        
        Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);
        
        StackPane icnPane = new StackPane();
        icnPane.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 10; -fx-padding: 8;");
        Text ic = new Text(icon);
        ic.setStyle("-fx-font-size: 18;");
        icnPane.getChildren().add(ic);

        top.getChildren().addAll(t, s, icnPane);

        Text val = new Text("0");
        val.setStyle("-fx-font-size: 36; -fx-font-weight: 800; -fx-fill: " + TEXT_PRIMARY + ";");

        card.getChildren().addAll(top, val);
        return card;
    }

    private Text createColHead(String txt, double width) {
        Text t = new Text(txt);
        t.setStyle("-fx-font-size: 10.5; -fx-font-weight: 800; -fx-fill: #94a3b8; -fx-letter-spacing: 0.05em;");
        t.setWrappingWidth(width);
        return t;
    }

    private Text createColHeadRight(String txt, double width) {
        Text t = createColHead(txt, width);
        t.setTextAlignment(TextAlignment.RIGHT);
        return t;
    }

    private void updateItemsDisplay() {
        itemsContainer.getChildren().clear();

        if (vaultService == null) return;
        List<VaultItem> items = vaultService.getItemsByCategory(selectedCategory);

        if (items.isEmpty()) {
            VBox empty = new VBox(16);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(80, 0, 80, 0));
            Text eI = new Text("\uD83D\uDD12");
            eI.setStyle("-fx-font-size: 48; -fx-opacity: 0.5;");
            Text eT = new Text("No Records Found");
            eT.setStyle("-fx-font-size: 18; -fx-font-weight: 700; -fx-fill: " + TEXT_PRIMARY + ";");
            Text eS = new Text("Create an entry to start encrypting data.");
            eS.setStyle("-fx-fill: " + TEXT_SECONDARY + ";");
            empty.getChildren().addAll(eI, eT, eS);
            itemsContainer.getChildren().add(empty);
        } else {
            for (VaultItem item : items) {
                itemsContainer.getChildren().add(createItemRow(item));
            }
        }
        updateStats();
    }

    private HBox createItemRow(VaultItem item) {
        HBox row = new HBox(0);
        row.getStyleClass().add("row-hover");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 24, 16, 24));
        row.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        // Col 1
        HBox nCol = new HBox(12);
        nCol.setAlignment(Pos.CENTER_LEFT);
        nCol.setPrefWidth(280);
        
        StackPane iconPane = new StackPane();
        iconPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-min-width: 36; -fx-min-height: 36;");
        Text iconTxt = new Text(item.getTitle().substring(0,1).toUpperCase());
        iconTxt.setStyle("-fx-font-weight: 800; -fx-fill: " + BLUE_600 + ";");
        iconPane.getChildren().add(iconTxt);
        
        VBox nBox = new VBox(2);
        Text nT = new Text(item.getTitle());
        nT.setStyle("-fx-font-size: 14; -fx-font-weight: 700; -fx-fill: " + TEXT_PRIMARY + ";");
        Text iT = new Text("#" + item.getId().substring(0,6));
        iT.setStyle("-fx-font-size: 11; -fx-font-family: monospace; -fx-fill: #94a3b8;");
        nBox.getChildren().addAll(nT, iT);
        nCol.getChildren().addAll(iconPane, nBox);

        // Col 2
        Text uT = new Text(item.getUsername());
        uT.setStyle("-fx-font-size: 13; -fx-fill: " + TEXT_SECONDARY + "; -fx-font-weight: 500;");
        HBox idCol = new HBox(uT);
        idCol.setPrefWidth(200);
        idCol.setAlignment(Pos.CENTER_LEFT);

        // Col 3
        HBox pCol = new HBox(8);
        pCol.setPrefWidth(180);
        pCol.setAlignment(Pos.CENTER_LEFT);
        Text pT = new Text("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022");
        pT.setStyle("-fx-font-family: monospace; -fx-font-weight: 800; -fx-fill: #94a3b8; -fx-font-size: 14;");
        Button eyeBtn = new Button("\uD83D\uDC41\uFE0F");
        eyeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-opacity: 0.5;");
        eyeBtn.setOnMouseEntered(e -> eyeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-opacity: 1.0;"));
        eyeBtn.setOnMouseExited(e -> eyeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-opacity: 0.5;"));
        eyeBtn.setOnAction(e -> pT.setText(pT.getText().contains("\u2022") ? item.getPassword() : "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022"));
        pCol.getChildren().addAll(pT, eyeBtn);

        // Col 4
        boolean isStrong = item.getPassword().length() >= 12;
        Label badge = new Label(isStrong ? "High Security" : "Low Entropy");
        badge.setStyle(
            "-fx-background-color: " + (isStrong ? "#ecfdf5" : "#fef2f2") + ";" +
            "-fx-text-fill: " + (isStrong ? "#10b981" : "#ef4444") + ";" +
            "-fx-font-size: 10.5; -fx-font-weight: 800; -fx-padding: 4 10; -fx-background-radius: 12;"
        );
        HBox sCol = new HBox(badge);
        sCol.setAlignment(Pos.CENTER_LEFT);
        sCol.setPrefWidth(120);

        // Col 5
        HBox aCol = new HBox(4);
        aCol.setPrefWidth(100);
        aCol.setAlignment(Pos.CENTER_RIGHT);
        
        Button copyBtn = new Button("\uD83D\uDCCB");
        copyBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14;");
        copyBtn.setOnAction(e -> copyToClipboard(item.getPassword()));
        
        Button delBtn = new Button("\uD83D\uDDD1\uFE0F");
        delBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14;");
        delBtn.setOnAction(e -> deleteItem(item));
        
        aCol.getChildren().addAll(copyBtn, delBtn);

        row.getChildren().addAll(nCol, idCol, pCol, sCol, aCol);
        return row;
    }

    private void updateStats() {
        if (vaultService != null) {
            statTotalText.setText(String.valueOf(vaultService.getItemCount()));
            statStrongText.setText(String.valueOf(vaultService.getItemsByCategory("All").stream().filter(i -> i.getPassword().length() >= 12).count()));
        }
    }

    private void copyToClipboard(String text) {
        ClipboardContent c = new ClipboardContent();
        c.putString(text);
        Clipboard.getSystemClipboard().setContent(c);
    }
    
    private void deleteItem(VaultItem item) {
        try { vaultService.removeItem(item.getId()); updateItemsDisplay(); } catch (Exception ignored) {} 
    }
    private void showAddItemDialog() {
        new AddItemDialog(item -> {
            try { vaultService.addItem(item); updateItemsDisplay(); } catch (Exception ignored) {}
        });
    }
    private void handleExport() {
        if (vaultService == null || vaultService.getItemCount() == 0) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Export Secure Backup");
        dialog.setHeaderText("Set a passphrase to encrypt your backup");
        dialog.showAndWait().ifPresent(p -> {
            try { new ExportManager().exportVault(p); } catch (Exception ignored) {}
        });
    }
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setContentText(msg); a.showAndWait();
    }
    public Scene getScene() { return scene; }
}
