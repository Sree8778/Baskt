from typing import List, Optional
from src.services.merchant_base import BaseMerchantAdapter
from src.models.schemas import GroceryItemSchema

class MockInstacartAdapter(BaseMerchantAdapter):
    """Simulates Instacart Connect multi-store endpoints."""
    def __init__(self):
        self._catalog = [
            GroceryItemSchema(
                sku="IC-MILK-VALLEY-GAL",
                title="Organic Valley Whole Milk Jug",
                brand="Organic Valley",
                category="Dairy",
                price=6.19,
                standard_unit="gallon",
                is_organic=True,
                in_stock=True,
                ingredients=["Organic Grade A Milk", "Vitamin D3"],
            ),
            GroceryItemSchema(
                sku="IC-EGGS-VITAL-12",
                title="Vital Farms Organic Pasture-Raised Large Eggs",
                brand="Vital Farms",
                category="Dairy & Eggs",
                price=7.49,
                standard_unit="count",
                is_organic=True,
                in_stock=True,
                ingredients=["Organic Eggs"],
            ),
        ]

    async def search_items(self, query: str, brand_preference: Optional[str] = None, organic_only: bool = False) -> List[GroceryItemSchema]:
        q = query.lower()
        return [
            item for item in self._catalog
            if (q in item.title.lower() or q in item.category.lower())
            and (not organic_only or item.is_organic)
            and (not brand_preference or brand_preference.lower() in item.brand.lower())
        ]

    async def get_item_by_sku(self, sku: str) -> Optional[GroceryItemSchema]:
        for item in self._catalog:
            if item.sku == sku:
                return item
        return None

    async def check_stock(self, sku: str) -> bool:
        item = await self.get_item_by_sku(sku)
        return item.in_stock if item else False

    async def get_substitutions(self, sku: str) -> List[GroceryItemSchema]:
        target = await self.get_item_by_sku(sku)
        if not target:
            return []
        return [item for item in self._catalog if item.sku != target.sku][:2]

    async def create_order(self, session_id: str, items: list, delivery_slot: str) -> dict:
        return {
            "merchant": "Instacart Connect",
            "order_id": f"IC-{session_id[-6:]}",
            "status": "DISPATCHED",
            "delivery_window": delivery_slot,
            "items_count": len(items),
        }
