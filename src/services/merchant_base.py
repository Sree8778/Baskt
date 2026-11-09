from abc import ABC, abstractmethod
from typing import List, Optional
from src.models.schemas import GroceryItemSchema

class BaseMerchantAdapter(ABC):
    @abstractmethod
    async def search_items(self, query: str, brand_preference: Optional[str] = None, organic_only: bool = False) -> List[GroceryItemSchema]:
        """Search items in merchant catalog matching query and filters."""
        pass

    @abstractmethod
    async def get_item_by_sku(self, sku: str) -> Optional[GroceryItemSchema]:
        """Fetch item details by SKU."""
        pass

    @abstractmethod
    async def check_stock(self, sku: str) -> bool:
        """Verify real-time stock availability."""
        pass

    @abstractmethod
    async def get_substitutions(self, sku: str) -> List[GroceryItemSchema]:
        """Retrieve top 2 viable replacement items."""
        pass

    @abstractmethod
    async def create_order(self, session_id: str, items: list, delivery_slot: str) -> dict:
        """Place external merchant order."""
        pass
