@echo off
REM StealthVault Build and Run Script
REM Make sure JavaFX SDK is installed and JAVAFX_HOME is set

setlocal enabledelayedexpansion

REM Set your JavaFX SDK path here (update if needed)
set JAVAFX_HOME=C:\javafx-sdk-21
set JAVAFX_MODS=%JAVAFX_HOME%\lib

if not exist "%JAVAFX_MODS%" (
    echo ERROR: JavaFX SDK not found at %JAVAFX_HOME%
    echo Please install JavaFX SDK from https://gluonhq.com/products/javafx/
    echo Or update JAVAFX_HOME variable in this script
    pause
    exit /b 1
)

echo Compiling StealthVault...
javac --module-path "%JAVAFX_MODS%" ^
      --add-modules javafx.controls,javafx.graphics ^
      -d bin ^
      src\Main.java ^
      src\ui\LoginScreen.java ^
      src\ui\VaultDashboard.java ^
      src\ui\VaultItem.java ^
      src\ui\AddItemDialog.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo.
echo Running StealthVault...
echo.

java --module-path "%JAVAFX_MODS%" ^
     --add-modules javafx.controls,javafx.graphics ^
     -cp bin ^
     Main

pause
