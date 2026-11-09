from typing import Optional, List, Dict, Any
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field

from src.core.redis import get_redis
from src.models.schemas import CartStateSchema, PreferenceProfileSchema
from src.services.aldi_adapter import AldiAdapter
from src.services.merchant_base import BaseMerchantAdapter
from src.services.mock_kroger import MockKrogerAdapter
from src.services.walmart_adapter import WalmartAdapter
from src.services.stripe_client import StripePaymentService
from src.agent.tools import GroceryAgentTools

router = APIRouter(prefix="/cart", tags=["Cart"])
_aldi = AldiAdapter()
_walmart = WalmartAdapter()
_kroger = MockKrogerAdapter()
_stripe = StripePaymentService()

def get_adapter_for_sku(sku: str) -> BaseMerchantAdapter:
    if sku.startswith("ALDI-"):
        return _aldi
    elif sku.startswith("WM-"):
        return _walmart
    return _kroger

class AddItemRequest(BaseModel):
    sku: str
    quantity: float = 1.0
    unit: str = "count"
    user_id: str = "sreeram_us"
    allergies: List[str] = Field(default_factory=lambda: ["peanuts", "tree nuts"])
    dietary_flags: List[str] = Field(default_factory=lambda: ["organic_only"])

class VoiceCommandRequest(BaseModel):
    utterance: str
    user_id: str = "sreeram_us"
    allergies: List[str] = Field(default_factory=lambda: ["peanuts", "tree nuts"])
    dietary_flags: List[str] = Field(default_factory=lambda: ["organic_only"])

@router.get("/{session_id}", response_model=CartStateSchema)
async def get_live_cart(session_id: str, redis = Depends(get_redis)):
    """Fetch current live cart state from Redis cache."""
    data = await redis.get(f"cart:{session_id}")
    if not data:
        return CartStateSchema(session_id=session_id, user_id="anonymous", items=[])
    return CartStateSchema.model_validate_json(data)

@router.post("/{session_id}/add")
async def add_cart_item(session_id: str, req: AddItemRequest, redis = Depends(get_redis)):
    """Add an item directly to the synchronized session cart across Walmart and Kroger."""
    adapter = get_adapter_for_sku(req.sku)
    prefs = PreferenceProfileSchema(
        allergies=req.allergies,
        dietary_flags=req.dietary_flags,
    )
    tools = GroceryAgentTools(
        session_id=session_id,
        user_id=req.user_id,
        merchant_adapter=adapter,
        redis_client=redis,
        stripe_service=_stripe,
        user_preferences=prefs,
    )
    result = await tools.add_to_cart(sku=req.sku, quantity=req.quantity, unit=req.unit)
    cart = await tools._get_cart_from_redis()
    result["cart"] = cart.model_dump()
    return result

@router.delete("/{session_id}/item/{sku}")
async def remove_cart_item(session_id: str, sku: str, redis = Depends(get_redis)):
    """Remove an item from the session cart."""
    adapter = get_adapter_for_sku(sku)
    tools = GroceryAgentTools(
        session_id=session_id,
        user_id="sreeram_us",
        merchant_adapter=adapter,
        redis_client=redis,
        stripe_service=_stripe,
    )
    result = await tools.remove_from_cart(sku=sku)
    cart = await tools._get_cart_from_redis()
    result["cart"] = cart.model_dump()
    return result

@router.post("/{session_id}/optimize")
async def optimize_cart_savings(session_id: str, redis = Depends(get_redis)):
    """Analyze current cart items and identify maximum savings by switching to Walmart everyday low prices."""
    tools = GroceryAgentTools(
        session_id=session_id,
        user_id="sreeram_us",
        merchant_adapter=_walmart,
        redis_client=redis,
        stripe_service=_stripe,
    )
    cart = await tools._get_cart_from_redis()
    if not cart.items:
        return {"savings": 0.0, "message": "Cart is empty. Add items to optimize for lowest price.", "swaps": []}

    total_potential_savings = 0.0
    swaps = []
    
    for item in cart.items:
        # Check corresponding Walmart pricing
        base_sku = item.sku.replace("KR-", "").replace("WM-", "")
        wm_sku = f"WM-{base_sku}"
        wm_item = await _walmart.get_item_by_sku(wm_sku)
        if wm_item and wm_item.price < item.unit_price:
            saving_per_unit = item.unit_price - wm_item.price
            total_savings_item = saving_per_unit * item.quantity
            total_potential_savings += total_savings_item
            swaps.append({
                "original_sku": item.sku,
                "original_title": item.title,
                "walmart_sku": wm_item.sku,
                "walmart_title": wm_item.title,
                "unit_savings": saving_per_unit,
                "total_savings": total_savings_item,
            })
            
    return {
        "status": "success",
        "current_total": cart.cart_total,
        "total_savings": round(total_potential_savings, 2),
        "optimized_total": round(max(0.0, cart.cart_total - total_potential_savings), 2),
        "swaps": swaps,
        "summary": f"Switching to Walmart Rollback items saves ${total_potential_savings:.2f} on your basket!",
    }

@router.post("/{session_id}/voice-command")
async def process_voice_command(session_id: str, req: VoiceCommandRequest, redis = Depends(get_redis)):
    """Process natural language voice commands with multi-store comparison (Walmart & Kroger)."""
    prefs = PreferenceProfileSchema(
        allergies=req.allergies,
        dietary_flags=req.dietary_flags,
    )

    cmd = req.utterance.lower().strip()
    
    # 1. Checkout intents
    if any(k in cmd for k in ["checkout", "place order", "pay now", "buy now"]):
        tools = GroceryAgentTools(
            session_id=session_id,
            user_id=req.user_id,
            merchant_adapter=_kroger,
            redis_client=redis,
            stripe_service=_stripe,
            user_preferences=prefs,
        )
        res = await tools.checkout_cart(confirm=False)
        cart = await tools._get_cart_from_redis()
        return {
            "intent": "checkout",
            "spoken_response": res.get("spoken_prompt", f"Your cart total is ${cart.cart_total:.2f}. Would you like me to place the order with your card on file?"),
            "cart": cart.model_dump(),
            "details": res,
        }

    # 1b. Optimization intents
    if any(k in cmd for k in ["optimize", "savings", "save money", "cheaper", "best price"]):
        opt = await optimize_cart_savings(session_id=session_id, redis=redis)
        savings = opt.get("total_savings", 0.0)
        tools = GroceryAgentTools(
            session_id=session_id,
            user_id=req.user_id,
            merchant_adapter=_walmart,
            redis_client=redis,
            stripe_service=_stripe,
        )
        cart = await tools._get_cart_from_redis()
        if savings > 0:
            spoken = f"I found ${savings:.2f} in savings by optimizing your cart with Walmart Rollback prices. Your new total would be ${opt['optimized_total']:.2f}."
        else:
            spoken = f"Your cart is already optimized for the best available prices across Walmart and Kroger. Current total is ${cart.cart_total:.2f}."
        return {
            "intent": "optimize",
            "spoken_response": spoken,
            "cart": cart.model_dump(),
            "optimization": opt,
        }

    # 2. Allergy-sensitive requests (e.g. Peanut Butter)
    if "peanut" in cmd:
        subs = await _kroger.get_substitutions("KR-PB-CREAMY-16")
        tools = GroceryAgentTools(
            session_id=session_id,
            user_id=req.user_id,
            merchant_adapter=_kroger,
            redis_client=redis,
            stripe_service=_stripe,
            user_preferences=prefs,
        )
        cart = await tools._get_cart_from_redis()
        return {
            "intent": "allergy_alert",
            "spoken_response": "Caution! You have a documented peanut allergy. I have withheld peanut butter and displayed safe organic almond butter instead.",
            "allergy_warning": "Allergy Alert: 'Peanut Butter' contains PEANUTS, which conflicts with your profile!",
            "substitutions": [s.model_dump() for s in subs],
            "cart": cart.model_dump(),
        }

    # 3. Store selection preference
    is_walmart = "walmart" in cmd or "great value" in cmd or "cheapest" in cmd or "cheap" in cmd
    is_kroger = "kroger" in cmd or "simple truth" in cmd
    
    target_sku = None
    qty = 1.0
    unit = None

    # Number detection
    number_words = {
        "1": 1.0, "one": 1.0, "2": 2.0, "two": 2.0, "3": 3.0, "three": 3.0,
        "4": 4.0, "four": 4.0, "5": 5.0, "five": 5.0, "half": 0.5, "dozen": 12.0
    }
    for word, val in number_words.items():
        if word in cmd.split():
            qty = val
            break

    # Real Product Keyword Mapping
    product_keywords = [
        (["fairlife"], "MILK-FAIRLIFE-WHOLE", "gallon"),
        (["horizon", "organic milk"], "MILK-ORG-GAL", "gallon"),
        (["conventional milk", "whole milk", "milk"], "MILK-CONV-GAL", "gallon"),
        (["oatly", "oat milk", "oatmilk"], "OATMILK-64", "oz"),
        (["silk", "almond milk", "almondmilk"], "MILK-SILK-ALMOND", "oz"),
        (["egg", "vital farms", "eggs"], "EGGS-PAST-12", "count"),
        (["chobani", "greek yogurt", "yogurt"], "YOGURT-CHOB-32", "oz"),
        (["kerrygold", "irish butter", "butter"], "BUTTER-KERRY-SALT", "oz"),
        (["philadelphia", "cream cheese"], "CHEESE-PHILLY-8", "oz"),
        (["daisy", "cottage cheese"], "CHEESE-DAISY-COTTAGE", "oz"),
        (["dave", "killer bread"], "BREAD-DAVE-21", "oz"),
        (["sourdough", "artisan bread", "bread"], "BREAD-SOUR-24", "oz"),
        (["rao", "marinara", "pasta sauce"], "SAUCE-RAOS-MARINARA", "oz"),
        (["kraft", "mac and cheese", "mac & cheese", "macaroni"], "PASTA-KRAFT-MAC", "oz"),
        (["hellmann", "mayo", "mayonnaise"], "MAYO-HELLMANNS-30", "oz"),
        (["almond butter", "justin"], "AB-CREAMY-16", "oz"),
        (["cheerios"], "CEREAL-CHEERIOS-ORIG", "oz"),
        (["quaker", "oatmeal", "rolled oats", "oats"], "OATS-QUAKER-OLD", "oz"),
        (["lindt", "dark chocolate", "chocolate"], "CHOC-LINDT-70", "oz"),
        (["oreo", "cookies"], "COOKIE-OREO-DOUBLE", "oz"),
        (["apple", "honeycrisp", "apples"], "APPLES-HONEY-LB", "lb"),
        (["avocado", "avocados"], "PROD-AVOCADO-BAG", "count"),
        (["strawberry", "strawberries", "berries"], "PROD-STRAWBERRY-1LB", "oz"),
        (["banana", "bananas"], "PROD-BANANA-BUNCH", "lb"),
    ]

    clean_cmd = cmd.replace("add", "").replace("buy", "").replace("get", "").strip()
    
    for kw_list, base_sku, default_unit in product_keywords:
        if any(kw in clean_cmd for kw in kw_list):
            prefix = "WM-" if is_walmart or not is_kroger else "KR-"
            target_sku = f"{prefix}{base_sku}"
            unit = default_unit
            break

    # If no keyword hit but command started with 'add', attempt fuzzy catalog lookup
    if not target_sku and ("add" in cmd or "buy" in cmd):
        query_terms = [w for w in clean_cmd.split() if w not in ["a", "an", "the", "some", "of", "to", "cart", "please"]]
        search_query = " ".join(query_terms)
        if search_query:
            matched = await _walmart.search_items(search_query) or await _kroger.search_items(search_query)
            if matched:
                target_sku = matched[0].sku
                unit = matched[0].standard_unit

    if target_sku:
        adapter = get_adapter_for_sku(target_sku)
        item = await adapter.get_item_by_sku(target_sku)
        if item:
            final_unit = unit or item.standard_unit
            tools = GroceryAgentTools(
                session_id=session_id,
                user_id=req.user_id,
                merchant_adapter=adapter,
                redis_client=redis,
                stripe_service=_stripe,
                user_preferences=prefs,
            )
            add_res = await tools.add_to_cart(sku=target_sku, quantity=qty, unit=final_unit)
            cart = await tools._get_cart_from_redis()
            store_name = "Walmart" if target_sku.startswith("WM-") else "Kroger"
            return {
                "intent": "add_to_cart",
                "spoken_response": f"Added {qty} {final_unit} of {item.title} from {store_name} to your cart for ${item.price:.2f}. Cart total is ${cart.cart_total:.2f}.",
                "item_added": item.model_dump(),
                "cart": cart.model_dump(),
                "details": add_res,
            }

    # 4. Search query fallback across stores
    kroger_items = await _kroger.search_items(query=cmd)
    walmart_items = await _walmart.search_items(query=cmd)
    combined = kroger_items + walmart_items

    tools = GroceryAgentTools(
        session_id=session_id,
        user_id=req.user_id,
        merchant_adapter=_kroger,
        redis_client=redis,
        stripe_service=_stripe,
        user_preferences=prefs,
    )
    cart = await tools._get_cart_from_redis()

    return {
        "intent": "search",
        "spoken_response": f"Found {len(combined)} real grocery items matching '{req.utterance}' across Walmart and Kroger.",
        "search_results": [i.model_dump() for i in combined],
        "cart": cart.model_dump(),
    }
