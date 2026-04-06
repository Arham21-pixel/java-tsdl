# StealthVault - Secure Password Manager UI Demo 🔐

A premium JavaFX dark-themed password vault application with a professional UI.

## Features

✨ **Three Beautiful Screens:**
- **Login Screen** - Clean centered card layout with username/password fields
- **Vault Dashboard** - Left sidebar with categories, right panel showing vault items as cards
- **Add Item Dialog** - Popup to add new password entries

🎨 **Premium Dark Theme:**
- Dark background: `#1a1a2e`
- Accent color: `#7c3aed` (Purple)
- Smooth animations and hover effects
- Professional card-based design

🔐 **Features:**
- Category filtering (All, Websites, Email, Banking, Social)
- Password masking with copy button
- Show/hide password functionality
- Responsive layout
- Demo data pre-loaded

## Prerequisites

Before running the application, you need:

1. **Java 11 or higher** installed
   - Check: `java -version`
   
2. **JavaFX SDK 21** (or your Java version)
   - Download from: https://gluonhq.com/products/javafx/
   - Extract to a location (e.g., `C:\javafx-sdk-21`)

## Setup Instructions

### Step 1: Download JavaFX SDK
1. Visit https://gluonhq.com/products/javafx/
2. Download the **Windows SDK** for your Java version
3. Extract to `C:\javafx-sdk-21` (or your preferred location)

### Step 2: Update the Build Script
Edit `build.ps1` or `build.bat` and update this line with your JavaFX path:
```
set JAVAFX_HOME=C:\javafx-sdk-21
```

### Step 3: Compile and Run

#### Option A: Using PowerShell (Recommended)
```powershell
cd C:\Users\ADMIN\Documents\Stealth Vault\StealthVault
.\build.ps1
```

#### Option B: Using Command Prompt (batch)
```cmd
cd C:\Users\ADMIN\Documents\Stealth Vault\StealthVault
build.bat
```

#### Option C: Manual Compilation
```cmd
# Compile
javac --module-path "C:\javafx-sdk-21\lib" ^
      --add-modules javafx.controls,javafx.graphics ^
      -d bin ^
      src\Main.java ^
      src\ui\*.java

# Run
java --module-path "C:\javafx-sdk-21\lib" ^
     --add-modules javafx.controls,javafx.graphics ^
     -cp bin ^
     Main
```

## Demo Credentials

**Login Screen:**
- Any non-empty username and password works in demo mode
- Examples: `demo`/`password` or `john`/`test123`

**Demo Vault Data:**
- GitHub: john.doe / SuperSecret123!
- Gmail: john.doe@gmail.com / EmailPass456!
- Bank Account: jdoe123 / BankSecure789!
- LinkedIn: john-doe / LinkedInPass321!
- Twitter: johndoe / TweetPass654!

## Features to Try

1. **Login Screen**
   - Enter any username and password
   - Click LOGIN to proceed

2. **Vault Dashboard**
   - Click category buttons to filter items
   - Hover over items to see highlight effect
   - Click "SHOW" to view full password
   - Click "COPY" to copy password (demo mode)
   - Click "LOGOUT" to return to login

3. **Add Password Dialog**
   - Click "+ ADD PASSWORD" button
   - Fill in service name, username, and password
   - Toggle "Show Password" to reveal/hide password
   - Select category from dropdown
   - Click "ADD PASSWORD" to add item

## File Structure

```
StealthVault/
├── src/
│   ├── Main.java                    # JavaFX Application entry point
│   ├── module-info.java             # Module configuration
│   └── ui/
│       ├── LoginScreen.java         # Login screen UI
│       ├── VaultDashboard.java      # Main vault dashboard
│       ├── AddItemDialog.java       # Add password dialog
│       └── VaultItem.java           # Data model for passwords
├── resources/
│   └── styles.css                   # CSS styling (reference)
├── build.ps1                        # PowerShell build script
├── build.bat                        # Batch build script
└── README.md                        # This file
```

## Color Scheme

| Element | Color | Hex |
|---------|-------|-----|
| Background | Dark Blue | #1a1a2e |
| Secondary | Darker Blue | #0f3460 |
| Cards | Medium Blue | #16213e |
| Accent | Purple | #7c3aed |
| Accent Light | Light Purple | #a855f7 |
| Text | White | #ffffff |
| Text Secondary | Light Purple | #b0a5d4 |

## Troubleshooting

### Error: "JavaFX SDK not found"
- Make sure JavaFX SDK is extracted correctly
- Update JAVAFX_HOME path in build script
- Verify path exists: `C:\javafx-sdk-21\lib`

### Error: "javac: command not found"
- Java is not in your PATH
- Install Java from oracle.com or use an IDE like IntelliJ or VS Code

### Error: "require javafx.controls"
- module-info.java needs proper JavaFX modules
- Ensure using Java 11+ with `--module-path`

### Application won't start
- Check that all .java files are in `src/` and `src/ui/` folders
- Verify `bin/` directory is created during compilation
- Try running from command line for better error messages

## Development Notes

The application uses:
- **JavaFX 21** for modern UI framework
- **Inline CSS styling** for flexibility and theme changes
- **Event-driven architecture** for screen navigation
- **Observable lists** for dynamic data updates

All styling is done programmatically in the Java code, making it easy to modify colors and themes.

## Future Enhancements

- Database persistence (SQLite/H2)
- Actual password encryption
- Master password authentication
- Password strength indicator
- Search functionality
- Backup/export features
- Dark/Light theme toggle

## License

Built for demo purposes. Feel free to modify and enhance!

---

**Ready to test?** Run `.\build.ps1` or `build.bat` now! 🚀
