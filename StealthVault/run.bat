:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: StealthVault Quick Setup & Run
:: This downloads and compiles everything automatically
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

:: Create bin directory
if not exist "bin" mkdir bin

echo.
echo Compiling StealthVault...
echo This may take a moment...
echo.

:: Try to compile with Gradle first (if available)
call gradle --version >nul 2>&1
if errorlevel 0 (
    echo Using Gradle...
    call gradle clean build -q
    if errorlevel 0 (
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

set JAVA_MODULES=javafx.controls,javafx.graphics,javafx.fxml

:: Try common JavaFX SDK locations
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

javac --module-path "!JAVAFX_PATH!" ^
      --add-modules %JAVA_MODULES% ^
      -d bin ^
      src\Main.java ^
      src\ui\LoginScreen.java ^
      src\ui\VaultDashboard.java ^
      src\ui\VaultItem.java ^
      src\ui\AddItemDialog.java

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
