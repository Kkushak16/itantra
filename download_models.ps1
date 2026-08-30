# download_models.ps1

$ErrorActionPreference = "Stop"

$AssetsDir = "app/src/main/assets/models"
$LibsDir = "app/libs"

# Create directories
Write-Host "Creating directories..."
New-Item -ItemType Directory -Force -Path "$AssetsDir/stt" | Out-Null
New-Item -ItemType Directory -Force -Path "$AssetsDir/tts/en" | Out-Null
New-Item -ItemType Directory -Force -Path "$AssetsDir/tts/hi" | Out-Null
New-Item -ItemType Directory -Force -Path $LibsDir | Out-Null

# Skip STT and EN TTS models (already downloaded)
Start-Sleep -Seconds 2
Copy-Item -Path "vits-piper-en_US-amy-low/*" -Destination "$AssetsDir/tts/en/" -Recurse -Force
Remove-Item -Path "vits-piper-en_US-amy-low" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "tts_en_model.tar.bz2" -Force -ErrorAction SilentlyContinue

# Download and extract Hindi TTS Model (Piper)
Write-Host "`nDownloading Hindi TTS Model (Piper hi_IN-swara-low)..."
$TtsHiUrl = "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-hi_IN-swara-low.tar.bz2"
Invoke-WebRequest -Uri $TtsHiUrl -OutFile "tts_hi_model.tar.bz2"
Write-Host "Extracting Hindi TTS model..."
tar -xf "tts_hi_model.tar.bz2"
Start-Sleep -Seconds 2
Copy-Item -Path "vits-piper-hi_IN-swara-low/*" -Destination "$AssetsDir/tts/hi/" -Recurse -Force
Remove-Item -Path "vits-piper-hi_IN-swara-low" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "tts_hi_model.tar.bz2" -Force -ErrorAction SilentlyContinue

# Download Sherpa-ONNX AAR
Write-Host "`nDownloading Sherpa-ONNX AAR v1.13.6..."
$AarUrl = "https://github.com/k2-fsa/sherpa-onnx/releases/download/v1.13.6/sherpa-onnx-1.13.6.aar"
Invoke-WebRequest -Uri $AarUrl -OutFile "$LibsDir/sherpa-onnx-1.13.6.aar"

Write-Host "`nDownload complete!"
Write-Host "Directory tree for models:"
Get-ChildItem -Path $AssetsDir -Recurse | Select-Object FullName
