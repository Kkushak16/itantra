# iTantra — Offline Emergency Peer-to-Peer Communicator

**Platform:** Android (Kotlin, Native)  
**Architecture:** MVVM + Clean Architecture + Hilt + Room + Jetpack Compose  
**Primary Loop:** Off-grid voice speech-to-text transmission over local P2P radios with on-device text-to-speech audio playback.

---

## Product Overview

iTantra is a **fully offline**, phone-to-phone emergency walkie-talkie. Two nearby Android devices — without internet, SIM cards, or Wi-Fi routers — discover each other over Wi-Fi Direct and Bluetooth radios, pair, and exchange short transcribed voice messages:

1. **User speaks:** Press and hold the mic button on the Talk screen.
2. **On-device STT:** Converts speech into light text packets using Android `SpeechRecognizer` (`EXTRA_PREFER_OFFLINE`) or Vosk fallback.
3. **P2P Transport:** Transmits serialized JSON payloads over Google Nearby Connections API (`P2P_CLUSTER`).
4. **On-device TTS:** Receiving device converts text into speech and plays it out loud on the speaker automatically.
5. **SOS Emergency Siren:** Long-press SOS triggers a high-priority `STREAM_ALARM` siren, vibration waveform, and flashlight strobe across all paired endpoints.

---

## Tech Stack

| Layer | Technology Choice | Rationale / Details |
|---|---|---|
| **Language** | Kotlin | Coroutines & Flow for asynchronous callback handling |
| **UI** | Jetpack Compose (Material 3) | Emergency dark theme (`#0A0A0A`), Safety Orange (`#FF5A1F`), Alert Red (`#FF2E2E`) |
| **Architecture** | MVVM + Unidirectional State | `StateFlow` and `SharedFlow` reactive architecture |
| **DI** | Hilt (Dagger) | Flavor-based interface binding for `mock` and `prod` source sets |
| **P2P Transport** | Nearby Connections API | Strategy `P2P_CLUSTER` over local Wi-Fi & Bluetooth |
| **Speech-to-Text** | Android STT + Vosk Fallback | Offline speech recognition (`EXTRA_PREFER_OFFLINE = true`) |
| **Text-to-Speech** | System `TextToSpeech` engine | `QUEUE_ADD` audio playback for hands-free walkie-talkie |
| **Audio Alerts** | `AudioManager` + `Vibrator` | `STREAM_ALARM` max volume audio + waveform vibration + safe strobe |
| **Persistence** | Room Database | Local message transcript and peer history persistence |
| **Foreground Service** | `ItantraForegroundService` | Maintains radio session with screen off or backgrounded |

---

## Build Flavors & Development Loop

iTantra features two distinct build flavors configured via Gradle product flavors and Hilt DI modules:

### 1. `mock` (Default / Demo Loop)
- **Command:** `./gradlew assembleMockDebug`
- **Behavior:** Binds `MockTransport` and `MockSpeechToText`. Simulates peer discovery, handshake, network delay spikes, and canned speech responses. Ideal for emulators, CI/CD pipelines, and single-device live demonstrations.

### 2. `prod` (Physical Device Deployment)
- **Command:** `./gradlew assembleProdRelease`
- **Behavior:** Binds `NearbyTransport` (Google Nearby Connections) and `AndroidSttEngine` (system recognizer / Vosk). Connects two physical Android devices over real radios without internet.

---

## Important SOS Capability Disclaimer

iTantra plays emergency SOS alert siren audio on **`AudioManager.STREAM_ALARM`**, which is designed on Android OS to bypass standard hardware ringer mute settings. However, hardware mute switches on specific OEM Android implementations or total-silence Do Not Disturb modes may block audio unless alarm exceptions are allowed by the user.

---

## How to Run the Demo

For a step-by-step 2-minute demonstration script for judges or first responders, see [DEMO_SCRIPT.md](./DEMO_SCRIPT.md).
