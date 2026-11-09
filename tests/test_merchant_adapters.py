import pytest
from src.services.mock_kroger import MockKrogerAdapter
from src.services.mock_instacart import MockInstacartAdapter

@pytest.mark.asyncio
async def test_kroger_catalog_search_filters():
    adapter = MockKrogerAdapter()
    
    # Search all milk
    all_milk = await adapter.search_items("milk")
    assert len(all_milk) >= 2
    
    # Filter organic only
    organic_milk = await adapter.search_items("milk", organic_only=True)
    assert len(organic_milk) >= 1
    assert all(item.is_organic for item in organic_milk)
    
    # Filter by brand
    st_milk = await adapter.search_items("milk", brand_preference="Simple Truth")
    assert len(st_milk) >= 1
    assert "Simple Truth" in st_milk[0].brand

@pytest.mark.asyncio
async def test_kroger_substitutions():
    adapter = MockKrogerAdapter()
    subs = await adapter.get_substitutions("KR-MILK-ORG-GAL")
    assert len(subs) == 2
    assert all(s.sku != "KR-MILK-ORG-GAL" for s in subs)

@pytest.mark.asyncio
async def test_kroger_check_stock():
    adapter = MockKrogerAdapter()
    assert await adapter.check_stock("KR-MILK-ORG-GAL") is True
    assert await adapter.check_stock("NON-EXISTENT-SKU") is False

@pytest.mark.asyncio
async def test_instacart_adapter_search_and_order():
    adapter = MockInstacartAdapter()
    results = await adapter.search_items("eggs")
    assert len(results) >= 1
    assert results[0].sku == "IC-EGGS-VITAL-12"

    order = await adapter.create_order("session-abc", results, "Tomorrow 10am")
    assert order["merchant"] == "Instacart Connect"
    assert order["status"] == "DISPATCHED"

@pytest.mark.asyncio
async def test_walmart_adapter_search_and_price_comparison():
    from src.services.walmart_adapter import WalmartAdapter
    adapter = WalmartAdapter()
    
    # Search Great Value organic milk
    wm_milk = await adapter.search_items("milk", organic_only=True)
    assert len(wm_milk) >= 1
    assert wm_milk[0].brand == "Great Value"
    assert wm_milk[0].price == 4.68  # Lower than Kroger's $5.49

    # Check stock
    assert await adapter.check_stock("WM-MILK-ORG-GAL") is True
    
    # Check order creation
    order = await adapter.create_order("session-xyz", wm_milk, "Today 5-7pm")
    assert "WM-" in order["order_id"]
    assert order["status"] == "SCHEDULED"

