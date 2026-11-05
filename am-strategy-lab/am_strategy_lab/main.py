from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from am_strategy_lab.api.routers import backtests, strategies
from am_strategy_lab.core.config import get_settings
from am_strategy_lab.core.database import close_db, get_database, init_db, ping_db
from am_strategy_lab.memory_db import MemoryDatabase
from am_strategy_lab.services import StrategyLabService

s = get_settings()


@asynccontextmanager
async def lifespan(app: FastAPI):
    if s.memory_db:
        app.state.lab = StrategyLabService(MemoryDatabase())
        app.state.memory = True
    else:
        await init_db()
        app.state.lab = StrategyLabService(get_database())
        app.state.memory = False
    try:
        yield
    finally:
        if not getattr(app.state, "memory", False):
            await close_db()


app = FastAPI(title="AM Strategy Lab", version="0.1.0", lifespan=lifespan)


@app.exception_handler(Exception)
async def unhandled(_: Request, exc: Exception) -> JSONResponse:
    msg = str(exc) if s.app_env.lower() in ("dev", "local") else "Internal server error"
    return JSONResponse(status_code=500, content={"error": msg})


@app.get("/health")
@app.get("/health/live")
@app.get("/v1/health")
async def live() -> dict:
    return {"status": "ok", "service": s.app_name}


@app.get("/health/ready")
async def ready() -> dict:
    if s.memory_db:
        return {"status": "ok", "mongo": False, "memory": True}
    ok = await ping_db()
    return {"status": "ok" if ok else "degraded", "mongo": ok}


app.include_router(strategies)
app.include_router(backtests)
