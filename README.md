# 📡 iTantra Neural Transceiver

**iTantra** is a 100% offline, peer-to-peer "Neural Walkie-Talkie" Android application built for emergency response, disaster relief, and off-grid communication scenarios (Developed for ISRO Problem Statement SIH26173). 

Instead of streaming raw voice data which requires high bandwidth, iTantra uses on-device machine learning to **transcribe** your voice to text (STT), transmits the microscopic text payload over a local ad-hoc network, and **synthesizes** it back into a human voice (TTS) on the receiving device. This ultra-low bitrate approach ensures reliable communication even over degraded RF/Wi-Fi links.

---

## ✨ Features
- **100% Offline Operation:** No cloud APIs or internet connection required. All ML inference runs locally on the device.
- **Multilingual Support (10 Indian Languages):** Supports Hindi, Gujarati, Marathi, Kannada, Malayalam, Tamil, Telugu, Odia, Bengali, and English.
- **Zero-Config P2P Discovery:** Automatically discovers nearby peers using UDP beacons over a local Wi-Fi Hotspot. 
- **Ultra-Low Bandwidth:** Transmits lightweight JSON text payloads instead of heavy audio streams.
- **Emergency Broadcast Mode:** SOS packets override receiver volume and trigger tactical device vibrations (bypassing Silent mode) to ensure critical alerts are heard.
- **Tactical Dark UI:** Built with Jetpack Compose featuring a high-contrast, military-inspired aesthetic with real-time audio visualizers.

---

## 🛠️ Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** Clean Architecture (MVVM + StateFlow/SharedFlow)
- **Audio Pipeline:** `AudioRecord` (16kHz PCM capture) & `AudioTrack` (Raw PCM playback)
- **Networking:** Raw TCP/UDP Sockets (Zero-config UDP Discovery + TCP persistent connections)
- **Machine Learning:** [k2-fsa/sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx) (for quantized INT8 ONNX models)

---

## 🧠 Machine Learning Engine (Sherpa-ONNX)

iTantra is designed to run the official lightweight **Sherpa-ONNX** models for fast, on-device transcription and synthesis. 

### Development (Mock Mode)
To allow developers to build and test the app without downloading 100MB+ of ML models, the app includes a **Mock Fallback Engine**. 
If the app cannot find the ONNX models in the `assets/` folder, it will gracefully boot into Mock Mode. It will simulate a transmission delay, transcribe a hardcoded SOS message, and generate a 440Hz sine wave beep instead of a human voice on the receiving end.

### Production (Real Models)
To enable the real Neural Engine, you must download the models and the native library.
A helper script is included in the repository root: `download_models.ps1`.

1. Run the script or manually download the recommended models from the [k2-fsa releases](https://github.com/k2-fsa/sherpa-onnx/releases).
   - **STT (Speech-to-Text):** Download `sherpa-onnx-ncnn-zipformer-en-2023-02-13.tar.bz2` (or the multi-language equivalent).
   - **TTS (Text-to-Speech):** Download `vits-piper-en_US-amy-low.tar.bz2`.
2. Extract the STT model files into: `app/src/main/assets/models/stt/`
3. Extract the TTS model files into: `app/src/main/assets/models/tts/`
4. Download the latest Android library (`sherpa-onnx-X.X.XX-android.aar`) from k2-fsa and place it in the `app/libs/` directory.

Once the models and AAR are present, the app will automatically switch from Mock Mode to Production Mode.

---

## 🚀 Installation & Testing

### 1. Build and Install
Enable **Developer Options** and **USB Debugging** on your Android devices, connect them via USB, and run:
```bash
./gradlew installDebug
```

### 2. Connect the Devices (No Internet Required!)
1. Turn on the **Mobile Hotspot** on Device A.
2. Connect Device B to Device A's Hotspot Wi-Fi network.

### 3. Pair and Communicate
1. Open the iTantra app on both devices.
2. On Device A, tap the Antenna icon at the top right and select **🖥️ HOST — Start Server**.
3. On Device B, open the Connection Dialog and tap **🔍 SCAN & PAIR**.
4. Device B will automatically discover Device A. Tap the connect button that appears.
5. The status bar will turn green. Hold the **MIC** button to speak, and release it to transmit!

---

## 📄 License
This project was developed for the Smart India Hackathon (SIH26173). 
