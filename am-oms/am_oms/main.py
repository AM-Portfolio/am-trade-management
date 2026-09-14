import asyncio
from contextlib import asynccontextmanager
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from am_platform_common import APIException, InternalServerError, LoggingMiddleware, setup_logging
from am_oms.api.routers import orders, prefs, wallets
from am_oms.core.config import get_settings
from am_oms.core.database import close_db, get_database, init_db, ping_db
from am_oms.deps import get_clients
from am_oms.matcher import matcher_loop
from am_oms.services import Oms

s = get_settings()
setup_logging(env=s.app_env, level=s.log_level)


@asynccontextmanager
async def lifespan(_: FastAPI):
    await init_db()
    task = asyncio.create_task(matcher_loop(Oms(get_database(), get_clients())))
    try:
        yield
    finally:
        task.cancel()
        await close_db()


app = FastAPI(title="AM OMS", version="0.1.0", lifespan=lifespan)
app.add_middleware(LoggingMiddleware)


@app.exception_handler(APIException)
async def api_ex(_: Request, exc: APIException) -> JSONResponse:
    return JSONResponse(status_code=exc.status_code, content=exc.to_dict())


@app.exception_handler(Exception)
async def unhandled(_: Request, exc: Exception) -> JSONResponse:
    body = InternalServerError(str(exc) if s.app_env.lower() in ("dev", "local") else "Internal server error")
    return JSONResponse(status_code=500, content=body.to_dict())


@app.get("/health")
@app.get("/health/live")
async def live() -> dict:
    return {"status": "ok", "service": s.app_name}


@app.get("/health/ready")
async def ready() -> dict:
    ok = await ping_db()
    return {"status": "ok" if ok else "degraded", "mongo": ok}


app.include_router(wallets)
app.include_router(orders)
app.include_router(prefs)
