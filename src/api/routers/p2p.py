from typing import List, Optional
from fastapi import APIRouter, HTTPException, Depends, Query
from src.core.redis import get_redis
from src.models.schemas import (
    P2POrderSchema,
    P2PCreateOrderRequest,
    P2PAcceptRequest,
    P2PPickItemRequest,
    ShopperWalletSchema,
    CartStateSchema,
)
from src.services.p2p_service import p2p_service
from src.services.real_products_service import get_real_products_for_merchant

router = APIRouter(prefix="/p2p", tags=["P2P Neighbor Shopper"])

@router.get("/orders", response_model=List[P2POrderSchema])
async def list_open_orders(store: Optional[str] = Query(None, description="Filter by store, e.g. walmart or kroger")):
    """List open orders available for shoppers currently in or near a store."""
    return p2p_service.get_open_orders(store_filter=store)

@router.get("/orders/{order_id}", response_model=P2POrderSchema)
async def get_order_details(order_id: str):
    """Fetch status and item picking progress of a specific P2P order."""
    order = p2p_service.get_order(order_id)
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")
    return order

@router.post("/orders", response_model=P2POrderSchema)
async def create_neighbor_order(req: P2PCreateOrderRequest, redis = Depends(get_redis)):
    """Customer broadcasts their active cart to nearby in-store shoppers with a bounty."""
    data = await redis.get(f"cart:{req.session_id}")
    if not data:
        raise HTTPException(status_code=400, detail="Active cart not found")
    
    cart = CartStateSchema.model_validate_json(data)
    if not cart.items:
        raise HTTPException(status_code=400, detail="Cart is empty")

    # Build catalog dictionary for metadata lookup
    all_items = get_real_products_for_merchant("walmart") + get_real_products_for_merchant("kroger")
    cat_dict = {i.sku: i for i in all_items}

    order = p2p_service.create_order_from_cart(cart=cart, req=req, catalog_dict=cat_dict)
    
    # Clear cart after broadcast
    await redis.delete(f"cart:{req.session_id}")
    return order

@router.post("/orders/{order_id}/accept", response_model=P2POrderSchema)
async def accept_order_as_shopper(order_id: str, req: P2PAcceptRequest):
    """Shopper in the store accepts an open order to fulfill."""
    order = p2p_service.accept_order(order_id, shopper_id=req.shopper_id, shopper_name=req.shopper_name)
    if not order:
        raise HTTPException(status_code=400, detail="Order is no longer available or does not exist")
    return order

@router.post("/orders/{order_id}/pick", response_model=P2POrderSchema)
async def pick_item_in_store(order_id: str, req: P2PPickItemRequest):
    """Shopper marks an item as picked after verifying barcode/UPC."""
    order = p2p_service.pick_item(order_id, sku=req.sku, barcode=req.barcode)
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")
    return order

@router.post("/orders/{order_id}/checkout", response_model=P2POrderSchema)
async def mark_order_checkout(order_id: str):
    """Shopper finishes register checkout and heads out for dropoff."""
    order = p2p_service.mark_out_for_delivery(order_id)
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")
    return order

@router.post("/orders/{order_id}/deliver", response_model=P2POrderSchema)
async def complete_delivery_and_release_escrow(order_id: str, shopper_id: str = Query("shopper_alex_01")):
    """Shopper confirms drop-off at customer's doorstep; releases bounty to shopper wallet."""
    order = p2p_service.complete_delivery(order_id, shopper_id=shopper_id)
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")
    return order

@router.get("/wallet/{shopper_id}", response_model=ShopperWalletSchema)
async def get_shopper_wallet(shopper_id: str):
    """Retrieve shopper earnings balance, rating, and payout history."""
    return p2p_service.get_or_create_wallet(shopper_id)

@router.post("/wallet/{shopper_id}/cashout")
async def cashout_shopper_wallet(shopper_id: str):
    """Instant cashout of available balance to linked debit card."""
    return p2p_service.cashout_wallet(shopper_id)
