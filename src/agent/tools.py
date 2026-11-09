import json
import time
from typing import Optional, Dict, Any, List
import redis.asyncio as aioredis

from src.services.merchant_base import BaseMerchantAdapter
from src.services.stripe_client import StripePaymentService
from src.models.schemas import (
    CartItemSchema,
    CartStateSchema,
    WebRTCDataPayload,
    PreferenceProfileSchema,
)

# Standard US unit normalization map
US_UNIT_NORMALIZATION = {
    "gal": "gallon",
    "gallons": "gallon",
    "gallon": "gallon",
    "half-gallon": "half_gallon",
    "half gallon": "half_gallon",
    "qt": "quart",
    "quarts": "quart",
    "pt": "pint",
    "pints": "pint",
    "fl oz": "fl_oz",
    "fluid ounce": "fl_oz",
    "fluid ounces": "fl_oz",
    "lb": "lb",
    "lbs": "lb",
    "pound": "lb",
    "pounds": "lb",
    "oz": "oz",
    "ounce": "oz",
    "ounces": "oz",
    "ct": "count",
    "count": "count",
    "carton": "carton",
    "bunch": "bunch",
    "pack": "count",
}

def parse_us_unit(unit_str: str) -> str:
    cleaned = unit_str.strip().lower()
    return US_UNIT_NORMALIZATION.get(cleaned, cleaned)

class GroceryAgentTools:
    def __init__(
        self,
        session_id: str,
        user_id: str,
        merchant_adapter: BaseMerchantAdapter,
        redis_client: aioredis.Redis,
        stripe_service: StripePaymentService,
        user_preferences: Optional[PreferenceProfileSchema] = None,
        data_channel_emitter = None,
    ):
        self.session_id = session_id
        self.user_id = user_id
        self.merchant = merchant_adapter
        self.redis = redis_client
        self.stripe = stripe_service
        self.preferences = user_preferences or PreferenceProfileSchema()
        self.data_channel_emitter = data_channel_emitter

    async def _emit_data_channel(self, payload: WebRTCDataPayload):
        if self.data_channel_emitter:
            raw_json = payload.model_dump_json()
            await self.data_channel_emitter("cart_sync", raw_json)

    async def _get_cart_from_redis(self) -> CartStateSchema:
        key = f"cart:{self.session_id}"
        data = await self.redis.get(key)
        if data:
            return CartStateSchema.model_validate_json(data)
        return CartStateSchema(session_id=self.session_id, user_id=self.user_id)

    async def _save_cart_to_redis(self, cart: CartStateSchema):
        subtotal = sum(i.total_price for i in cart.items)
        cart.subtotal = round(subtotal, 2)
        cart.tax = round(subtotal * 0.0825, 2)  # Standard US 8.25% sales tax estimate
        cart.cart_total = round(cart.subtotal + cart.tax + (cart.delivery_fee if cart.items else 0.0), 2)
        key = f"cart:{self.session_id}"
        await self.redis.set(key, cart.model_dump_json(), ex=86400)

    def check_allergy_warning(self, item_ingredients: List[str], item_title: str) -> Optional[str]:
        text_to_check = f"{item_title} {' '.join(item_ingredients)}".lower()
        for allergy in self.preferences.allergies:
            allergy_term = allergy.lower().strip()
            if allergy_term in text_to_check or (allergy_term == "peanuts" and "peanut" in text_to_check):
                return f"Allergy Alert: '{item_title}' contains {allergy.upper()}, which conflicts with your allergy profile!"
        return None

    async def search_catalog(self, query: str, brand_preference: Optional[str] = None, organic_only: bool = False) -> Dict[str, Any]:
        """Search products in catalog, filtering by organic and preferences."""
        if not brand_preference and self.preferences.brand_preferences:
            for category_key, pref_brand in self.preferences.brand_preferences.items():
                if category_key.lower() in query.lower():
                    brand_preference = pref_brand
                    break

        req_organic = organic_only if organic_only else ("organic_only" in self.preferences.dietary_flags)
        items = await self.merchant.search_items(
            query=query,
            brand_preference=brand_preference,
            organic_only=req_organic,
        )

        warnings = []
        q_lower = query.lower()
        for allergy in self.preferences.allergies:
            allergy_term = allergy.lower().strip()
            if allergy_term in q_lower or (allergy_term == "peanuts" and "peanut" in q_lower):
                warnings.append(f"Allergy Alert: Search query '{query}' conflicts with your allergy profile ({allergy.upper()})!")

        for item in items:
            w = self.check_allergy_warning(item.ingredients, item.title)
            if w and w not in warnings:
                warnings.append(w)

        await self._emit_data_channel(WebRTCDataPayload(
            event="CATALOG_RESULTS",
            action="NONE",
            items=[i.model_dump() for i in items],
            message=warnings[0] if warnings else None,
            timestamp=time.time(),
        ))

        return {
            "found_count": len(items),
            "top_item": items[0].model_dump() if items else None,
            "warnings": warnings,
            "spoken_summary": f"Found {len(items)} items for {query}. Displayed on your screen.",
        }

    async def add_to_cart(self, sku: str, quantity: float = 1.0, unit: str = "count") -> Dict[str, Any]:
        """Add item to active cart with parsed US measurement units."""
        norm_unit = parse_us_unit(unit)
        item = await self.merchant.get_item_by_sku(sku)
        if not item:
            return {"status": "error", "message": f"SKU {sku} not found"}

        allergy_warning = self.check_allergy_warning(item.ingredients, item.title)

        cart = await self._get_cart_from_redis()
        unit_price = item.price
        total_price = round(unit_price * quantity, 2)

        existing = next((i for i in cart.items if i.sku == sku), None)
        if existing:
            existing.quantity += quantity
            existing.total_price = round(existing.quantity * existing.unit_price, 2)
            cart_item = existing
        else:
            cart_item = CartItemSchema(
                sku=item.sku,
                title=item.title,
                brand=item.brand,
                quantity=quantity,
                unit=norm_unit,
                unit_price=unit_price,
                total_price=total_price,
                is_organic=item.is_organic,
            )
            cart.items.append(cart_item)

        await self._save_cart_to_redis(cart)

        await self._emit_data_channel(WebRTCDataPayload(
            event="CART_MUTATED",
            action="ADD",
            item=cart_item.model_dump(),
            cart_total=cart.cart_total,
            message=allergy_warning,
            timestamp=time.time(),
        ))

        return {
            "status": "success",
            "added_item": cart_item.model_dump(),
            "cart_total": cart.cart_total,
            "allergy_warning": allergy_warning,
        }

    async def remove_from_cart(self, sku: str) -> Dict[str, Any]:
        """Remove item from cart and recalculate totals."""
        cart = await self._get_cart_from_redis()
        removed = next((i for i in cart.items if i.sku == sku), None)
        if not removed:
            return {"status": "error", "message": f"Item {sku} not in cart"}

        cart.items = [i for i in cart.items if i.sku != sku]
        await self._save_cart_to_redis(cart)

        await self._emit_data_channel(WebRTCDataPayload(
            event="CART_MUTATED",
            action="REMOVE",
            item=removed.model_dump(),
            cart_total=cart.cart_total,
            timestamp=time.time(),
        ))

        return {"status": "success", "removed_sku": sku, "cart_total": cart.cart_total}

    async def request_substitutions(self, sku: str) -> Dict[str, Any]:
        """Retrieve top 2 replacement options and push to UI."""
        subs = await self.merchant.get_substitutions(sku)
        subs_data = [s.model_dump() for s in subs]

        await self._emit_data_channel(WebRTCDataPayload(
            event="SUBSTITUTIONS_AVAILABLE",
            action="SUBSTITUTE",
            items=subs_data,
            timestamp=time.time(),
        ))

        return {
            "target_sku": sku,
            "substitutions": subs_data,
            "spoken_summary": f"I've sent {len(subs_data)} substitution options to your screen.",
        }

    async def checkout_cart(self, confirm: bool = False) -> Dict[str, Any]:
        """Verify cart, process payment through Stripe, and book delivery."""
        cart = await self._get_cart_from_redis()
        if not cart.items:
            return {"status": "error", "message": "Cart is empty"}

        if not confirm:
            return {
                "status": "confirmation_required",
                "subtotal": cart.subtotal,
                "tax": cart.tax,
                "delivery_fee": cart.delivery_fee,
                "total": cart.cart_total,
                "spoken_prompt": f"Your total is ${cart.cart_total:.2f}. Would you like me to place the order with your card on file?",
            }

        payment = await self.stripe.create_payment_intent(
            amount_dollars=cart.cart_total,
            customer_id=self.user_id,
            metadata={"session_id": self.session_id},
        )

        delivery = await self.stripe.book_delivery_slot(self.session_id)
        merchant_order = await self.merchant.create_order(
            session_id=self.session_id,
            items=[i.model_dump() for i in cart.items],
            delivery_slot=delivery["time_window"],
        )

        await self._emit_data_channel(WebRTCDataPayload(
            event="CHECKOUT_COMPLETE",
            action="NONE",
            cart_total=cart.cart_total,
            message=f"Order confirmed! Delivery booked for {delivery['time_window']}",
            timestamp=time.time(),
        ))

        await self.redis.delete(f"cart:{self.session_id}")

        return {
            "status": "success",
            "order_id": merchant_order["order_id"],
            "delivery": delivery,
            "payment": payment,
            "spoken_summary": f"Order confirmed! Your groceries will arrive {delivery['time_window']}.",
        }
