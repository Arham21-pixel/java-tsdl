:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: StealthVault Quick Setup & Run
:: This compiles and runs the complete application
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

@echo off
setlocal enabledelayedexpansion

cls
echo.
echo  ╔════════════════════════════════════════╗
echo  ║   STEALTH VAULT - QUICK START          ║
echo  ║   Secure Password Manager              ║
echo  ╚════════════════════════════════════════╝
echo.

:: Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found!
    echo Please install Java from: https://www.oracle.com/java/technologies/downloads/
    echo.
    pause
    exit /b 1
)

echo ✓ Java is installed
echo.

:: Create output directories
if not exist "bin" mkdir bin
if not exist "data" mkdir data
if not exist "data\vault" mkdir data\vault

echo.
echo Compiling StealthVault...
echo This may take a moment...
echo.

:: Try to compile with Gradle first (if available)
call gradle --version >nul 2>&1
if not errorlevel 1 (
    echo Using Gradle...
    call gradle clean build -q
    if not errorlevel 1 (
        echo Build successful with Gradle!
        call gradle run
        exit /b 0
    )
)

:: Fallback: Try with just Maven/manual compilation
echo.
echo Attempting manual compilation...
echo (Note: This requires JavaFX SDK to be installed)
echo.

set JAVA_MODULES=javafx.controls,javafx.graphics

:: Try common JavaFX SDK locations (newest first)
if exist "C:\javafx-sdk-26\lib" (
    set JAVAFX_PATH=C:\javafx-sdk-26\lib
    goto compile
)

if exist "C:\Program Files\javafx-sdk-26\lib" (
    set JAVAFX_PATH=C:\Program Files\javafx-sdk-26\lib
    goto compile
)

if exist "C:\javafx-sdk-23\lib" (
    set JAVAFX_PATH=C:\javafx-sdk-23\lib
    goto compile
)

if exist "C:\javafx-sdk-22\lib" (
    set JAVAFX_PATH=C:\javafx-sdk-22\lib
    goto compile
)

if exist "C:\javafx-sdk-21\lib" (
    set JAVAFX_PATH=C:\javafx-sdk-21\lib
    goto compile
)

if exist "C:\Program Files\javafx-sdk-21\lib" (
    set JAVAFX_PATH=C:\Program Files\javafx-sdk-21\lib
    goto compile  
)

if exist "%JAVA_HOME%\..\javafx-sdk-21\lib" (
    set JAVAFX_PATH=%JAVA_HOME%\..\javafx-sdk-21\lib
    goto compile
)

echo WARNING: JavaFX SDK not found in common locations
echo.
echo Would you like to:
echo 1. Download JavaFX SDK (requires browser + ~300MB)
echo 2. Specify custom JavaFX SDK path
echo 3. Try using available system libraries
echo.
set /p choice="Enter 1-3: "

if "%choice%"=="1" (
    echo Opening JavaFX download page...
    start https://gluonhq.com/products/javafx/
    echo.
    echo Please:
    echo 1. Download the Windows SDK
    echo 2. Extract to C:\javafx-sdk-21
    echo 3. Run this script again
    echo.
    pause
    exit /b 1
)

if "%choice%"=="2" (
    set /p JAVAFX_PATH="Enter path to javafx SDK lib folder (e.g., C:\javafx-sdk-21\lib): "
    goto compile
)

:compile
if not exist "!JAVAFX_PATH!" (
    echo ERROR: JavaFX path not found: !JAVAFX_PATH!
    echo.
    echo Please download JavaFX from: https://gluonhq.com/products/javafx/
    echo Extract to: C:\javafx-sdk-21
    echo.
    pause
    exit /b 1
)

echo Compiling with JavaFX from: !JAVAFX_PATH!
echo.

:: Copy resources to bin
if not exist "bin\styles.css" (
    copy resources\styles.css bin\ >nul 2>&1
)

:: Compile ALL source files (auth, crypto, storage, recovery, ui, Main)
javac --module-path "!JAVAFX_PATH!" ^
      --add-modules %JAVA_MODULES% ^
      -d bin ^
      src\auth\PasswordUtils.java ^
      src\auth\AuthManager.java ^
      src\crypto\AESEncryption.java ^
      src\crypto\KeyManager.java ^
      src\storage\FileHandler.java ^
      src\storage\VaultStorage.java ^
      src\storage\VaultService.java ^
      src\recovery\SecurityQuestions.java ^
      src\recovery\ExportManager.java ^
      src\ui\VaultItem.java ^
      src\ui\AddItemDialog.java ^
      src\ui\LoginScreen.java ^
      src\ui\VaultDashboard.java ^
      src\Main.java

if errorlevel 1 (
    echo.
    echo COMPILATION FAILED!
    echo.
    echo This usually means JavaFX SDK is not properly installed.
    echo.
    echo Solution:
    echo 1. Visit: https://gluonhq.com/products/javafx/
    echo 2. Download: Windows SDK (matching your Java version)
    echo 3. Extract to: C:\javafx-sdk-21
    echo 4. Run this script again
    echo.
    pause
    exit /b 1
)

echo.
echo ✓ Compilation successful!
echo   - auth (AuthManager, PasswordUtils)
echo   - crypto (AESEncryption, KeyManager)
echo   - storage (FileHandler, VaultStorage, VaultService)
echo   - recovery (SecurityQuestions, ExportManager)
echo   - ui (LoginScreen, VaultDashboard, AddItemDialog, VaultItem)
echo   - Main
echo.
echo ╔════════════════════════════════════════╗
echo ║   STARTING STEALTHVAULT...            ║
echo ╚════════════════════════════════════════╝
echo.

java --module-path "!JAVAFX_PATH!" ^
     --add-modules %JAVA_MODULES% ^
     -cp bin ^
     Main

echo.
echo StealthVault closed.
echo.
pause
