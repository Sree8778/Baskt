import asyncio
import redis.asyncio as aioredis
from src.core.config import settings

class InMemoryAsyncRedis:
    """In-memory fallback cache when Redis daemon is not running."""
    def __init__(self):
        self._store: dict[str, str] = {}

    async def get(self, key: str):
        return self._store.get(key)

    async def set(self, key: str, value: str, ex: int | None = None):
        self._store[key] = value
        return True

    async def delete(self, key: str):
        if key in self._store:
            del self._store[key]
            return 1
        return 0

    async def ping(self):
        return True

    async def close(self):
        pass

_redis_client = None

async def get_redis_client():
    global _redis_client
    if _redis_client is None:
        try:
            client = aioredis.from_url(
                settings.redis_url,
                decode_responses=True,
                socket_connect_timeout=0.5,
                socket_timeout=0.5,
            )
            # test quick ping
            await asyncio.wait_for(client.ping(), timeout=0.6)
            _redis_client = client
        except Exception:
            # Fallback to high-speed in-memory dict for local execution
            _redis_client = InMemoryAsyncRedis()
    return _redis_client

async def close_redis_client():
    global _redis_client
    if _redis_client is not None:
        await _redis_client.close()
        _redis_client = None

async def get_redis():
    """FastAPI dependency for accessing Redis client."""
    return await get_redis_client()

