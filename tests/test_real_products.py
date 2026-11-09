import pytest
from httpx import AsyncClient, ASGITransport
from src.api.main import app
from src.core.redis import get_redis
from src.services.real_products_service import get_real_products_for_merchant

@pytest.fixture(autouse=True)
def override_redis_dep(fake_redis):
    app.dependency_overrides[get_redis] = lambda: fake_redis
    yield
    app.dependency_overrides.clear()

def test_real_products_catalog_structure():
    wm_items = get_real_products_for_merchant("walmart")
    kr_items = get_real_products_for_merchant("kroger")

    assert len(wm_items) >= 20
    assert len(kr_items) >= 20

    # Verify authentic packaging images are present
    vital_farms = next(i for i in wm_items if "Vital Farms" in i.title)
    assert "openfoodfacts" in vital_farms.image_url
    assert vital_farms.sku == "WM-EGGS-PAST-12"

    dave_bread = next(i for i in kr_items if "Dave's Killer Bread" in i.title)
    assert "openfoodfacts" in dave_bread.image_url
    assert dave_bread.is_organic is True

    raos = next(i for i in wm_items if "Rao's" in i.title)
    assert "openfoodfacts" in raos.image_url
    assert raos.price == 6.98

@pytest.mark.asyncio
async def test_catalog_endpoint_returns_real_products():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        res = await ac.get("/api/v1/catalog")
        assert res.status_code == 200
        items = res.json()
        assert len(items) >= 40

        titles = [i["title"] for i in items]
        assert any("Fairlife" in t for t in titles)
        assert any("Vital Farms" in t for t in titles)
        assert any("Chobani" in t for t in titles)
        assert any("Dave's Killer Bread" in t for t in titles)
        assert any("Cheerios" in t for t in titles)

@pytest.mark.asyncio
async def test_voice_command_resolves_real_products():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        # Test adding Fairlife milk via voice
        res = await ac.post(
            "/api/v1/cart/test_session_voice/voice-command",
            json={"utterance": "Add Fairlife milk from Walmart", "user_id": "user_voice_1"}
        )
        assert res.status_code == 200
        data = res.json()
        assert data["intent"] == "add_to_cart"
        assert "Fairlife" in data["spoken_response"]
        assert data["cart"]["cart_total"] > 0
        assert any("Fairlife" in item["title"] for item in data["cart"]["items"])

        # Test adding Cheerios via voice
        res2 = await ac.post(
            "/api/v1/cart/test_session_voice/voice-command",
            json={"utterance": "Add Cheerios cereal", "user_id": "user_voice_1"}
        )
        assert res2.status_code == 200
        data2 = res2.json()
        assert data2["intent"] == "add_to_cart"
        assert "Cheerios" in data2["spoken_response"]
