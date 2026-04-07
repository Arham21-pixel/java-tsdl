STEALTH VAULT - APPLICATION ARCHITECTURE
==========================================

╔════════════════════════════════════════════════════════════════════════╗
║                          APPLICATION FLOW                            ║
╚════════════════════════════════════════════════════════════════════════╝

                              Main.java
                              (JavaFX App)
                                  │
                    ┌─────────────────────────────┐
                    │                             │
                    ▼                             ▼
              LoginScreen                  VaultDashboard
         (Authentication UI)            (Main Vault Interface)
         ┌──────────────────┐           ┌──────────────────┐
         │ - Username Field │           │ - Left Sidebar   │
         │ - Password Field │           │ - Category Btns  │
         │ - Login Button   │           │ - Item Cards     │
         │ - Card Layout    │           │ - Add Button     │
         │ - Dark Theme     │           │ - Logout Button  │
         └────────┬─────────┘           └────────┬─────────┘
                  │                              │
                  │ (Authentication OK)          │ (Add Password)
                  └──────────────────┬───────────┘
                                     │
                                     ▼
                            AddItemDialog
                         (Add Password Popup)
                         ┌──────────────────┐
                         │ - Service Name   │
                         │ - Username Field │
                         │ - Password Field │
                         │ - Show/Hide Pwd  │
                         │ - Category Box   │
                         │ - Add/Cancel Btn │
                         └──────────────────┘


╔════════════════════════════════════════════════════════════════════════╗
║                          CLASS HIERARCHY                              ║
╚════════════════════════════════════════════════════════════════════════╝

Main (extends Application)
├── start(Stage stage)
├── showDashboard(String username)
└── showLoginScreen()

LoginScreen
├── scene: Scene
├── createScene(): Scene
├── applyFieldFocusStyle(TextField)
├── handleLogin()
└── getScene(): Scene

VaultDashboard
├── vaultItems: List<VaultItem>
├── createScene(): Scene
├── createSidebar(): VBox
├── createContentArea(): VBox
├── createItemCard(VaultItem): HBox
├── updateItemsDisplay()
├── copyToClipboard(String)
├── showPassword(VaultItem)
└── showAddItemDialog()

AddItemDialog
├── dialogStage: Stage
├── initializeDialog()
├── styleInputField(TextField, String)
├── validateForm(...): boolean
└── showError(String)

VaultItem (Data Model)
├── id: String
├── title: String
├── username: String
├── password: String
├── category: String
├── createdAt: long
├── getters & setters
└── getMaskedPassword(): String


╔════════════════════════════════════════════════════════════════════════╗
║                          THEME COLOR PALETTE                          ║
╚════════════════════════════════════════════════════════════════════════╝

PRIMARY COLORS:
  Background        #1a1a2e  ████████ Deep Dark Blue
  Secondary         #0f3460  ████████ Dark Blue
  Card              #16213e  ████████ Medium Dark
  
ACCENT COLORS:
  Primary Accent    #7c3aed  ████████ Vibrant Purple
  Light Accent      #a855f7  ████████ Light Purple
  
TEXT COLORS:
  Primary Text      #ffffff  ████████ White
  Secondary Text    #b0a5d4  ████████ Light Purple
  Muted Text        #8b7db8  ████████ Muted Purple


╔════════════════════════════════════════════════════════════════════════╗
║                        NAVIGATION FLOW                                ║
╚════════════════════════════════════════════════════════════════════════╝

                          ┌──────────────┐
                          │ Main.java    │
                          │   (Start)    │
                          └────────┬─────┘
                                   │
                    ┌──────────────▼──────────────┐
                    │  Show LoginScreen           │
                    │  (Initialize at startup)    │
                    └──────────────┬──────────────┘
                                   │
                         ┌─────────▼─────────┐
                         │  User enters      │
                         │  credentials      │
                         └─────────┬─────────┘
                                   │
                         ┌─────────▼─────────┐
                         │  Click LOGIN      │
                         └─────────┬─────────┘
                                   │
              ┌────────────────────▼────────────────────┐
              │  showDashboard(username)                 │
              │  - Initialize VaultItem list             │
              │  - Load demo data                        │
              │  - Build UI with categories              │
              └─────────────────┬──────────────────────┘
                                │
                ┌───────────────▼────────────────┐
                │  VaultDashboard displays       │
                │  with 5 sample passwords       │
                └───────────┬────────────────────┘
                            │
               ┌────────────┴────────────┬──────────────┐
               │                         │              │
       ┌───────▼──────┐      ┌──────────▼─────┐   ┌────▼──────┐
       │ Filter by    │      │ Add Password    │   │  Logout   │
       │ Category     │      │ (Opens Dialog)  │   │  Button   │
       └──────────────┘      └────────┬────────┘   └────┬──────┘
                                      │                 │
                            ┌─────────▼────────┐        │
                            │ AddItemDialog     │        │
                            │ (Modal Popup)     │        │
                            │ - Fill form       │        │
                            │ - Validate        │        │
                            │ - Add to list     │        │
                            └─────────┬────────┘        │
                                      │                 │
                            ┌─────────▼────────┐        │
                            │ Update display   │        │
                            └──────────────────┘        │
                                                        │
                            ┌──────────────────────────┘
                            │
                    ┌───────▼──────────────┐
                    │  showLoginScreen()   │
                    │  (Back to Login)     │
                    └──────────────────────┘


╔════════════════════════════════════════════════════════════════════════╗
║                      UI COMPONENT LAYOUT                              ║
╚════════════════════════════════════════════════════════════════════════╝

VAULT DASHBOARD LAYOUT:
┌─────────────────────────────────────────────────────────────────┐
│                         (HBox Main)                            │
├────────────────────┬──────────────────────────────────────────┤
│   SIDEBAR (VBox)   │         CONTENT AREA (VBox)             │
│ (width: 200px)     │ (flex: grow)                            │
├────────────────────┼──────────────────────────────────────────┤
│ STEALTH            │ TITLE: "Your Vault - {Category}"       │
│ User: {name}       │ ┌────────────────────────────────────┐ │
│ ────────           │ │  + ADD PASSWORD  (Button)          │ │
│                    │ └────────────────────────────────────┘ │
│ CATEGORIES         │ ┌────────────────────────────────────┐ │
│ [All]              │ │  ScrollPane (VBox itemsContainer)  │ │
│ [Websites]         │ │  ┌────────────────────────────────┐│ │
│ [Email]            │ │  │ CARD 1: GitHub                 ││ │
│ [Banking]          │ │  │ ┌──────────────────────────────┤│ │
│ [Social]           │ │  │ │ GitHub                       ││ │
│ ────────           │ │  │ │ ID: john.doe Pass: •••••••   ││ │
│                    │ │  │ │ Websites  [COPY] [SHOW]      ││ │
│ [LOGOUT]           │ │  │ └──────────────────────────────┘│ │
│                    │ │  │                                 ││ │
│                    │ │  │ CARD 2: Gmail                  ││ │
│                    │ │  │ ... (more cards)               ││ │
│                    │ │  └────────────────────────────────┘│ │
│                    │ └────────────────────────────────────┘ │
└────────────────────┴──────────────────────────────────────────┘

LOGIN SCREEN LAYOUT:
┌─────────────────────────────────────────────────────────────────┐
│                    (StackPane centered)                         │
├─────────────────────────────────────────────────────────────────┤
│         ┌─────────────────────────────────────┐                │
│         │    LOGIN CARD (VBox)                │                │
│         │  (width: 350px, centered)           │                │
│         ├─────────────────────────────────────┤                │
│         │                                     │                │
│         │           STEALTH                   │                │
│         │  Secure Password Vault              │                │
│         │                                     │                │
│         │ [____________ Username _________]   │                │
│         │                                     │                │
│         │ [____________ Password _________]   │                │
│         │                                     │                │
│         │      [    LOGIN BUTTON    ]         │                │
│         │                                     │                │
│         └─────────────────────────────────────┘                │
└─────────────────────────────────────────────────────────────────┘

ADD PASSWORD DIALOG LAYOUT:
┌─────────────────────────────────────┐
│    Add New Password Entry           │
├─────────────────────────────────────┤
│ Service/Website Name:               │
│ [____________ Gmail _________]      │
│                                     │
│ Username or Email:                  │
│ [____________ john@gmail.com _]     │
│                                     │
│ Password:                           │
│ [____________ ••••••••• _____]      │
│ ☐ Show Password                     │
│                                     │
│ Category:                           │
│ [Websites ▼]                        │
│                                     │
│    [CANCEL]     [ADD PASSWORD]      │
└─────────────────────────────────────┘


╔════════════════════════════════════════════════════════════════════════╗
║                    KEY DESIGN DECISIONS                               ║
╚════════════════════════════════════════════════════════════════════════╝

1. INLINE STYLING
   ✓ Programmatic CSS for maximum flexibility
   ✓ Easy theme changes during runtime
   ✓ No separate CSS file needed (but provided as reference)

2. EVENT-DRIVEN ARCHITECTURE
   ✓ Callback functions for screen navigation
   ✓ Lambda expressions for button actions
   ✓ Consumer<T> pattern for data passing

3. RESPONSIVE LAYOUT
   ✓ VBox/HBox with Priority.ALWAYS for flexible sizing
   ✓ ScrollPane for overflow content
   ✓ Dynamic card creation based on data

4. DEMO MODE
   ✓ Pre-loaded sample passwords for testing
   ✓ No database/persistence required
   ✓ Simple login validation (any credentials work)

5. DARK THEME
   ✓ High contrast for readability
   ✓ Reduced eye strain
   ✓ Premium/modern aesthetic
   ✓ Accent colors for visual hierarchy


╔════════════════════════════════════════════════════════════════════════╗
║                    READY FOR ENHANCEMENT                             ║
╚════════════════════════════════════════════════════════════════════════╝

The application is designed to be easily extensible:

• Add Database Layer → Replace demo data with SQLite/H2
• Add Encryption      → Implement password encryption in VaultItem
• Add Search         → Add search field in VaultDashboard
• Add Sorting        → Implement sort functions on categories
• Add Two-Factor     → New authentication screen
• Add Password Gen   → New dialog for generating passwords
• Add Settings       → New settings screen
• Add Sync           → Cloud sync functionality

All while maintaining the clean architecture and premium UI! 🎨

