import pytest
import fakeredis.aioredis
from src.services.mock_kroger import MockKrogerAdapter
from src.services.stripe_client import StripePaymentService
from src.models.schemas import PreferenceProfileSchema
from src.agent.tools import GroceryAgentTools

@pytest.fixture
def fake_redis():
    return fakeredis.aioredis.FakeRedis(decode_responses=True)

@pytest.fixture
def mock_merchant():
    return MockKrogerAdapter()

@pytest.fixture
def stripe_service():
    return StripePaymentService()

@pytest.fixture
def emitted_events():
    events = []
    async def emitter(topic: str, payload_str: str):
        import json
        events.append({"topic": topic, "data": json.loads(payload_str)})
    emitter.events = events
    return emitter

@pytest.fixture
def agent_tools(fake_redis, mock_merchant, stripe_service, emitted_events):
    prefs = PreferenceProfileSchema(
        allergies=["peanuts"],
        dietary_flags=["organic_only"],
        brand_preferences={"milk": "Simple Truth"}
    )
    return GroceryAgentTools(
        session_id="test-session-123",
        user_id="user-456",
        merchant_adapter=mock_merchant,
        redis_client=fake_redis,
        stripe_service=stripe_service,
        user_preferences=prefs,
        data_channel_emitter=emitted_events,
    )
