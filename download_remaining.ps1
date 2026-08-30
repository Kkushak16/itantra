$ErrorActionPreference = "Continue"
$AssetsDir = "app/src/main/assets/models"
$LibsDir = "app/libs"

Write-Host "Downloading Hindi TTS Model..."
$TtsHiUrl = "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-hi_IN-swara-low.tar.bz2"
Invoke-WebRequest -Uri $TtsHiUrl -OutFile "tts_hi_model.tar.bz2"
tar -xf "tts_hi_model.tar.bz2"
Copy-Item -Path "vits-piper-hi_IN-swara-low/*" -Destination "$AssetsDir/tts/hi/" -Recurse -Force
Remove-Item -Path "vits-piper-hi_IN-swara-low" -Recurse -Force
Remove-Item -Path "tts_hi_model.tar.bz2" -Force

Write-Host "Downloading AAR..."
$AarUrl = "https://github.com/k2-fsa/sherpa-onnx/releases/download/v1.13.6/sherpa-onnx-1.13.6.aar"
Invoke-WebRequest -Uri $AarUrl -OutFile "$LibsDir/sherpa-onnx-1.13.6.aar"
Write-Host "Done!"
