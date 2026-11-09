import time
from typing import Dict, List, Optional, Any
from src.models.schemas import (
    P2POrderSchema,
    P2PPickItem,
    P2PCreateOrderRequest,
    ShopperWalletSchema,
    CartStateSchema,
)

# Store Aisle Map to generate linear, non-backtracking in-store routing
AISLE_MAP = {
    "Produce": ("Aisle 1 (Produce)", 1),
    "Bakery": ("Aisle 3 (Artisan Bakery)", 2),
    "Snacks": ("Aisle 6 (Snacks & Treats)", 3),
    "Breakfast": ("Aisle 8 (Breakfast & Cereals)", 4),
    "Pantry": ("Aisle 11 (Pantry & Sauces)", 5),
    "Dairy": ("Aisle 16 (Dairy & Eggs)", 6),
}

class P2PService:
    def __init__(self):
        self.orders: Dict[str, P2POrderSchema] = {}
        self.wallets: Dict[str, ShopperWalletSchema] = {}
        self._seed_default_orders()

    def _seed_default_orders(self):
        # Default seed orders so in-store earner mode immediately has live neighbor orders
        order1 = P2POrderSchema(
            order_id="P2P-WM-849201",
            customer_id="cust_emily_09",
            customer_name="Emily K.",
            store_id="wm_bentonville_1842",
            store_name="Walmart Supercenter (#1842)",
            delivery_address="742 Evergreen Terrace, Apt 4A (0.6 mi away)",
            delivery_distance_miles=0.6,
            items=[
                P2PPickItem(
                    sku="WM-MILK-CONV-GAL",
                    title="Great Value Conventional Whole Milk Grade A",
                    brand="Great Value",
                    quantity=1.0,
                    unit="gallon",
                    unit_price=2.98,
                    barcode="0078742351866",
                    image_url="https://images.unsplash.com/photo-1563636619-e9143da7973b?w=500&auto=format&fit=crop&q=80",
                    aisle="Aisle 16 (Dairy & Eggs)",
                    is_picked=False,
                ),
                P2PPickItem(
                    sku="WM-BREAD-DAVE-21",
                    title="Dave's Killer Bread Organic 21 Whole Grains",
                    brand="Dave's Killer Bread",
                    quantity=1.0,
                    unit="oz",
                    unit_price=5.98,
                    barcode="0013764027053",
                    image_url="https://images.openfoodfacts.net/images/products/001/376/402/7053/front_en.454.400.jpg",
                    aisle="Aisle 3 (Artisan Bakery)",
                    is_picked=False,
                ),
                P2PPickItem(
                    sku="WM-CROISSANT-MKT-4",
                    title="Marketside All Butter Croissants (4 ct)",
                    brand="Marketside",
                    quantity=1.0,
                    unit="count",
                    unit_price=3.98,
                    barcode="0681131123456",
                    image_url="https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=500&auto=format&fit=crop&q=80",
                    aisle="Aisle 3 (Artisan Bakery)",
                    is_picked=False,
                ),
            ],
            items_subtotal=12.94,
            shopper_bounty=7.25,
            total_customer_charge=20.19,
            status="open",
            created_at="10 mins ago",
            notes="Please leave on porch by the blue planter. Thank you!",
        )

        order2 = P2POrderSchema(
            order_id="P2P-WM-302194",
            customer_id="cust_marcus_21",
            customer_name="Marcus T.",
            store_id="wm_bentonville_1842",
            store_name="Walmart Supercenter (#1842)",
            delivery_address="1208 Pine Ridge Rd (1.1 mi away)",
            delivery_distance_miles=1.1,
            items=[
                P2PPickItem(
                    sku="WM-EGGS-PAST-12",
                    title="Vital Farms Pasture-Raised Grade A Brown Eggs (12 ct)",
                    brand="Vital Farms",
                    quantity=1.0,
                    unit="count",
                    unit_price=5.98,
                    barcode="0861745000010",
                    image_url="https://images.openfoodfacts.net/images/products/086/174/500/0010/front_en.72.400.jpg",
                    aisle="Aisle 16 (Dairy & Eggs)",
                    is_picked=False,
                ),
                P2PPickItem(
                    sku="WM-YOGURT-CHOB-32",
                    title="Chobani Nonfat Plain Greek Yogurt (32 oz Tub)",
                    brand="Chobani",
                    quantity=1.0,
                    unit="oz",
                    unit_price=5.48,
                    barcode="0894700010137",
                    image_url="https://images.openfoodfacts.net/images/products/089/470/001/0137/front_en.640.400.jpg",
                    aisle="Aisle 16 (Dairy & Eggs)",
                    is_picked=False,
                ),
            ],
            items_subtotal=11.46,
            shopper_bounty=5.75,
            total_customer_charge=17.21,
            status="open",
            created_at="4 mins ago",
            notes="Gate code #4412. Quick drop-off, I work from home.",
        )

        self.orders[order1.order_id] = order1
        self.orders[order2.order_id] = order2

        # Seed earner wallet
        self.wallets["shopper_alex_01"] = ShopperWalletSchema(
            shopper_id="shopper_alex_01",
            available_balance=18.50,
            total_earned=142.25,
            completed_deliveries=14,
            rating=4.98,
            payouts_history=[
                {"date": "Yesterday", "amount": 6.50, "store": "Walmart", "items": 2},
                {"date": "2 days ago", "amount": 12.00, "store": "Walmart", "items": 4},
            ]
        )

    def get_open_orders(self, store_filter: Optional[str] = None) -> List[P2POrderSchema]:
        results = [o for o in self.orders.values() if o.status == "open"]
        if store_filter and store_filter.lower() != "all":
            results = [o for o in results if store_filter.lower() in o.store_id.lower() or store_filter.lower() in o.store_name.lower()]
        return results

    def get_order(self, order_id: str) -> Optional[P2POrderSchema]:
        return self.orders.get(order_id)

    def create_order_from_cart(self, cart: CartStateSchema, req: P2PCreateOrderRequest, catalog_dict: dict) -> P2POrderSchema:
        order_num = str(int(time.time() * 1000))[-6:]
        order_id = f"P2P-WM-{order_num}" if "walmart" in req.store.lower() else f"P2P-KR-{order_num}"
        store_name = "Walmart Supercenter (#1842)" if "walmart" in req.store.lower() else "Kroger Market (#402)"

        pick_items = []
        for ci in cart.items:
            cat_item = catalog_dict.get(ci.sku)
            category = cat_item.category if cat_item else "Pantry"
            barcode = cat_item.barcode if cat_item else "0078742000000"
            img = cat_item.image_url if cat_item else "https://images.unsplash.com/photo-1542838132-92c53300491e?w=500"
            aisle_info = AISLE_MAP.get(category, ("Aisle 10 (General Grocery)", 99))

            pick_items.append(
                P2PPickItem(
                    sku=ci.sku,
                    title=ci.title,
                    brand=ci.brand,
                    quantity=ci.quantity,
                    unit=ci.unit,
                    unit_price=ci.unit_price,
                    barcode=barcode,
                    image_url=img,
                    aisle=aisle_info[0],
                    is_picked=False,
                )
            )

        # Sort items along optimal physical store path (Bakery -> Produce -> Pantry -> Dairy)
        pick_items.sort(key=lambda x: [order for (name, order) in AISLE_MAP.values() if name == x.aisle] or [99])

        # Dynamic bounty: $4.00 base + $0.75 per item + tip
        item_count = len(pick_items)
        shopper_bounty = round(4.00 + (item_count * 0.75) + req.tip, 2)
        total_charge = round(cart.subtotal + cart.tax + shopper_bounty, 2)

        order = P2POrderSchema(
            order_id=order_id,
            customer_id=req.customer_id,
            customer_name=req.customer_name,
            store_id="wm_bentonville_1842" if "walmart" in req.store.lower() else "kr_market_402",
            store_name=store_name,
            delivery_address=req.delivery_address,
            delivery_distance_miles=0.8,
            items=pick_items,
            items_subtotal=cart.subtotal,
            shopper_bounty=shopper_bounty,
            total_customer_charge=total_charge,
            status="open",
            created_at="Just now",
            notes=req.notes,
        )

        self.orders[order_id] = order
        return order

    def accept_order(self, order_id: str, shopper_id: str, shopper_name: str) -> Optional[P2POrderSchema]:
        order = self.orders.get(order_id)
        if not order or order.status != "open":
            return None
        order.status = "picking"
        order.shopper_id = shopper_id
        order.shopper_name = shopper_name
        return order

    def pick_item(self, order_id: str, sku: str, barcode: Optional[str] = None) -> Optional[P2POrderSchema]:
        order = self.orders.get(order_id)
        if not order:
            return None
        
        for item in order.items:
            if item.sku.lower() == sku.lower():
                item.is_picked = True
                item.verified_barcode = barcode or item.barcode
                break

        # Check if all items picked
        if all(i.is_picked for i in order.items):
            order.status = "checkout_ready"

        return order

    def mark_out_for_delivery(self, order_id: str) -> Optional[P2POrderSchema]:
        order = self.orders.get(order_id)
        if not order:
            return None
        order.status = "out_for_delivery"
        return order

    def complete_delivery(self, order_id: str, shopper_id: str) -> Optional[P2POrderSchema]:
        order = self.orders.get(order_id)
        if not order:
            return None
        order.status = "completed"

        # Release bounty into shopper wallet
        wallet = self.get_or_create_wallet(shopper_id)
        wallet.available_balance = round(wallet.available_balance + order.shopper_bounty, 2)
        wallet.total_earned = round(wallet.total_earned + order.shopper_bounty, 2)
        wallet.completed_deliveries += 1
        wallet.payouts_history.insert(0, {
            "date": "Just now",
            "amount": order.shopper_bounty,
            "store": order.store_name,
            "items": len(order.items),
            "order_id": order.order_id,
        })
        return order

    def get_or_create_wallet(self, shopper_id: str) -> ShopperWalletSchema:
        if shopper_id not in self.wallets:
            self.wallets[shopper_id] = ShopperWalletSchema(shopper_id=shopper_id)
        return self.wallets[shopper_id]

    def cashout_wallet(self, shopper_id: str) -> Dict[str, Any]:
        wallet = self.get_or_create_wallet(shopper_id)
        cashed_out = wallet.available_balance
        wallet.available_balance = 0.0
        return {
            "status": "success",
            "amount_cashed_out": cashed_out,
            "destination": "Instant Transfer (Debit Card •••• 4821)",
            "remaining_balance": 0.0,
        }

p2p_service = P2PService()
