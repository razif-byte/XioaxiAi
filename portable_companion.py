#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Xiaoxi Portable AI - Portable Companion Script
This Python script runs on the host PC directly from the USB drive. It provides:
1. Registration & heartbeats to the Android app.
2. An HTTP receiver server on port 8080 to trigger actions (such as opening VLC or WhatsApp).
3. Optional offline, real-time voice command recognition via Vosk or Whisper, using the PC's mic.
"""

import sys
import os
import json
import time
import queue
import threading
import subprocess
from http.server import HTTPServer, BaseHTTPRequestHandler

# Import speech-to-text dependencies with safe fallback
try:
    import sounddevice as sd
    import numpy as np
    HAS_AUDIO_DEVICES = True
except ImportError:
    HAS_AUDIO_DEVICES = False
    print("⚠️ sounddevice or numpy not installed. Local live mic transcription is disabled.")

try:
    from vosk import Model, KaldiRecognizer
    HAS_VOSK = True
except ImportError:
    HAS_VOSK = False
    print("⚠️ Vosk speech recognition library not installed. Local live mic transcription is disabled.")

try:
    import requests
    HAS_REQUESTS = True
except ImportError:
    HAS_REQUESTS = False
    print("⚠️ requests library not installed. Connection to Android app is disabled.")


# Global thread-safe queue for audio recording
audio_queue = queue.Queue()

# Default configuration settings
DEFAULT_ANDROID_IP = "192.168.1.15"  # Modify or provide dynamically via arg
ANDROID_PORT = 8080
CLIENT_PORT = 8080

def get_local_ip():
    """Retrieves the local active IPv4 address of this machine."""
    import socket
    try:
        # Create a dummy socket connection to retrieve local IP safely
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"

# Helper to identify platform and host details
CLIENT_IP = get_local_ip()
PC_NAME = os.getenv('COMPUTERNAME', os.getenv('HOSTNAME', 'Portable_PC'))

print(f"====================================================")
print(f"🤖 XIAOXI PORTABLE AI COMPANION (PYTHON CLIENT)")
print(f"====================================================")
print(f"📍 Windows/Unix PC Name: {PC_NAME}")
print(f"📍 Local IP Address   : {CLIENT_IP}")
print(f"📍 Platform           : {sys.platform.upper()}")
print(f"====================================================")

def execute_local_action(action):
    """Executes target system tasks cross-platform based on received actions."""
    try:
        if action == 'LAUNCH_VLC':
            print("🎬 Launching VLC Media Player...")
            if sys.platform == 'win32':
                # Try standard start first
                try:
                    subprocess.Popen(['start', 'vlc'], shell=True)
                except Exception:
                    # Try default install paths
                    common_vlc_paths = [
                        "C:\\Program Files\\VideoLAN\\VLC\\vlc.exe",
                        "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe"
                    ]
                    launched = False
                    for p in common_vlc_paths:
                        if os.path.exists(p):
                            subprocess.Popen([p])
                            launched = True
                            break
                    if not launched:
                        print("❌ Failed to open VLC. Sila pastikan VLC dipasang atau berada dalam PATH.")
            elif sys.platform == 'darwin':
                subprocess.Popen(['open', '-a', 'VLC'])
            else:  # Linux
                subprocess.Popen(['vlc'])
            return True

        elif action == 'LAUNCH_WHATSAPP':
            print("💬 Opening WhatsApp Desktop / Web Client...")
            if sys.platform == 'win32':
                try:
                    subprocess.Popen(['start', 'whatsapp://'], shell=True)
                except Exception:
                    # Fallback to browser
                    subprocess.Popen(['start', 'https://web.whatsapp.com'], shell=True)
            elif sys.platform == 'darwin':
                # Try app link first, then fallback to Web
                try:
                    subprocess.Popen(['open', 'whatsapp://'])
                except Exception:
                    subprocess.Popen(['open', 'https://web.whatsapp.com'])
            else:  # Linux
                subprocess.Popen(['xdg-open', 'https://web.whatsapp.com'])
            return True

        else:
            print(f"❓ Action command '{action}' is unrecognized locally.")
            return False

    except Exception as e:
        print(f"❌ Error during execution: {e}")
        return False

class CompanionHTTPHandler(BaseHTTPRequestHandler):
    """Handles POST requests sent from the Android remote server to the PC."""
    
    def log_message(self, format, *args):
        # Override to suppress default HTTP logs to keep terminal clean
        pass

    def do_POST(self):
        if self.path == '/command':
            content_length = int(self.headers['Content-Length'])
            post_data = self.rfile.read(content_length)
            try:
                payload = json.loads(post_data.decode('utf-8'))
                action = payload.get('action')
                command = payload.get('command', '')

                print(f"\n🎤 Received command from Android: \"{command}\" [Action: {action}]")
                success = execute_local_action(action)

                self.send_response(200)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                self.wfile.write(json.dumps({"status": "success" if success else "failed"}).encode('utf-8'))
            except Exception as e:
                self.send_response(400)
                self.end_headers()
                self.wfile.write(f"Bad Request: {str(e)}".encode('utf-8'))
        else:
            self.send_response(404)
            self.end_headers()

def run_http_server():
    """Runs a local web server to listen to broadcasted commands from Android."""
    server_address = ('', CLIENT_PORT)
    try:
        httpd = HTTPServer(server_address, CompanionHTTPHandler)
        print(f"🎧 Desktop Client HTTP Server listening on port {CLIENT_PORT}...")
        httpd.serve_forever()
    except Exception as e:
        print(f"❌ Failed to start HTTP server on port {CLIENT_PORT}: {e}")

def register_to_android(android_ip):
    """Registers this local PC with the Android Remote AI application."""
    if not HAS_REQUESTS:
        return

    payload = {
        "name": PC_NAME,
        "type": "Windows" if sys.platform == 'win32' else "Mac/Linux"
    }
    url = f"http://{android_ip}:{ANDROID_PORT}/register"
    
    try:
        response = requests.post(url, json=payload, timeout=4)
        if response.status_code == 200:
            print(f"✅ Connection successful! Registered with Android Server at http://{android_ip}:{ANDROID_PORT}")
            # Start heartbeat loop on registration success
            threading.Thread(target=heartbeat_loop, args=(android_ip,), daemon=True).start()
        else:
            print(f"❌ Android server rejected registration. Status: {response.status_code}")
    except Exception as e:
        print(f"⚠️ Failed to connect to Android Server ({e}). Retrying in 5 seconds...")
        # Retry in thread to prevent blocking
        threading.Timer(5.0, register_to_android, args=(android_ip,)).start()

def heartbeat_loop(android_ip):
    """Maintains active status with the Android server by sending a periodic ping."""
    url = f"http://{android_ip}:{ANDROID_PORT}/ping"
    while True:
        try:
            requests.post(url, timeout=3)
        except Exception:
            # Silent fallback if server goes temporarily offline
            pass
        time.sleep(10)

def handle_local_voice_command(text):
    """Processes locally transcribed voice commands."""
    text_lower = text.lower()
    if "buka vlc" in text_lower or "launch vlc" in text_lower:
        execute_local_action("LAUNCH_VLC")
    elif "buka whatsapp" in text_lower or "launch whatsapp" in text_lower:
        execute_local_action("LAUNCH_WHATSAPP")

def audio_callback(indata, frames, time, status):
    """Callback function for the sounddevice microphone recorder stream."""
    if status:
        print(status, file=sys.stderr)
    audio_queue.put(bytes(indata))

def local_speech_to_text_vosk():
    """Listens continuously to the local mic and transcribes using Vosk model."""
    if not (HAS_AUDIO_DEVICES and HAS_VOSK):
        return

    # Look for model inside standard models folder relative to USB root
    models_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'models')
    model_path = os.path.join(models_dir, 'vosk-model-small-en-us')

    if not os.path.exists(model_path):
        print(f"⚠️ Vosk model not found at '{model_path}'. Local offline mic transcription disabled.")
        return

    print(f"🎙️ Loading local speech model from {model_path}...")
    try:
        model = Model(model_path)
        recognizer = KaldiRecognizer(model, 16000)
    except Exception as e:
        print(f"❌ Error initializing Vosk model: {e}")
        return

    print("🎙️ Offline Local Mic Speech Recognition online. You can speak into your mic!")
    
    try:
        with sd.RawInputStream(samplerate=16000, blocksize=8000, dtype='int16',
                               channels=1, callback=audio_callback):
            while True:
                data = audio_queue.get()
                if recognizer.AcceptWaveform(data):
                    res_json = json.loads(recognizer.Result())
                    text = res_json.get('text', '').strip()
                    if text:
                        print(f"🗣️ Offline Speech Detected: '{text}'")
                        handle_local_voice_command(text)
    except Exception as e:
        print(f"⚠️ Audio record stream error: {e}")

if __name__ == "__main__":
    # 1. Ask or parse Android Server IP
    android_ip = DEFAULT_ANDROID_IP
    if len(sys.argv) > 1:
        android_ip = sys.argv[1]
    else:
        ip_input = input(f"Masukkan IP Server Android Xiaoxi (Default: {DEFAULT_ANDROID_IP}): ").strip()
        if ip_input:
            android_ip = ip_input

    # 2. Start HTTP Listener server on a background thread
    server_thread = threading.Thread(target=run_http_server, daemon=True)
    server_thread.start()

    # 3. Connect to Android App remote
    if HAS_REQUESTS:
        print(f"🔗 Attempting to register with Android server at http://{android_ip}:{ANDROID_PORT}...")
        register_to_android(android_ip)
    else:
        print("⚠️ requests package is missing. Android registration skipped. Local-only mode active.")

    # 4. Initialize Local Speech recognition (Vosk)
    if HAS_AUDIO_DEVICES and HAS_VOSK:
        stt_thread = threading.Thread(target=local_speech_to_text_vosk, daemon=True)
        stt_thread.start()

    # Keep main thread alive
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\n👋 Exiting Portable Companion Client...")
