import pytest
from httpx import AsyncClient, ASGITransport
from src.api.main import app
from src.services.p2p_service import p2p_service

@pytest.mark.asyncio
async def test_p2p_list_orders():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        res = await ac.get("/api/v1/p2p/orders")
        assert res.status_code == 200
        orders = res.json()
        assert len(orders) >= 2
        assert orders[0]["order_id"].startswith("P2P-")
        assert orders[0]["shopper_bounty"] > 0
        assert len(orders[0]["items"]) > 0

@pytest.mark.asyncio
async def test_p2p_order_details_and_lifecycle():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # 1. Get open orders
        res = await ac.get("/api/v1/p2p/orders")
        order_id = res.json()[0]["order_id"]

        # 2. Accept order as shopper
        accept_res = await ac.post(
            f"/api/v1/p2p/orders/{order_id}/accept",
            json={"shopper_id": "shopper_alex_01", "shopper_name": "Alex Morgan"}
        )
        assert accept_res.status_code == 200
        assert accept_res.json()["status"] == "picking"
        assert accept_res.json()["shopper_id"] == "shopper_alex_01"

        # 3. Pick items with barcode verification
        items = accept_res.json()["items"]
        first_item = items[0]
        pick_res = await ac.post(
            f"/api/v1/p2p/orders/{order_id}/pick",
            json={"sku": first_item["sku"], "barcode": first_item["barcode"]}
        )
        assert pick_res.status_code == 200
        updated_item = [i for i in pick_res.json()["items"] if i["sku"] == first_item["sku"]][0]
        assert updated_item["is_picked"] is True

        # 4. Mark checkout
        checkout_res = await ac.post(f"/api/v1/p2p/orders/{order_id}/checkout")
        assert checkout_res.status_code == 200
        assert checkout_res.json()["status"] == "out_for_delivery"

        # 5. Complete delivery and release 100% escrow to shopper
        initial_wallet = await ac.get("/api/v1/p2p/wallet/shopper_alex_01")
        initial_balance = initial_wallet.json()["available_balance"]

        deliver_res = await ac.post(
            f"/api/v1/p2p/orders/{order_id}/deliver?shopper_id=shopper_alex_01"
        )
        assert deliver_res.status_code == 200
        assert deliver_res.json()["status"] == "completed"

        # 6. Verify wallet received full bounty with zero middleman deduction
        wallet_res = await ac.get("/api/v1/p2p/wallet/shopper_alex_01")
        new_balance = wallet_res.json()["available_balance"]
        bounty = deliver_res.json()["shopper_bounty"]
        assert round(new_balance - initial_balance, 2) == round(bounty, 2)

@pytest.mark.asyncio
async def test_p2p_wallet_cashout():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        cashout_res = await ac.post("/api/v1/p2p/wallet/shopper_alex_01/cashout")
        assert cashout_res.status_code == 200
        data = cashout_res.json()
        assert data["status"] == "success"
        assert data["remaining_balance"] == 0.0

        # Check wallet now has 0 available balance
        wallet_res = await ac.get("/api/v1/p2p/wallet/shopper_alex_01")
        assert wallet_res.json()["available_balance"] == 0.0
