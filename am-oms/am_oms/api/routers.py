from fastapi import APIRouter, Depends, Header, Query, Request
from fastapi.responses import JSONResponse
from am_platform_common import APIResponse
from am_platform_security import AuthContext, require_auth_context
from am_oms.deps import get_oms
from am_oms.schemas import OrderCreateRequest, OrderListResponse, WalletCreateRequest, WalletListResponse
from am_oms.services import Oms

wallets = APIRouter(prefix="/v1/wallets", tags=["wallets"])
orders = APIRouter(prefix="/v1/orders", tags=["orders"])


def _token(request: Request) -> str:
    h = request.headers.get("authorization") or ""
    return h.split(" ", 1)[1] if h.lower().startswith("bearer ") else ""


@wallets.post("")
async def create_wallet(req: WalletCreateRequest, request: Request, ctx: AuthContext = Depends(require_auth_context()), oms: Oms = Depends(get_oms)):
    data, code = await oms.create_wallet(ctx.subject, _token(request), req)
    return JSONResponse(APIResponse(data=data.model_dump(mode="json")).model_dump(mode="json"), status_code=code)


@wallets.get("", response_model=APIResponse[WalletListResponse])
async def list_wallets(kind: str | None = None, ctx: AuthContext = Depends(require_auth_context()), oms: Oms = Depends(get_oms)):
    return APIResponse(data=await oms.list_wallets(ctx.subject, kind))


@wallets.get("/{wallet_id}")
async def get_wallet(wallet_id: str, ctx: AuthContext = Depends(require_auth_context()), oms: Oms = Depends(get_oms)):
    return APIResponse(data=await oms.get_wallet(ctx.subject, wallet_id))


@wallets.get("/{wallet_id}/positions")
async def list_positions(wallet_id: str, ctx: AuthContext = Depends(require_auth_context()), oms: Oms = Depends(get_oms)):
    return APIResponse(data=await oms.list_positions(ctx.subject, wallet_id))


@wallets.get("/{wallet_id}/ledger")
async def ledger():
    return JSONResponse({"error_code": "NOT_IMPLEMENTED", "message": "ledger P1"}, status_code=501)


@orders.post("")
async def create_order(
    req: OrderCreateRequest,
    request: Request,
    idempotency_key: str = Header(..., alias="Idempotency-Key"),
    ctx: AuthContext = Depends(require_auth_context()),
    oms: Oms = Depends(get_oms),
):
    data, code = await oms.create_order(ctx.subject, _token(request), idempotency_key, req)
    return JSONResponse(APIResponse(data=data.model_dump(mode="json")).model_dump(mode="json"), status_code=code)


@orders.get("", response_model=APIResponse[OrderListResponse])
async def list_orders(walletId: str | None = None, status: str | None = Query(None), ctx: AuthContext = Depends(require_auth_context()), oms: Oms = Depends(get_oms)):
    return APIResponse(data=OrderListResponse(items=await oms.list_orders(ctx.subject, walletId, status)))


@orders.get("/{order_id}")
async def get_order(order_id: str, ctx: AuthContext = Depends(require_auth_context()), oms: Oms = Depends(get_oms)):
    return APIResponse(data=await oms.get_order(ctx.subject, order_id))


@orders.post("/{order_id}/cancel")
async def cancel_order(order_id: str, ctx: AuthContext = Depends(require_auth_context()), oms: Oms = Depends(get_oms)):
    return APIResponse(data=await oms.cancel_order(ctx.subject, order_id))


@orders.post("/{order_id}/replace")
async def replace_order():
    return JSONResponse({"error_code": "REPLACE_NOT_ENABLED", "message": "replace P1"}, status_code=400)
