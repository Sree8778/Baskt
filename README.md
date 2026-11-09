# Baskt: Autonomous Voice & Grocery Assistant Ecosystem

An end-to-end intelligent voice ecosystem combining **autonomous grocery ordering** with **real-world telephonic voice calling** for customer support and friend coordination.

[![GitHub Repo](https://img.shields.io/badge/GitHub-Sree8778%2FBaskt-blue?logo=github)](https://github.com/Sree8778/Baskt)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20FastAPI%20%7C%20LiveKit-green)](https://github.com/Sree8778/Baskt)

---

## 📖 Complete Documentation
For the complete app agenda, detailed architecture breakdown, and future roadmap, read:
👉 **[PROJECT_AGENDA_AND_ROADMAP.md](docs/PROJECT_AGENDA_AND_ROADMAP.md)**

---

## 🌟 Ecosystem Architecture

### 1. `VoiceCareAgent/` (Android Autonomous Telephony)
- **Cellular Outbound Calls:** Places calls over cellular SIM (`Intent.ACTION_CALL`) to real telephone numbers.
- **Dual Persona Engine:**
  - **Customer Care Mode:** Navigates automated phone menus, handles order disputes, flight cancellations, and internet outages.
  - **Friends & Personal Mode:** Coordinates dinner plans, study sessions, gym workouts, and appointments with customizable topics.
- **🛡️ Safe Contact Allowlist:** Restricts calling strictly to user-selected sandbox contacts via an in-app multi-select contact picker and search.
- **Duplex Conversational Turn-Taking:** Dynamic STT + TTS loop that introduces itself, pauses, listens to the callee, and responds contextually without speaking over them.

### 2. `SupportAgent/` (Android Autonomous Shopping Companion)
- **Visual-First (Anti-Monologue):** Pushes interactive grocery flashcards to the screen rather than reading long audio catalogs.
- **Ghost Touch Automation:** Android Accessibility Service to execute automated searches and additions across retailer apps.
- **Multi-Store Aggregator:** Compares prices and inventory across Walmart, Aldi, and Instacart.
- **P2P Delivery Marketplace:** Direct driver bidding and live two-way customer-driver chat.

### 3. `src/` (FastAPI & LiveKit WebRTC Cloud Backend)
- **LiveKit Agents:** Real-time WebRTC room with Deepgram Nova-2 streaming STT and Cartesia streaming TTS.
- **Allergy & Dietary Guards:** Automatically screens item additions against dietary restrictions (gluten, peanut, dairy, etc.).
- **Merchant Integration Layer:** Extensible adapter architecture with mock and live store interfaces.

---

## 🚀 Getting Started

### Backend Setup
```bash
python -m venv .venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate
pip install -r requirements.txt
uvicorn src.api.main:app --reload
```

### Android Apps
- Open `VoiceCareAgent/` or `SupportAgent/` in Android Studio or compile with Gradle:
```bash
cd VoiceCareAgent && ./gradlew assembleDebug
```
