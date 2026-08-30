#!/bin/bash
set -e

ASSETS_DIR="app/src/main/assets/models"
LIBS_DIR="app/libs"

# Create directories
echo "Creating directories..."
mkdir -p "$ASSETS_DIR/stt"
mkdir -p "$ASSETS_DIR/tts/en"
mkdir -p "$ASSETS_DIR/tts/hi"
mkdir -p "$LIBS_DIR"

# Download and extract STT Model (Whisper Tiny Multilingual INT8)
echo -e "\nDownloading STT Model (Whisper Tiny INT8 Multilingual)..."
STT_URL="https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-whisper-tiny.tar.bz2"
curl -L -o stt_model.tar.bz2 "$STT_URL"
echo "Extracting STT model..."
tar -xf stt_model.tar.bz2
rm -rf "$ASSETS_DIR/stt/whisper"
mv sherpa-onnx-whisper-tiny "$ASSETS_DIR/stt/whisper"
rm stt_model.tar.bz2

# Download and extract English TTS Model (Piper)
echo -e "\nDownloading English TTS Model (Piper en_US-amy-low)..."
TTS_EN_URL="https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-en_US-amy-low.tar.bz2"
curl -L -o tts_en_model.tar.bz2 "$TTS_EN_URL"
echo "Extracting English TTS model..."
tar -xf tts_en_model.tar.bz2
mv vits-piper-en_US-amy-low/* "$ASSETS_DIR/tts/en/"
rm -rf vits-piper-en_US-amy-low
rm tts_en_model.tar.bz2

# Download and extract Hindi TTS Model (Piper)
echo -e "\nDownloading Hindi TTS Model (Piper hi_IN-swara-low)..."
TTS_HI_URL="https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-hi_IN-swara-low.tar.bz2"
curl -L -o tts_hi_model.tar.bz2 "$TTS_HI_URL"
echo "Extracting Hindi TTS model..."
tar -xf tts_hi_model.tar.bz2
mv vits-piper-hi_IN-swara-low/* "$ASSETS_DIR/tts/hi/"
rm -rf vits-piper-hi_IN-swara-low
rm tts_hi_model.tar.bz2

# Download Sherpa-ONNX AAR
echo -e "\nDownloading Sherpa-ONNX AAR v1.13.6..."
AAR_URL="https://github.com/k2-fsa/sherpa-onnx/releases/download/v1.13.6/sherpa-onnx-1.13.6.aar"
curl -L -o "$LIBS_DIR/sherpa-onnx-1.13.6.aar" "$AAR_URL"

echo -e "\nDownload complete!"
echo "Directory tree for models:"
find "$ASSETS_DIR"
