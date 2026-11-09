import asyncio
import json
import logging
from typing import Optional

from src.core.config import settings
from src.core.redis import get_redis_client
from src.services.mock_kroger import MockKrogerAdapter
from src.services.stripe_client import StripePaymentService
from src.models.schemas import PreferenceProfileSchema
from src.agent.prompts import GROCERY_AGENT_SYSTEM_PROMPT
from src.agent.tools import GroceryAgentTools

logger = logging.getLogger("grocery-voice-agent")

async def entrypoint(ctx):
    """LiveKit agent worker entrypoint function."""
    logger.info(f"Connecting to LiveKit room: {ctx.room.name}")
    await ctx.connect()

    redis_client = await get_redis_client()
    merchant = MockKrogerAdapter()
    stripe_service = StripePaymentService()

    # Load user profile from room metadata
    user_metadata = {}
    if ctx.room.metadata:
        try:
            user_metadata = json.loads(ctx.room.metadata)
        except Exception:
            pass

    user_id = user_metadata.get("user_id", "guest_user")
    session_id = ctx.room.name
    pref_data = user_metadata.get("preferences", {})
    preferences = PreferenceProfileSchema(**pref_data)

    # Establish WebRTC Data Channel emitter for 'cart_sync'
    async def data_channel_emitter(topic: str, payload_str: str):
        try:
            await ctx.room.local_participant.publish_data(
                payload_str.encode("utf-8"),
                reliable=True,
                topic=topic,
            )
        except Exception as e:
            logger.error(f"Error publishing to data channel: {e}")

    tools = GroceryAgentTools(
        session_id=session_id,
        user_id=user_id,
        merchant_adapter=merchant,
        redis_client=redis_client,
        stripe_service=stripe_service,
        user_preferences=preferences,
        data_channel_emitter=data_channel_emitter,
    )

    try:
        from livekit import agents, rtc
        from livekit.plugins import deepgram, cartesia, openai, silero

        # 1. Silero VAD with aggressive barge-in interruption detection
        vad = silero.VAD.load(
            min_speech_duration=0.1,
            min_silence_duration=0.3,
        )

        # 2. Deepgram Nova-2 Streaming STT
        stt = deepgram.STT(
            model="nova-2-general",
            language="en-US",
            api_key=settings.deepgram_api_key,
        )

        # 3. Cartesia Streaming TTS
        tts = cartesia.TTS(
            model="sonic-english",
            voice="f114a96b-1148-4370-96f3-ee33f7c4eb0c",
            api_key=settings.cartesia_api_key,
        )

        # 4. LLM with function-calling agent
        llm = openai.LLM(
            model="gpt-4o",
            api_key=settings.openai_api_key,
        )

        assistant = agents.VoiceAssistant(
            vad=vad,
            stt=stt,
            llm=llm,
            tts=tts,
            system_message=GROCERY_AGENT_SYSTEM_PROMPT,
            fnc_ctx=tools,
        )

        # Listen for incoming WebRTC Data Channel events from mobile/web client
        @ctx.room.on("data_received")
        def on_data_received(data: rtc.DataPacket):
            try:
                text = data.data.decode("utf-8")
                payload = json.loads(text)
                logger.info(f"Received client data message on topic '{data.topic}': {payload}")
                if payload.get("event") == "SELECT_ITEM":
                    asyncio.create_task(
                        tools.add_to_cart(sku=payload["sku"], quantity=payload.get("quantity", 1.0))
                    )
            except Exception as err:
                logger.warning(f"Failed to parse incoming data packet: {err}")

        assistant.start(ctx.room)
        await assistant.say(
            "Hi there! I'm your grocery assistant. What can I get for you today?",
            allow_interruptions=True,
        )
    except ImportError:
        logger.warning("LiveKit plugins not installed; running in mockable mode.")

if __name__ == "__main__":
    try:
        from livekit.agents import WorkerOptions, cli
        cli.run_app(WorkerOptions(entrypoint_fnc=entrypoint))
    except ImportError:
        logger.error("LiveKit agents CLI requires livekit-agents package.")
