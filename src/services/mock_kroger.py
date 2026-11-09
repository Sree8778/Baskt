from typing import List, Optional
from src.services.merchant_base import BaseMerchantAdapter
from src.models.schemas import GroceryItemSchema
from src.services.real_products_service import get_real_products_for_merchant

class MockKrogerAdapter(BaseMerchantAdapter):
    """Simulates Kroger Product & Cart API with store brands like Simple Truth and national brands."""
    
    def __init__(self):
        self._catalog = get_real_products_for_merchant("kroger")

    async def search_items(self, query: str, brand_preference: Optional[str] = None, organic_only: bool = False) -> List[GroceryItemSchema]:
        query_lower = query.lower()
        results = []
        for item in self._catalog:
            match_title = (
                query_lower in item.title.lower()
                or query_lower in item.category.lower()
                or query_lower in item.sku.lower()
                or query_lower in item.brand.lower()
            )
            if not match_title:
                continue
            if organic_only and not item.is_organic:
                continue
            if brand_preference and brand_preference.lower() not in item.brand.lower():
                continue
            results.append(item)
        return results

    async def get_item_by_sku(self, sku: str) -> Optional[GroceryItemSchema]:
        for item in self._catalog:
            if item.sku.lower() == sku.lower():
                return item
        return None

    async def check_stock(self, sku: str) -> bool:
        item = await self.get_item_by_sku(sku)
        return item.in_stock if item else False

    async def get_substitutions(self, sku: str) -> List[GroceryItemSchema]:
        target = await self.get_item_by_sku(sku)
        if not target:
            return []
        
        # If peanut butter, recommend almond butter
        if "PB" in target.sku:
            ab = [i for i in self._catalog if "AB" in i.sku]
            if ab:
                return ab[:1]
        
        subs = [
            item for item in self._catalog 
            if item.sku != target.sku and (item.category == target.category or (target.category == "Dairy" and item.category == "Dairy Alternatives"))
        ]
        return subs[:2]

    async def create_order(self, session_id: str, items: list, delivery_slot: str) -> dict:
        return {
            "merchant": "Kroger",
            "order_id": f"KROG-{session_id[-6:]}",
            "status": "CONFIRMED",
            "delivery_window": delivery_slot,
            "items_count": len(items),
        }
