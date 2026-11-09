import pytest
from src.agent.tools import parse_us_unit

def test_parse_us_units():
    assert parse_us_unit("gallons") == "gallon"
    assert parse_us_unit("gal") == "gallon"
    assert parse_us_unit("lbs") == "lb"
    assert parse_us_unit("oz") == "oz"
    assert parse_us_unit("fluid ounces") == "fl_oz"
    assert parse_us_unit("carton") == "carton"
    assert parse_us_unit("bunch") == "bunch"

@pytest.mark.asyncio
async def test_add_to_cart_updates_totals_and_emits_event(agent_tools, emitted_events):
    # Add 1 gallon of Simple Truth Organic Whole Milk ($5.49)
    result = await agent_tools.add_to_cart(sku="KR-MILK-ORG-GAL", quantity=1.0, unit="gallon")
    
    assert result["status"] == "success"
    assert result["added_item"]["sku"] == "KR-MILK-ORG-GAL"
    assert result["added_item"]["quantity"] == 1.0
    assert result["added_item"]["unit"] == "gallon"
    
    # Subtotal 5.49, Tax ~0.45, Delivery 3.99 -> Total ~9.93
    assert result["cart_total"] > 5.49
    
    # Verify WebRTC Data Channel Event was emitted
    assert len(emitted_events.events) == 1
    event = emitted_events.events[0]
    assert event["topic"] == "cart_sync"
    assert event["data"]["event"] == "CART_MUTATED"
    assert event["data"]["action"] == "ADD"
    assert event["data"]["item"]["sku"] == "KR-MILK-ORG-GAL"
    assert event["data"]["cart_total"] == result["cart_total"]

@pytest.mark.asyncio
async def test_allergy_warning_triggered_for_peanut_allergy(agent_tools, emitted_events):
    # User has 'peanuts' in allergy profile; search catalog for peanut butter
    search_res = await agent_tools.search_catalog("peanut butter", organic_only=False)
    
    # Assert allergy alert is triggered
    assert len(search_res["warnings"]) > 0
    assert "Allergy Alert" in search_res["warnings"][0]
    assert "PEANUTS" in search_res["warnings"][0]
    
    # Adding to cart should also flag allergy warning in return payload
    add_res = await agent_tools.add_to_cart("KR-PB-CREAMY-16", quantity=1.0, unit="count")
    assert add_res["allergy_warning"] is not None
    assert "contains PEANUTS" in add_res["allergy_warning"]

@pytest.mark.asyncio
async def test_request_substitutions_emits_data_channel(agent_tools, emitted_events):
    # Request substitutions for milk
    sub_res = await agent_tools.request_substitutions("KR-MILK-ORG-GAL")
    
    assert len(sub_res["substitutions"]) <= 2
    assert len(sub_res["substitutions"]) > 0
    
    # Verify emitted visual options
    sub_events = [e for e in emitted_events.events if e["data"]["event"] == "SUBSTITUTIONS_AVAILABLE"]
    assert len(sub_events) == 1
    assert sub_events[0]["data"]["action"] == "SUBSTITUTE"
    assert len(sub_events[0]["data"]["items"]) > 0

@pytest.mark.asyncio
async def test_remove_from_cart_recalculates_and_emits(agent_tools, emitted_events):
    # Add two items
    await agent_tools.add_to_cart("KR-MILK-ORG-GAL", quantity=1.0)
    await agent_tools.add_to_cart("KR-EGGS-PAST-12", quantity=1.0)
    
    # Remove milk
    remove_res = await agent_tools.remove_from_cart("KR-MILK-ORG-GAL")
    assert remove_res["status"] == "success"
    
    last_event = emitted_events.events[-1]["data"]
    assert last_event["event"] == "CART_MUTATED"
    assert last_event["action"] == "REMOVE"
    assert last_event["item"]["sku"] == "KR-MILK-ORG-GAL"
    assert last_event["cart_total"] == remove_res["cart_total"]

@pytest.mark.asyncio
async def test_checkout_cart_flow(agent_tools, emitted_events):
    # Empty cart checkout returns error
    empty_res = await agent_tools.checkout_cart(confirm=True)
    assert empty_res["status"] == "error"

    # Add item
    await agent_tools.add_to_cart("KR-MILK-ORG-GAL", quantity=1.0)
    
    # Prompt confirmation step
    confirm_req = await agent_tools.checkout_cart(confirm=False)
    assert confirm_req["status"] == "confirmation_required"
    assert confirm_req["total"] > 0

    # Finalize checkout
    complete_res = await agent_tools.checkout_cart(confirm=True)
    assert complete_res["status"] == "success"
    assert "KROG-" in complete_res["order_id"]
    assert complete_res["payment"]["status"] == "succeeded"

    # Verify checkout event emitted to WebRTC data channel
    checkout_events = [e for e in emitted_events.events if e["data"]["event"] == "CHECKOUT_COMPLETE"]
    assert len(checkout_events) == 1
