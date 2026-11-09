import httpx
from typing import List, Optional
from src.services.merchant_base import BaseMerchantAdapter
from src.models.schemas import GroceryItemSchema
from src.services.real_products_service import get_real_products_for_merchant, search_open_food_facts_live

class WalmartAdapter(BaseMerchantAdapter):
    """
    Walmart Merchant Adapter.
    Uses authentic US supermarket catalog with real packaging photography, barcodes,
    and pluggable Open Food Facts live search.
    """
    def __init__(self, use_live_scraping: bool = True, proxy_url: Optional[str] = None):
        self.use_live_scraping = use_live_scraping
        self.proxy_url = proxy_url
        self._catalog = get_real_products_for_merchant("walmart")

    async def search_items(self, query: str, brand_preference: Optional[str] = None, organic_only: bool = False) -> List[GroceryItemSchema]:
        q_lower = query.lower()
        results = []
        for item in self._catalog:
            match = q_lower in item.title.lower() or q_lower in item.category.lower() or q_lower in item.sku.lower() or q_lower in item.brand.lower()
            if not match:
                continue
            if organic_only and not item.is_organic:
                continue
            if brand_preference and brand_preference.lower() not in item.brand.lower():
                continue
            results.append(item)
            
        if not results and self.use_live_scraping:
            live_results = await search_open_food_facts_live(query, limit=6, store="walmart")
            results.extend(live_results)
            
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
        return [i for i in self._catalog if i.sku != target.sku and i.category == target.category][:2]

    async def create_order(self, session_id: str, items: list, delivery_slot: str) -> dict:
        return {
            "merchant": "Walmart Grocery Delivery",
            "order_id": f"WM-{session_id[-6:]}",
            "status": "SCHEDULED",
            "delivery_window": delivery_slot,
            "items_count": len(items),
        }
