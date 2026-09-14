from motor.motor_asyncio import AsyncIOMotorClient, AsyncIOMotorDatabase
from am_oms.core.config import get_settings

_client: AsyncIOMotorClient | None = None
_db: AsyncIOMotorDatabase | None = None


async def init_db() -> None:
    global _client, _db
    s = get_settings()
    _client = AsyncIOMotorClient(s.effective_mongo_uri)
    _db = _client[s.mongo_database]
    await _db.wallets.create_index([("ownerId", 1), ("kind", 1)], unique=True)
    await _db.orders.create_index([("ownerId", 1), ("idempotencyKey", 1)], unique=True)
    await _db.orders.create_index("orderId", unique=True)
    await _db.orders.create_index([("venue", 1), ("status", 1), ("symbol", 1)])
    await _db.orders.create_index([("ownerId", 1), ("walletId", 1), ("createdAt", -1)])
    await _db.positions.create_index([("walletId", 1), ("symbol", 1)], unique=True)
    await _db.prefs.create_index("ownerId", unique=True)


def get_database() -> AsyncIOMotorDatabase:
    if _db is None:
        raise RuntimeError("Database not initialized")
    return _db


async def close_db() -> None:
    global _client, _db
    if _client:
        _client.close()
    _client = _db = None


async def ping_db() -> bool:
    try:
        await AsyncIOMotorClient(get_settings().effective_mongo_uri).admin.command("ping")
        return True
    except Exception:
        return False
