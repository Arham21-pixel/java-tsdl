# StealthVault - Automatic JavaFX Setup Script
# This script downloads and sets up JavaFX SDK for you

$ErrorActionPreference = "Stop"

Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   StealthVault JavaFX Auto-Setup      ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Detect Windows version and architecture
$OSArchitecture = $env:PROCESSOR_ARCHITECTURE
if ($OSArchitecture -eq "AMD64") {
    $arch = "x64"
} else {
    $arch = "x86"
}

Write-Host "System Architecture: $arch" -ForegroundColor Green
Write-Host ""

# Download URL for JavaFX SDK 21 (or you can use 23)
$javafxVersion = "21.0.4"
$downloadUrl = "https://gluonhq.com/download/javafx-21-0-4-sdk-windows/"
$javafxPath = "C:\javafx-sdk-21"

Write-Host "This script will download JavaFX SDK to: $javafxPath" -ForegroundColor Yellow
Write-Host ""
Write-Host "WARNING: This requires an internet connection and ~300MB of disk space" -ForegroundColor Yellow
Write-Host ""
Write-Host "Options:" -ForegroundColor Cyan
Write-Host "1. Download and install JavaFX SDK automatically (requires internet)"
Write-Host "2. Manual download instructions"
Write-Host "3. Skip (you must manually install JavaFX)"
Write-Host ""

$choice = Read-Host "Enter your choice (1-3)"

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "NOTE: Due to licensing, we can't auto-download directly." -ForegroundColor Yellow
        Write-Host "Please follow these steps:" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "1. Open: https://gluonhq.com/products/javafx/" -ForegroundColor White
        Write-Host "2. Download: 'Windows SDK' (full version matching your Java version)"
        Write-Host "3. Extract to: C:\javafx-sdk-21" -ForegroundColor White
        Write-Host "4. Re-run: .\build.ps1" -ForegroundColor White
        Write-Host ""
        Start-Process "https://gluonhq.com/products/javafx/"
        Write-Host "Opening download page in your browser..." -ForegroundColor Green
        Read-Host "Press Enter after you've downloaded and extracted JavaFX"
    }
    "2" {
        Write-Host ""
        Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
        Write-Host "║   Manual JavaFX Setup Instructions    ║" -ForegroundColor Cyan
        Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Step 1: Download JavaFX SDK" -ForegroundColor Green
        Write-Host "  - Visit: https://gluonhq.com/products/javafx/" -ForegroundColor White
        Write-Host "  - Download 'Windows SDK' (pick one matching your Java 25 version)"
        Write-Host "  - Size: ~300MB"
        Write-Host ""
        Write-Host "Step 2: Extract the ZIP file" -ForegroundColor Green
        Write-Host "  - Extract to: C:\javafx-sdk-21" -ForegroundColor White
        Write-Host "  - Your folder structure should look like:"
        Write-Host "    C:\javafx-sdk-21\"
        Write-Host "    ├── bin\"
        Write-Host "    ├── legal\"
        Write-Host "    ├── lib\" -ForegroundColor Yellow
        Write-Host "    ├── mods\"
        Write-Host "    └── ..."
        Write-Host ""
        Write-Host "Step 3: Verify Installation" -ForegroundColor Green
        Write-Host "  - Open PowerShell and run: " -ForegroundColor White
        Write-Host "    dir C:\javafx-sdk-21\lib" -ForegroundColor Cyan
        Write-Host "  - You should see .jar files like javafx.base.jar, etc."
        Write-Host ""
        Write-Host "Step 4: Run StealthVault" -ForegroundColor Green
        Write-Host "  - Open PowerShell in the StealthVault folder"
        Write-Host "  - Run: .\build.ps1" -ForegroundColor Cyan
        Write-Host ""
    }
    "3" {
        Write-Host ""
        Write-Host "You can still manually set up JavaFX and run the build script later." -ForegroundColor Yellow
        Write-Host "See SETUP_GUIDE.md for detailed instructions." -ForegroundColor White
        Write-Host ""
    }
    default {
        Write-Host "Invalid choice. Exiting." -ForegroundColor Red
        Exit 1
    }
}

Write-Host ""
Write-Host "Need help? Check SETUP_GUIDE.md for complete documentation." -ForegroundColor Cyan
Write-Host ""
