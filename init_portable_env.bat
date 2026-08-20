@echo off
setlocal enabledelayedexpansion
title Xiaoxi Portable AI Environment Initializer

echo ========================================================
echo 🤖 XIAOXI PORTABLE AI - ENVIRONMENT INITIALIZER (WINDOWS)
echo ========================================================
echo This script sets up a fully self-contained Python venv
echo and downloads offline Vosk/Whisper models directly on your USB.
echo No system folders or registry entries on the host PC will be modified.
echo ========================================================
echo.

:: 1. Navigate to USB Drive relative root
cd /d "%~dp0"

:: 2. Set environment variables to keep models and cache on the USB drive
set "PORTABLE_DIR=%CD%"
set "VENV_DIR=%PORTABLE_DIR%\venv"
set "MODELS_DIR=%PORTABLE_DIR%\models"
set "WHISPER_CACHE=%MODELS_DIR%\whisper"
set "HF_HOME=%MODELS_DIR%\huggingface"

echo [1/4] Configuring Portable Paths...
echo - USB Root: !PORTABLE_DIR!
echo - Virtual Env: !VENV_DIR!
echo - Models Directory: !MODELS_DIR!
if not exist "!MODELS_DIR!" mkdir "!MODELS_DIR!"
if not exist "!WHISPER_CACHE!" mkdir "!WHISPER_CACHE!"
if not exist "!HF_HOME!" mkdir "!HF_HOME!"
echo.

:: 3. Check for Python
echo [2/4] Checking for Python...
where python >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ Python was not found in the system PATH.
    echo Portable installation options:
    echo 1. Attempt to download portable Python embeddable zip (Windows x64)
    echo 2. Exit and let me install Python manually
    set /p choice="Select option (1-2): "
    if "!choice!"=="1" (
        echo Downloading portable Python 3.10...
        powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://www.python.org/ftp/python/3.10.11/python-3.10.11-embed-amd64.zip' -OutFile 'python_portable.zip'"
        if exist "python_portable.zip" (
            echo Extracting portable Python...
            powershell -Command "Expand-Archive -Path 'python_portable.zip' -DestinationPath 'python_portable' -Force"
            del python_portable.zip
            echo Configuring portable python...
            :: Enable site-packages in embeddable python (uncomment import site)
            powershell -Command "(Get-Content 'python_portable\python310._pth') -replace '#import site', 'import site' | Set-Content 'python_portable\python310._pth'"
            :: Download pip
            echo Downloading pip for portable Python...
            powershell -Command "Invoke-WebRequest -Uri 'https://bootstrap.pypa.io/get-pip.py' -OutFile 'get-pip.py'"
            .\python_portable\python.exe get-pip.py
            del get-pip.py
            set "PYTHON_EXE=!PORTABLE_DIR!\python_portable\python.exe"
        ) else (
            echo ❌ Failed to download portable Python. Exiting...
            pause
            exit /b 1
        )
    ) else (
        echo Exiting...
        exit /b 1
    )
) else (
    set "PYTHON_EXE=python"
    echo Yes, system Python found.
)

:: 4. Creating/Activating Virtual Environment
if not exist "!VENV_DIR!" (
    echo.
    echo Creating virtual environment in !VENV_DIR!...
    "!PYTHON_EXE!" -m venv "!VENV_DIR!"
    if %errorlevel% neq 0 (
        echo ❌ Failed to create virtual environment. Attempting fallback...
        if exist "!PORTABLE_DIR!\python_portable" (
            echo Fallback: Installing packages directly to portable python site-packages.
            set "PYTHON_RUN=!PORTABLE_DIR!\python_portable\python.exe"
        ) else (
            echo ❌ Please install standard Python with 'venv' module.
            pause
            exit /b 1
        )
    ) else (
        set "PYTHON_RUN=!VENV_DIR!\Scripts\python.exe"
    )
) else (
    echo.
    echo Virtual environment already exists.
    set "PYTHON_RUN=!VENV_DIR!\Scripts\python.exe"
)

:: 5. Install Dependencies
echo.
echo [3/4] Installing dependencies...
"!PYTHON_RUN!" -m pip install --upgrade pip
echo Installing speech-to-text libraries (Vosk, SoundDevice, NumPy, Requests)...
"!PYTHON_RUN!" -m pip install vosk sounddevice numpy requests

echo.
echo Would you also like to install OpenAI Whisper? (Requires PyTorch, large download)
set /p whisper_choice="Install Whisper? (y/n): "
if /i "!whisper_choice!"=="y" (
    echo Installing Whisper...
    "!PYTHON_RUN!" -m pip install openai-whisper
)
echo.

:: 6. Fetch/Configure Vosk Model
echo [4/4] Setting up Speech Models...
if not exist "!MODELS_DIR!\vosk-model-small-en-us" (
    if not exist "!MODELS_DIR!\vosk-model-small-ms" (
        echo Local speech model not found. Downloading lightweight Vosk English model (approx 40MB)...
        powershell -Command "Invoke-WebRequest -Uri 'https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip' -OutFile 'vosk-model.zip'"
        if exist "vosk-model.zip" (
            echo Extracting model to !MODELS_DIR!...
            powershell -Command "Expand-Archive -Path 'vosk-model.zip' -DestinationPath '!MODELS_DIR!' -Force"
            :: Rename extracted folder for ease of use
            move "!MODELS_DIR!\vosk-model-small-en-us-0.15" "!MODELS_DIR!\vosk-model-small-en-us"
            del vosk-model.zip
            echo ✅ Vosk model configured successfully.
        ) else (
            echo ⚠️ Failed to download Vosk model automatically. You can manually download one from 'https://alphacephei.com/vosk/models' and extract it to !MODELS_DIR!
        )
    )
) else (
    echo ✅ Vosk model already exists on USB drive.
)

echo.
echo ========================================================
echo 🎉 PORTABLE ENVIRONMENT SETUP COMPLETE!
echo ========================================================
echo.
echo To run the portable companion client with offline speech-to-text:
echo Press any key to launch 'portable_companion.py' now...
pause >nul

:: Launch Companion Script
"!PYTHON_RUN!" "!PORTABLE_DIR!\portable_companion.py"
pause
