#!/usr/bin/env pwsh

Write-Host "StealthVault AutoSetup" -ForegroundColor Cyan
Write-Host ""

# Check Java
Write-Host "Checking Java..." -ForegroundColor Yellow
try {
    &java -version 2>&1 | Out-Null
    Write-Host "Java is installed" -ForegroundColor Green
} catch {
    Write-Host "Java not found!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Checking for JavaFX SDK..." -ForegroundColor Yellow

$javafxFound = $false
$JAVAFX_HOME = ""

if (Test-Path "C:\javafx-sdk-21\lib") {
    Write-Host "Found JavaFX at: C:\javafx-sdk-21" -ForegroundColor Green
    $JAVAFX_HOME = "C:\javafx-sdk-21"
    $javafxFound = $true
}

if (-not $javafxFound) {
    Write-Host "JavaFX SDK not found - needs to be installed" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Please download and install:" -ForegroundColor Cyan
    Write-Host "https://gluonhq.com/products/javafx/" -ForegroundColor White
    Write-Host ""
    Write-Host "Steps:" -ForegroundColor Green
    Write-Host "1. Download Windows SDK (x64)"
    Write-Host "2. Extract to: C:\javafx-sdk-21"
    Write-Host "3. Run this script again"
    Write-Host ""
    exit 1
}

Write-Host "Building StealthVault..." -ForegroundColor Yellow
Write-Host ""

$JAVAFX_LIB = "$JAVAFX_HOME\lib"

# Create bin directory
if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" -OutVariable $null
}

# Compile
Write-Host "Compiling..." -ForegroundColor Cyan
javac --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.graphics -d bin src/Main.java src/ui/LoginScreen.java src/ui/VaultDashboard.java src/ui/VaultItem.java src/ui/AddItemDialog.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}

Write-Host "Compilation successful!" -ForegroundColor Green
Write-Host ""
Write-Host "Launching StealthVault..." -ForegroundColor Green
Write-Host ""

# Run
java --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.graphics -cp bin Main
