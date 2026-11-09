import pytest
from httpx import AsyncClient, ASGITransport
from src.api.main import app
from src.core.redis import get_redis
from src.models.schemas import CartStateSchema, CartItemSchema

@pytest.fixture(autouse=True)
def override_redis_dep(fake_redis):
    app.dependency_overrides[get_redis] = lambda: fake_redis
    yield
    app.dependency_overrides.clear()

@pytest.mark.asyncio
async def test_health_endpoint():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        res = await ac.get("/health")
        assert res.status_code == 200
        assert res.json()["status"] == "ok"

@pytest.mark.asyncio
async def test_voice_token_endpoint():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        payload = {
            "user_id": "test_user_1",
            "room_name": "room_grocery_1",
            "preferences": {"allergies": ["peanuts"]},
        }
        res = await ac.post("/api/v1/voice/token", json=payload)
        assert res.status_code == 200
        data = res.json()
        assert "token" in data
        assert data["room_name"] == "room_grocery_1"

@pytest.mark.asyncio
async def test_get_cart_empty():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        res = await ac.get("/api/v1/cart/non_existent_session")
        assert res.status_code == 200
        data = res.json()
        assert data["session_id"] == "non_existent_session"
        assert data["items"] == []

@pytest.mark.asyncio
async def test_checkout_confirm_empty_cart():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        payload = {
            "session_id": "empty_session",
            "user_id": "user_123",
            "biometric_signature": "sig_face_id_ok",
        }
        res = await ac.post("/api/v1/checkout/confirm", json=payload)
        assert res.status_code == 400

@pytest.mark.asyncio
async def test_checkout_confirm_with_items(fake_redis):
    # Seed active cart
    cart = CartStateSchema(
        session_id="active_session_1",
        user_id="user_123",
        items=[
            CartItemSchema(
                sku="KR-MILK-ORG-GAL",
                title="Simple Truth Organic Whole Milk",
                brand="Simple Truth",
                quantity=1.0,
                unit="gallon",
                unit_price=5.49,
                total_price=5.49,
                is_organic=True,
            )
        ],
        subtotal=5.49,
        tax=0.45,
        delivery_fee=3.99,
        cart_total=9.93,
    )
    await fake_redis.set("cart:active_session_1", cart.model_dump_json())

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        payload = {
            "session_id": "active_session_1",
            "user_id": "user_123",
            "biometric_signature": "face_id_valid",
        }
        res = await ac.post("/api/v1/checkout/confirm", json=payload)
        assert res.status_code == 200
        data = res.json()
        assert data["status"] == "COMPLETED"
        assert "KROG-" in data["order"]["order_id"]
        assert data["payment"]["status"] == "succeeded"

    # Verify cart deleted after checkout
    remaining = await fake_redis.get("cart:active_session_1")
    assert remaining is None
