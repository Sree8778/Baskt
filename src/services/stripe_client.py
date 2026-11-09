from typing import Dict, Any
from src.core.config import settings

class StripePaymentService:
    def __init__(self):
        self.api_key = settings.stripe_secret_key

    async def create_payment_intent(self, amount_dollars: float, customer_id: str, metadata: Dict[str, Any]) -> Dict[str, Any]:
        """Creates a Stripe PaymentIntent (or simulates for mock environment)."""
        amount_cents = int(round(amount_dollars * 100))
        return {
            "payment_intent_id": f"pi_mock_{metadata.get('session_id', '000')}",
            "amount": amount_cents,
            "currency": "usd",
            "status": "succeeded",
            "customer": customer_id,
        }

    async def book_delivery_slot(self, session_id: str, slot_preference: str = "Today, 6:00 PM - 8:00 PM") -> Dict[str, Any]:
        return {
            "booking_reference": f"DLV-{session_id[:6].upper()}",
            "time_window": slot_preference,
            "carrier": "DoorDash Drive / Kroger Direct",
            "status": "CONFIRMED",
        }
