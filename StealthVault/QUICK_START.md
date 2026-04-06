# 🔐 STEALTH VAULT - Quick Start Guide

Your premium dark-themed password vault UI is ready! Here's how to launch it:

---

## Step 1: Download JavaFX SDK (One-time setup)

### Option A: Automatic Download (Easiest)
The script will try to auto-download, or you can manually download:

1. **Go to:** https://gluonhq.com/products/javafx/ 
2. **Get:** Windows SDK (x64) - Version 21.0.4 or latest compatible with Java 25
3. **Extract to:** `C:\javafx-sdk-21`
4. Your folder structure should look like:
   ```
   C:\javafx-sdk-21\
   ├── bin/
   ├── legal/
   ├── lib/          ← This is important!
   ├── mods/
   └── README.txt
   ```

### Option B: Using Package Managers
If you have Scoop or Chocolatey:
```powershell
scoop install javafx
# or
choco install javafx
```

---

## Step 2: Run StealthVault

### Simple Command:
```powershell
cd "C:\Users\ADMIN\Documents\Stealth Vault\StealthVault"
.\start.ps1
```

### Or from Command Prompt:
```cmd
cd "C:\Users\ADMIN\Documents\Stealth Vault\StealthVault"
powershell -ExecutionPolicy Bypass -File start.ps1
```

---

## 🎮 How to Use

### **Login Screen**
- Enter any username and password (demo mode accepts anything)
- Examples: `demo`/`password` or `admin`/`123456`
- Click LOGIN

### **Vault Dashboard**
Your vault opens with 5 pre-loaded demo passwords:
- **GitHub** - john.doe / SuperSecret123!
- **Gmail** - john.doe@gmail.com / EmailPass456!
- **Bank Account** - jdoe123 / BankSecure789!
- **LinkedIn** - john-doe / LinkedInPass321!
- **Twitter** - johndoe / TweetPass654!

**Features:**
- ⬅️ **Left Sidebar:** Click categories to filter (All, Websites, Email, Banking, Social)
- 🎴 **Cards:** Show password details and copy button
- ➕ **Add Password:** Click "+ ADD PASSWORD" to add new entries
- 🚪 **Logout:** Click LOGOUT to go back to login

### **Add Password Dialog**
- **Service Name:** Gmail, Twitter, etc.
- **Username:** Your username/email
- **Password:** Your password (type it, or toggle "Show Password")
- **Category:** Select from dropdown
- Click "ADD PASSWORD"

---

## 🎨 UI Features

**Dark Theme Colors:**
- Background: `#1a1a2e` (Deep dark blue)
- Secondary: `#0f3460` (Dark blue)
- Cards: `#16213e` (Medium dark)
- Accent: `#7c3aed` (Purple)
- Text: White `#ffffff`

**Premium Details:**
- Smooth hover animations
- Card shadows and glows
- Focus effects on input fields
- Responsive layouts
- Professional typography

---

## ❌ Troubleshooting

### "JavaFX SDK not found"
✅ **Solution:** 
- Download from https://gluonhq.com/products/javafx/
- Extract to `C:\javafx-sdk-21`
- Make sure the `lib` folder exists inside

### "javac: command not found"
✅ **Solution:**
- Java not in PATH
- Install Java from https://www.oracle.com/java/technologies/downloads/ (Java 21+)
- Verify: `java -version`

### "Module not found: javafx.controls"  
✅ **Solution:**
- JavaFX SDK not properly extracted
- Check folder structure (should have `lib` subfolder)
- Try reinstalling JavaFX

### Application crashes on startup
✅ **Solution:**
- Try running from PowerShell directly
- Check that all .java files compiled (`bin` folder should have .class files)
- Make sure you're in the correct directory

---

## 📁 Project Files

```
StealthVault/
├── src/
│   ├── Main.java                    ← JavaFX entry point
│   ├── module-info.java             ← Module config
│   └── ui/
│       ├── LoginScreen.java         ← Login screen
│       ├── VaultDashboard.java      ← Main vault UI
│       ├── AddItemDialog.java       ← Add password popup
│       └── VaultItem.java           ← Password data model
├── resources/
│   └── styles.css                   ← Theme styling
├── bin/                             ← Compiled files (auto-created)
├── start.ps1                        ← Launch script
├── build.ps1                        ← Alternative build
├── run.bat                          ← Windows batch runner
├── pom.xml                          ← Maven config (optional)
└── build.gradle                     ← Gradle config (optional)
```

---

## 🚀 Advanced: Manual Build/Run

If `start.ps1` doesn't work, try manual compilation:

```powershell
# Set your JavaFX path
$JAVAFX = "C:\javafx-sdk-21\lib"

# Compile
javac --module-path $JAVAFX `
      --add-modules javafx.controls,javafx.graphics `
      -d bin `
      src/Main.java `
      src/ui/LoginScreen.java `
      src/ui/VaultDashboard.java `
      src/ui/VaultItem.java `
      src/ui/AddItemDialog.java

# Run
java --module-path $JAVAFX `
     --add-modules javafx.controls,javafx.graphics `
     -cp bin `
     Main
```

---

## 💡 Tips

1. **Try different Java versions** if you have multiple installed
2. **Run PowerShell as Administrator** if you get permission errors
3. **Check your internet connection** for auto-downloads
4. **Close the app** cleanly (don't force-close) to see the proper interface

---

## 🎯 What's Next?

After testing, you can:
- Add database persistence (SQLite)
- Implement real password encryption
- Add master password authentication
- Create password strength indicators
- Add search functionality
- Implement backup/export features

---

## 📞 Support

Having issues? Check:
1. **SETUP_GUIDE.md** - Detailed installation guide
2. **JavaFX Download:** https://gluonhq.com/products/javafx/
3. **Java Download:** https://www.oracle.com/java/technologies/downloads/

---

**Ready to launch? Run:**
```powershell
.\start.ps1
```

Enjoy your premium password vault! 🔐✨
