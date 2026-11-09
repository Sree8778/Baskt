# In-App Voice-Guided Grocery Assistant (US Market)

An end-to-end modular, real-time voice-guided grocery shopping assistant for the US market built with LiveKit WebRTC, Deepgram Nova-2, Cartesia TTS, OpenAI LLM, Redis, PostgreSQL, and FastAPI.

## Key Features
- **WebRTC Voice Room**: Connects to LiveKit with bidirectional audio and `cart_sync` WebRTC Data Channel.
- **Voice Pipeline**: Deepgram Nova-2 streaming STT, Silero VAD with aggressive barge-in interruption detection, and Cartesia streaming TTS.
- **US Grocery Intelligence**: Understands US customary units (`gal`, `qt`, `pt`, `fl oz`, `lb`, `oz`, `count`, `carton`), organic vs. conventional distinctions, and store brand preferences.
- **Visual-First (Anti-Monologue)**: Never recites long catalogs over voice; pushes interactive item cards and substitutions directly to the WebRTC Data Channel.
- **Allergy & Dietary Guards**: Intercepts queries and cart additions to check user allergy flags (e.g. peanuts, tree nuts, gluten, dairy) before confirming.
- **Merchant Integration Layer**: Extensible `BaseMerchantAdapter` with `MockKrogerAdapter` and `MockInstacartAdapter`.
- **Payment & Delivery**: Stripe checkout simulation and dynamic delivery time-slot booking.
