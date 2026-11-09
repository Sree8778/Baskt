from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel
from src.core.redis import get_redis
from src.models.schemas import CartStateSchema
from src.services.stripe_client import StripePaymentService
from src.services.mock_kroger import MockKrogerAdapter

router = APIRouter(prefix="/checkout", tags=["Checkout"])

class CheckoutConfirmRequest(BaseModel):
    session_id: str
    user_id: str
    biometric_signature: str

@router.post("/confirm")
async def confirm_1tap_checkout(req: CheckoutConfirmRequest, redis = Depends(get_redis)):
    """Fallback biometric 1-tap checkout endpoint."""
    data = await redis.get(f"cart:{req.session_id}")
    if not data:
        raise HTTPException(status_code=400, detail="Active cart session not found")

    cart = CartStateSchema.model_validate_json(data)
    if not cart.items:
        raise HTTPException(status_code=400, detail="Cart is empty")

    stripe = StripePaymentService()
    merchant = MockKrogerAdapter()

    payment = await stripe.create_payment_intent(
        amount_dollars=cart.cart_total,
        customer_id=req.user_id,
        metadata={"session_id": req.session_id, "biometric_auth": True},
    )

    delivery = await stripe.book_delivery_slot(req.session_id)
    order = await merchant.create_order(
        session_id=req.session_id,
        items=[i.model_dump() for i in cart.items],
        delivery_slot=delivery["time_window"],
    )

    await redis.delete(f"cart:{req.session_id}")
    return {
        "status": "COMPLETED",
        "order": order,
        "payment": payment,
        "delivery": delivery,
    }
