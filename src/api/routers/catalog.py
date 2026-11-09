from typing import Optional, List
from fastapi import APIRouter, Query
from src.models.schemas import GroceryItemSchema
from src.services.aldi_adapter import AldiAdapter
from src.services.mock_kroger import MockKrogerAdapter
from src.services.walmart_adapter import WalmartAdapter

router = APIRouter(prefix="/catalog", tags=["Catalog"])
_aldi = AldiAdapter()
_walmart = WalmartAdapter()
_kroger = MockKrogerAdapter()

@router.get("", response_model=List[GroceryItemSchema])
async def get_catalog(
    q: Optional[str] = Query(None, description="Search query"),
    organic_only: bool = Query(False, description="Filter for organic items"),
    category: Optional[str] = Query(None, description="Filter by category"),
    merchant: Optional[str] = Query("aldi", description="Filter by merchant: aldi, walmart, kroger, or all"),
):
    """Fetch grocery items with primary ALDI store catalog support and multi-merchant search."""
    if merchant.lower() == "aldi":
        adapters = [_aldi]
    elif merchant.lower() == "walmart":
        adapters = [_walmart]
    elif merchant.lower() == "kroger":
        adapters = [_kroger]
    else:
        adapters = [_aldi, _walmart, _kroger]

    items: List[GroceryItemSchema] = []
    for adapter in adapters:
        if q:
            results = await adapter.search_items(query=q, organic_only=organic_only)
        else:
            results = adapter._catalog
            if organic_only:
                results = [i for i in results if i.is_organic]
        items.extend(results)

    if category and category.lower() != "all":
        items = [i for i in items if i.category.lower() == category.lower()]

    return items

@router.get("/substitutions/{sku}", response_model=List[GroceryItemSchema])
async def get_substitutions(sku: str):
    """Retrieve top substitutions across merchants for a specific SKU."""
    if sku.startswith("ALDI-"):
        return await _aldi.get_substitutions(sku)
    elif sku.startswith("WM-"):
        return await _walmart.get_substitutions(sku)
    return await _kroger.get_substitutions(sku)
