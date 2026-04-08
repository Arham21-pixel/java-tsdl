# StealthVault Build and Run Script for PowerShell
# Make sure Java is installed and JavaFX SDK is set up

# Set your JavaFX SDK path here
$JAVAFX_HOME = "C:\javafx-sdk-21"  # Update this to your JavaFX SDK location
$JAVAFX_MODS = "$JAVAFX_HOME\lib"

if (-not (Test-Path $JAVAFX_MODS)) {
    Write-Host "ERROR: JavaFX SDK not found at $JAVAFX_HOME" -ForegroundColor Red
    Write-Host "Please install JavaFX SDK from https://gluonhq.com/products/javafx/" -ForegroundColor Yellow
    Write-Host "Or update JAVAFX_HOME variable in this script" -ForegroundColor Yellow
    Exit 1
}

Write-Host "Compiling StealthVault..." -ForegroundColor Cyan
Write-Host ""

# Create required directories
if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}
if (-not (Test-Path "data")) {
    New-Item -ItemType Directory -Path "data" | Out-Null
}
if (-not (Test-Path "data/vault")) {
    New-Item -ItemType Directory -Path "data/vault" | Out-Null
}

# Copy resources
if (Test-Path "resources/styles.css") {
    Copy-Item "resources/styles.css" "bin/" -Force
}

# Compile ALL source files across every package
javac --module-path "$JAVAFX_MODS" `
      --add-modules javafx.controls,javafx.graphics `
      -d bin `
      src/auth/PasswordUtils.java `
      src/auth/AuthManager.java `
      src/crypto/AESEncryption.java `
      src/crypto/KeyManager.java `
      src/storage/FileHandler.java `
      src/storage/VaultStorage.java `
      src/storage/VaultService.java `
      src/recovery/SecurityQuestions.java `
      src/recovery/ExportManager.java `
      src/ui/VaultItem.java `
      src/ui/AddItemDialog.java `
      src/ui/LoginScreen.java `
      src/ui/VaultDashboard.java `
      src/Main.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    Exit 1
}

Write-Host "Compilation successful!" -ForegroundColor Green
Write-Host "  - auth (AuthManager, PasswordUtils)"
Write-Host "  - crypto (AESEncryption, KeyManager)"
Write-Host "  - storage (FileHandler, VaultStorage, VaultService)"
Write-Host "  - recovery (SecurityQuestions, ExportManager)"
Write-Host "  - ui (LoginScreen, VaultDashboard, AddItemDialog, VaultItem)"
Write-Host "  - Main"
Write-Host ""
Write-Host "Running StealthVault..." -ForegroundColor Cyan
Write-Host ""

# Run the application
java --module-path "$JAVAFX_MODS" `
     --add-modules javafx.controls,javafx.graphics `
     -cp bin `
     Main

Write-Host ""
Write-Host "StealthVault closed." -ForegroundColor Green
