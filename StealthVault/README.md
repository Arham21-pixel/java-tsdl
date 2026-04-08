# 🔐 StealthVault - Enterprise-Grade Secure Password Manager

A highly polished, billion-dollar SaaS-style password manager. Built utilizing **Java 26, JavaFX, and AES-GCM Encryption**, featuring a stunning glassmorphism design, fluid animations, and robust underlying cryptography.

## ✨ Features

### Ultimate SaaS Experience
- **Fluid UI Elements**: Premium transitions and micro-animations on all components.
- **Glassmorphism Design**: Soft, translucent layered UI with intricate dropped shadows perfectly complementing the dynamic layout.
- **Google Fonts Integration**: Utilizing high-fidelity typography (*Inter* and *Playfair Display*).
- **Responsive Navigation**: Beautiful sidebars with active selection states, dynamic statistics, and interactive data tables.

### Enterprise-Grade Security
- **AES-GCM Encryption**: Real cryptography to secure local vault files.
- **Zero-Knowledge Architecture**: The master password is never saved; it is utilized dynamically to encrypt and decrypt the payload.
- **Secure Key Derivation**: Hardened SHA-256 salt & stretch mechanisms for reliable key creation.
- **Export & Backup**: Export fully encrypted `.bak` vault archives anywhere.
- **Account Recovery**: Implemented via secure, personalized security questions.

---

## 🚀 Quick Start

### Prerequisites
- **Java 26** or newer
- **JavaFX SDK 26** (Extracted to `C:\javafx-sdk-26`)

### Booting the Engine

1. **Launch the Application via Script**:
   We provide easy-to-use launch scripts:
   ```powershell
   # PowerShell
   .\start.ps1
   ```
   ```cmd
   # Windows Command Prompt
   .\run.bat
   ```

2. **First Time Setup**:
   - On the beautiful login screen, click **Sign Up** to create a master account.
   - Enter your username and a strong master password.
   - Configure your account recovery security questions.
   
3. **Enjoy the Vault**:
   - Begin adding credentials! 
   - View visual indicators showing password entropy, filter by identity categories, or export your full vault backup cleanly.

---

## 📁 Repository Structure

```
StealthVault/
├── src/
│   ├── Main.java                    # Entry point & scene routing
│   ├── auth/                        # User authentication payloads & checking
│   ├── crypto/                      # AES-GCM and Key management
│   ├── recovery/                    # Export/Import and Security questions
│   ├── storage/                     # File-system storage operations
│   └── ui/                          # Elegant, enterprise-grade JavaFX UI classes
├── resources/
│   └── styles.css                   # Extensive premium CSS typography & animations
├── start.ps1                        # PowerShell boot engine
├── run.bat                          # Command Line boot engine
└── pom.xml                          # Maven configuration (if building via Maven)
```

## 🎨 Philosophy

StealthVault was engineered to demonstrate that **desktop apps do not have to look dated**, and **secure applications do not have to be ugly**. By fusing a fully functional local cryptographic engine with web-tier aesthetic design, StealthVault brings the web 3.0 premium SaaS look into the desktop Java ecosystem.
