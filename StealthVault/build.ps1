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

# Create bin directory if it doesn't exist
if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

# Compile the project
javac --module-path "$JAVAFX_MODS" `
      --add-modules javafx.controls,javafx.graphics `
      -d bin `
      src/Main.java `
      src/ui/LoginScreen.java `
      src/ui/VaultDashboard.java `
      src/ui/VaultItem.java `
      src/ui/AddItemDialog.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    Exit 1
}

Write-Host "Compilation successful!" -ForegroundColor Green
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
