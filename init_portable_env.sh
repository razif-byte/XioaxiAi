#!/bin/bash

# Navigate to script folder (USB directory)
cd "$(dirname "$0")"
PORTABLE_DIR="$(pwd)"
VENV_DIR="$PORTABLE_DIR/venv"
MODELS_DIR="$PORTABLE_DIR/models"
export WHISPER_CACHE_DIR="$MODELS_DIR/whisper"
export HF_HOME="$MODELS_DIR/huggingface"

echo "========================================================"
echo "🤖 XIAOXI PORTABLE AI - ENVIRONMENT INITIALIZER (UNIX)"
echo "========================================================"
echo "This script sets up a fully self-contained Python venv"
echo "and downloads offline Vosk/Whisper models directly on your USB."
echo "========================================================"
echo

mkdir -p "$MODELS_DIR"
mkdir -p "$WHISPER_CACHE_DIR"
mkdir -p "$HF_HOME"

# Check Python3
echo "[1/4] Checking for Python..."
if ! command -v python3 &> /dev/null; then
    echo "❌ Python3 is not installed on this system. Please install python3 first."
    exit 1
fi
echo "✓ Python3 found."

# Create virtual environment
echo
echo "[2/4] Setting up Virtual Environment..."
if [ ! -d "$VENV_DIR" ]; then
    echo "Creating virtual environment in $VENV_DIR..."
    python3 -m venv "$VENV_DIR"
    if [ $? -ne 0 ]; then
        echo "❌ Failed to create virtual environment. Installing python3-venv might be required."
        echo "On Ubuntu/Debian, try: sudo apt install python3-venv"
        exit 1
    fi
fi

# Activate virtual environment
source "$VENV_DIR/bin/activate"

# Install Dependencies
echo
echo "[3/4] Installing dependencies inside Virtual Env..."
pip install --upgrade pip
echo "Installing speech-to-text libraries (Vosk, SoundDevice, NumPy, Requests)..."
# On some Linux distros sounddevice needs libportaudio2
if command -v apt-get &> /dev/null; then
    echo "Note: If audio recording fails, you might need portaudio (sudo apt install libportaudio2)"
fi
pip install vosk sounddevice numpy requests

echo
read -p "Would you also like to install OpenAI Whisper? (Requires PyTorch, large download) (y/N): " whisper_choice
if [[ "$whisper_choice" =~ ^[Yy]$ ]]; then
    echo "Installing Whisper..."
    pip install openai-whisper
fi

# Download Vosk Model
echo
echo "[4/4] Setting up speech models..."
if [ ! -d "$MODELS_DIR/vosk-model-small-en-us" ]; then
    echo "Local speech model not found. Downloading lightweight Vosk English model (approx 40MB)..."
    curl -L -o vosk-model.zip https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip
    if [ -f vosk-model.zip ]; then
        echo "Extracting model..."
        unzip vosk-model.zip -d "$MODELS_DIR"
        mv "$MODELS_DIR/vosk-model-small-en-us-0.15" "$MODELS_DIR/vosk-model-small-en-us"
        rm vosk-model.zip
        echo "✓ Vosk model configured successfully."
    else
        echo "⚠️ Failed to download Vosk model automatically. You can download manually from Alphacephei and extract to $MODELS_DIR."
    fi
else
    echo "✓ Vosk model already exists on USB drive."
fi

echo
echo "========================================================"
echo "🎉 PORTABLE ENVIRONMENT SETUP COMPLETE!"
echo "========================================================"
echo
read -p "Press Enter to launch 'portable_companion.py' now..."
python "$PORTABLE_DIR/portable_companion.py"
