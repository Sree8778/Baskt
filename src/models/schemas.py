from typing import List, Optional, Dict, Any, Literal
from pydantic import BaseModel, Field

class PreferenceProfileSchema(BaseModel):
    dietary_flags: List[str] = Field(default_factory=list, description="e.g. ['vegan', 'organic_only', 'keto', 'gluten_free']")
    allergies: List[str] = Field(default_factory=list, description="e.g. ['peanuts', 'tree_nuts', 'dairy', 'shellfish']")
    brand_preferences: Dict[str, str] = Field(default_factory=dict, description="e.g. {'milk': 'Organic Valley', 'bread': 'Dave\\'s Killer Bread'}")

class UserProfileSchema(BaseModel):
    id: str
    email: str
    name: str
    stripe_customer_id: Optional[str] = None
    preferences: PreferenceProfileSchema = Field(default_factory=PreferenceProfileSchema)

class GroceryItemSchema(BaseModel):
    sku: str
    title: str
    brand: str
    category: str
    price: float
    standard_unit: str  # e.g., 'gallon', 'oz', 'lb', 'count', 'carton'
    is_organic: bool = False
    in_stock: bool = True
    ingredients: List[str] = Field(default_factory=list)
    image_url: Optional[str] = None
    barcode: Optional[str] = None
    counterpart_price: Optional[float] = None
    rating: float = 4.8
    review_count: int = 1250

class CartItemSchema(BaseModel):
    sku: str
    title: str
    brand: str
    quantity: float
    unit: str
    unit_price: float
    total_price: float
    is_organic: bool = False

class CartStateSchema(BaseModel):
    session_id: str
    user_id: str
    items: List[CartItemSchema] = Field(default_factory=list)
    subtotal: float = 0.0
    tax: float = 0.0
    delivery_fee: float = 3.99
    cart_total: float = 0.0

class WebRTCDataPayload(BaseModel):
    event: Literal["CART_MUTATED", "SUBSTITUTIONS_AVAILABLE", "CATALOG_RESULTS", "WARNING", "CHECKOUT_COMPLETE"]
    action: Optional[Literal["ADD", "REMOVE", "UPDATE", "SUBSTITUTE", "NONE"]] = None
    item: Optional[Dict[str, Any]] = None
    cart_total: Optional[float] = None
    items: Optional[List[Dict[str, Any]]] = None
    message: Optional[str] = None
    timestamp: Optional[float] = None

# --- P2P NEIGHBOR SHOPPER SCHEMAS ---

class P2PPickItem(BaseModel):
    sku: str
    title: str
    brand: str
    quantity: float
    unit: str
    unit_price: float
    barcode: Optional[str] = None
    image_url: Optional[str] = None
    aisle: str = "Aisle A1"
    is_picked: bool = False
    verified_barcode: Optional[str] = None

class P2POrderSchema(BaseModel):
    order_id: str
    customer_id: str
    customer_name: str
    store_id: str
    store_name: str
    delivery_address: str
    delivery_distance_miles: float
    items: List[P2PPickItem]
    items_subtotal: float
    shopper_bounty: float
    total_customer_charge: float
    status: Literal["open", "accepted", "picking", "checkout_ready", "out_for_delivery", "completed", "cancelled"] = "open"
    shopper_id: Optional[str] = None
    shopper_name: Optional[str] = None
    created_at: str
    notes: Optional[str] = None

class P2PCreateOrderRequest(BaseModel):
    session_id: str
    customer_id: str = "sreeram_us"
    customer_name: str = "Sreeram V."
    delivery_address: str = "452 Elm Street, Apt 3B (0.8 mi away)"
    tip: float = 2.0
    notes: Optional[str] = "Please ring doorbell, do not leave in lobby."
    store: str = "walmart"

class P2PAcceptRequest(BaseModel):
    shopper_id: str = "shopper_alex_01"
    shopper_name: str = "Alex Morgan"

class P2PPickItemRequest(BaseModel):
    sku: str
    barcode: Optional[str] = None

class ShopperWalletSchema(BaseModel):
    shopper_id: str
    available_balance: float = 0.0
    total_earned: float = 0.0
    completed_deliveries: int = 0
    rating: float = 4.98
    payouts_history: List[Dict[str, Any]] = Field(default_factory=list)

