import pytest
from src.models.schemas import PreferenceProfileSchema, GroceryItemSchema, CartItemSchema, CartStateSchema

def test_preference_profile_defaults():
    pref = PreferenceProfileSchema()
    assert pref.dietary_flags == []
    assert pref.allergies == []
    assert pref.brand_preferences == {}

def test_grocery_item_schema():
    item = GroceryItemSchema(
        sku="TEST-SKU-1",
        title="Organic Whole Milk",
        brand="Organic Valley",
        category="Dairy",
        price=5.99,
        standard_unit="gallon",
        is_organic=True,
    )
    assert item.is_organic is True
    assert item.in_stock is True

def test_cart_state_schema():
    cart = CartStateSchema(
        session_id="session-1",
        user_id="user-1",
        items=[
            CartItemSchema(
                sku="TEST-SKU-1",
                title="Organic Whole Milk",
                brand="Organic Valley",
                quantity=2.0,
                unit="gallon",
                unit_price=5.99,
                total_price=11.98,
                is_organic=True,
            )
        ],
        subtotal=11.98,
        tax=0.99,
        delivery_fee=3.99,
        cart_total=16.96,
    )
    assert len(cart.items) == 1
    assert cart.cart_total == 16.96
