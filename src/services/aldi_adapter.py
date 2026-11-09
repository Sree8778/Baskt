from typing import List, Optional
from src.services.merchant_base import BaseMerchantAdapter
from src.models.schemas import GroceryItemSchema
from src.services.aldi_service import get_aldi_catalog, search_open_food_facts_aldi

class AldiAdapter(BaseMerchantAdapter):
    """
    ALDI US Merchant Adapter.
    Uses authentic ALDI private-label catalog (Friendly Farms, Simply Nature,
    Clancy's, L'oven Fresh, Millville, Specially Selected, Happy Farms) with
    authentic packaging photos and real ALDI barcodes (4099...).
    """
    def __init__(self, use_live_scraping: bool = True, proxy_url: Optional[str] = None):
        self.use_live_scraping = use_live_scraping
        self.proxy_url = proxy_url
        self._catalog = get_aldi_catalog()

    async def search_items(self, query: str, brand_preference: Optional[str] = None, organic_only: bool = False) -> List[GroceryItemSchema]:
        q_lower = query.lower()
        results = []
        for item in self._catalog:
            match = (
                q_lower in item.title.lower()
                or q_lower in item.category.lower()
                or q_lower in item.sku.lower()
                or q_lower in item.brand.lower()
            )
            if not match:
                continue
            if organic_only and not item.is_organic:
                continue
            if brand_preference and brand_preference.lower() not in item.brand.lower():
                continue
            results.append(item)

        if not results and self.use_live_scraping:
            live_results = await search_open_food_facts_aldi(query, limit=6)
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
        # Return same category items or allergen-safe swaps
        if "PB" in sku or "peanut" in target.title.lower():
            # Swap peanut butter for Simply Nature Almond Butter
            almond_butter = await self.get_item_by_sku("ALDI-AB-SIMPLY-NATURE")
            if almond_butter:
                return [almond_butter]
        return [i for i in self._catalog if i.sku != target.sku and i.category == target.category][:2]

    async def create_order(self, session_id: str, items: list, delivery_slot: str) -> dict:
        return {
            "merchant": "ALDI US",
            "order_id": f"ALDI-{session_id[-6:]}",
            "status": "CONFIRMED",
            "delivery_window": delivery_slot,
            "items_count": len(items),
        }
