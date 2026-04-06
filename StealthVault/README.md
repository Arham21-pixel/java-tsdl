# 🔐 StealthVault - Premium Dark-Themed Password Vault

A professional JavaFX application with a modern dark-themed UI for managing passwords securely. Built with premium design principles and smooth animations.

## ✨ Features

### Three Beautiful Screens
- **LoginScreen** - Clean centered card layout with username/password
- **VaultDashboard** - Sidebar navigation with vault items displayed as cards  
- **AddItemDialog** - Popup modal to add new password entries

### Premium Dark Theme
- **Primary Color**: `#1a1a2e` (Deep dark blue background)
- **Accent Color**: `#7c3aed` (Vibrant purple highlights)
- **Secondary Color**: `#0f3460` (Dark blue elements)
- **Text Color**: White with purple accents for hierarchy

### Vault Management
- ✓ 5 pre-loaded demo passwords
- ✓ Category filtering (All, Websites, Email, Banking, Social)
- ✓ Password masking display (••••••)
- ✓ Copy password functionality
- ✓ Show/hide password toggle
- ✓ Add new password entries
- ✓ Responsive card-based layout
- ✓ Smooth hover animations

## 🚀 Quick Start

### Prerequisites
- **Java 21+** 
- **JavaFX SDK 21.0.4+**

### Installation

1. **Download JavaFX SDK**
   - Visit: https://gluonhq.com/products/javafx/
   - Download: Windows SDK (x64)
   - Extract to: `C:\javafx-sdk-21`

2. **Launch the Application**
   ```powershell
   cd "C:\Users\ADMIN\Documents\Stealth Vault\StealthVault"
   .\start.ps1
   ```

3. **Login**
   - Username: Any text (e.g., "demo")
   - Password: Any text (e.g., "password")
   - Click LOGIN

## 📁 Project Structure

```
StealthVault/
├── src/
│   ├── Main.java                    # JavaFX Application entry point
│   ├── module-info.java             # Module declaration
│   └── ui/
│       ├── LoginScreen.java         # Authentication UI
│       ├── VaultDashboard.java      # Main vault interface
│       ├── AddItemDialog.java       # Add password popup
│       └── VaultItem.java           # Password data model
├── resources/
│   └── styles.css                   # CSS styling
├── start.ps1                        # PowerShell launcher
├── build.ps1                        # Build script
├── run.bat                          # Batch launcher
├── QUICK_START.md                   # Quick setup guide
└── SETUP_GUIDE.md                   # Detailed installation
```

## 🎮 How to Use

### Login Screen
1. Enter any username
2. Enter any password  
3. Click **LOGIN** button

### Vault Dashboard
- **Sidebar**: Click categories to filter passwords
- **Cards**: Services with username, masked password, copy & show buttons
- **Add**: Click "+ ADD PASSWORD" to create new entry
- **Logout**: Return to login screen

### Add Password Dialog
1. Enter **Service Name** (e.g., "Gmail")
2. Enter **Username/Email**
3. Enter **Password**
4. Select **Category**
5. Click **ADD PASSWORD**

## 🎨 Design

Dark theme with:
- Deep blue background `#1a1a2e`
- Purple accents `#7c3aed`
- Smooth animations
- Professional shadows & effects
- Responsive layouts

## 📊 Demo Data

5 pre-loaded passwords for testing across different categories.

## 🚀 Running

```powershell
.\start.ps1
```

Detailed guides in QUICK_START.md and SETUP_GUIDE.md
