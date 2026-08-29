# download_models.ps1

$AssetsDir = "app/src/main/assets/models"
New-Item -ItemType Directory -Force -Path "$AssetsDir/stt"
New-Item -ItemType Directory -Force -Path "$AssetsDir/tts"

Write-Host "Downloading STT Model (Sherpa-ONNX Zipformer INT8)..."
$SttUrl = "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-ncnn-zipformer-en-2023-02-13.tar.bz2"
Invoke-WebRequest -Uri $SttUrl -OutFile "stt_model.tar.bz2"

Write-Host "Downloading TTS Model (VITS / Piper)..."
$TtsUrl = "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-en_US-amy-low.tar.bz2"
Invoke-WebRequest -Uri $TtsUrl -OutFile "tts_model.tar.bz2"

Write-Host "Download complete!"
Write-Host "Please extract stt_model.tar.bz2 into app/src/main/assets/models/stt/"
Write-Host "Please extract tts_model.tar.bz2 into app/src/main/assets/models/tts/"
