from motor.motor_asyncio import AsyncIOMotorClient, AsyncIOMotorDatabase

from am_strategy_lab.core.config import get_settings

_client: AsyncIOMotorClient | None = None
_db: AsyncIOMotorDatabase | None = None


async def init_db() -> None:
    global _client, _db
    s = get_settings()
    _client = AsyncIOMotorClient(s.effective_mongo_uri)
    _db = _client[s.mongo_database]
    await _db.strategies.create_index("strategyId", unique=True)
    await _db.backtests.create_index("jobId", unique=True)
    await _db.backtests.create_index([("strategyId", 1), ("createdAt", -1)])


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
        client = AsyncIOMotorClient(get_settings().effective_mongo_uri, serverSelectionTimeoutMS=2000)
        await client.admin.command("ping")
        client.close()
        return True
    except Exception:
        return False
