from __future__ import annotations
from decimal import Decimal
from typing import Any
import httpx
from am_platform_common import APIException, BadRequestError, NotFoundError
from am_oms.clients import Clients
from am_oms.matcher import limit_touchable, stop_touchable
from am_oms.paper_execution import PaperExecutionEngine
from am_oms.schemas import (
    InstrumentType, OrderCreateRequest, OrderResponse, OrderSide, OrderStatus, OrderType,
    PositionListResponse, PositionResponse, PrefsResponse, Venue, WalletCreateRequest, WalletKind,
    WalletListResponse, WalletResponse, WalletSnapshot, money, now, uid,
)
from am_oms.session import phase_from_status

SEED = Decimal("1000000.00")
_ALLOWED_PRODUCT = {"", "INVESTING", "CNC", "DELIVERY"}


def _rej(code: str, msg: str, **d):
    raise BadRequestError(msg, error_code=code, details=d)


def apply_fill(avail: Decimal, reserved: Decimal, pos: Decimal, side: OrderSide, qty: Decimal, px: Decimal):
    n = qty * px
    if side == OrderSide.BUY:
        return avail - n, reserved, pos + qty
    return avail + n, reserved, pos - qty


def reserve_buy(avail: Decimal, reserved: Decimal, qty: Decimal, px: Decimal):
    n = qty * px
    return avail - n, reserved + n


def release_buy(avail: Decimal, reserved: Decimal, qty: Decimal, px: Decimal):
    n = qty * px
    return avail + n, reserved - n


def fill_buy_reserved(avail: Decimal, reserved: Decimal, pos: Decimal, qty: Decimal, res_px: Decimal, fill_px: Decimal):
    avail, reserved = release_buy(avail, reserved, qty, res_px)
    return apply_fill(avail, reserved, pos, OrderSide.BUY, qty, fill_px)


def _snap(w: dict) -> WalletSnapshot:
    return WalletSnapshot(available=money(w["available"]), reserved=money(w["reserved"]))


def _wallet(d: dict) -> WalletResponse:
    return WalletResponse(**{**d, "available": money(d["available"]), "reserved": money(d["reserved"])})


def _enum(v):
    return v.value if hasattr(v, "value") else v


def _order(d: dict, w: dict) -> OrderResponse:
    return OrderResponse(
        orderId=d["orderId"], walletId=d["walletId"], ownerId=d["ownerId"], venue=_enum(d["venue"]),
        instrumentType=_enum(d["instrumentType"]), symbol=d["symbol"], side=_enum(d["side"]),
        orderType=_enum(d["orderType"]), quantity=d["quantity"], status=_enum(d["status"]),
        fillPrice=d.get("fillPrice"), filledQuantity=d.get("filledQuantity"),
        rejectReason=d.get("rejectReason"), limitPrice=d.get("limitPrice"),
        triggerPrice=d.get("triggerPrice"), targetPrice=d.get("targetPrice"),
        stopLoss=d.get("stopLoss"), trailJump=d.get("trailJump"),
        ocoGroupId=d.get("ocoGroupId"), parentOrderId=d.get("parentOrderId"),
        option=d.get("option"), walletSnapshot=_snap(w), createdAt=d["createdAt"],
        updatedAt=d["updatedAt"], idempotencyKey=d["idempotencyKey"],
    )


class Oms:
    def __init__(self, db: Any, clients: Clients):
        self.db, self.clients = db, clients

    async def create_wallet(self, owner: str, token: str, req: WalletCreateRequest) -> tuple[WalletResponse, int]:
        if req.kind == WalletKind.LIVE:
            _rej("LIVE_NOT_ENABLED", "LIVE wallet is not enabled")
        found = await self.db.wallets.find_one({"ownerId": owner, "kind": "PAPER"})
        if found:
            return _wallet(found), 200
        seed = money(req.seedAmount or SEED)
        try:
            pid, pname = await self.clients.create_journal_portfolio(token, seed)
        except httpx.HTTPError as e:
            raise APIException("Journal portfolio create failed", error_code="JOURNAL_UNAVAILABLE", status_code=502) from e
        t = now()
        doc = {
            "walletId": uid(), "kind": "PAPER", "currency": req.currency, "available": seed,
            "reserved": money(0), "portfolioId": pname, "portfolioUuid": pid, "ownerId": owner,
            "createdAt": t, "updatedAt": t,
        }
        await self.db.wallets.insert_one(doc)
        return _wallet(doc), 201

    async def list_wallets(self, owner: str, kind: str | None) -> WalletListResponse:
        q: dict = {"ownerId": owner}
        if kind:
            q["kind"] = kind
        return WalletListResponse(items=[_wallet(x) for x in await self.db.wallets.find(q).to_list(50)])

    async def get_wallet(self, owner: str, wallet_id: str) -> WalletResponse:
        return _wallet(await self._wallet(owner, wallet_id))

    async def list_positions(self, owner: str, wallet_id: str) -> PositionListResponse:
        await self._wallet(owner, wallet_id)
        rows = await self.db.positions.find({"walletId": wallet_id}).to_list(100)
        return PositionListResponse(items=[
            PositionResponse(walletId=r["walletId"], symbol=r["symbol"], qty=money(r.get("qty", 0)))
            for r in rows if Decimal(str(r.get("qty", 0))) != 0
        ])

    async def create_order(self, owner: str, _token: str, key: str, req: OrderCreateRequest) -> tuple[OrderResponse, int]:
        existing = await self.db.orders.find_one({"ownerId": owner, "idempotencyKey": key})
        if existing:
            return _order(existing, await self._wallet(owner, existing["walletId"])), 200
        w = await self._wallet(owner, req.walletId)
        t, oid = now(), uid()
        if req.venue == Venue.LIVE:
            return await self._reject(oid, owner, key, req, t, w, "LIVE_NOT_ENABLED")
        if req.instrumentType == InstrumentType.OPTION:
            return await self._reject(oid, owner, key, req, t, w, "OPTIONS_NOT_ENABLED")
        if req.orderType == OrderType.STOP_LIMIT:
            return await self._reject(oid, owner, key, req, t, w, "STOP_LIMIT_NOT_ENABLED")
        if req.amo:
            return await self._reject(oid, owner, key, req, t, w, "AMO_NOT_SUPPORTED")
        pm = (req.productMode or "Investing").strip().upper()
        if pm not in _ALLOWED_PRODUCT:
            return await self._reject(oid, owner, key, req, t, w, "PRODUCT_NOT_SUPPORTED")

        status = await self.clients.market_status("NSE")
        phase = phase_from_status(status)
        engine = PaperExecutionEngine(phase)

        if req.orderType == OrderType.SUPER:
            return await self._create_super(owner, key, req, w, t, oid, engine)
        if req.orderType == OrderType.TRAIL:
            return await self._create_trail(owner, key, req, w, t, oid, engine)
        if req.orderType == OrderType.LIMIT:
            return await self._create_limit(owner, key, req, w, t, oid, engine)
        if req.orderType == OrderType.STOP:
            return await self._create_stop(owner, key, req, w, t, oid, engine)
        return await self._create_market(owner, key, req, w, t, oid, engine)

    async def cancel_order(self, owner: str, order_id: str) -> OrderResponse:
        d = await self.db.orders.find_one({"orderId": order_id, "ownerId": owner})
        if not d:
            raise NotFoundError("Order not found", error_code="NOT_FOUND")
        if _enum(d["status"]) != OrderStatus.ACCEPTED.value:
            _rej("CANCEL_NOT_ENABLED", "Only ACCEPTED orders can be cancelled")
        w = await self._wallet(owner, d["walletId"])
        t = now()
        claimed = await self._claim_cancel(d, t)
        if not claimed:
            d = await self.db.orders.find_one({"orderId": order_id, "ownerId": owner})
            return _order(d, w)
        await self._release_buy_reserve(w, d, t)
        await self._cancel_oco_siblings(d, t)
        d["status"], d["updatedAt"] = OrderStatus.CANCELLED.value, t
        if d.get("filledQuantity") is None:
            d["filledQuantity"] = money(0)
        return _order(d, w)

    async def cancel_all_orders(self, owner: str, wallet_id: str) -> int:
        await self._wallet(owner, wallet_id)
        rows = await self.db.orders.find({
            "ownerId": owner,
            "walletId": wallet_id,
            "status": OrderStatus.ACCEPTED.value,
        }).to_list(500)
        t = now()
        n = 0
        for d in rows:
            claimed = await self._claim_cancel(d, t)
            if not claimed:
                continue
            w = await self._wallet(owner, d["walletId"])
            await self._release_buy_reserve(w, d, t)
            await self._cancel_oco_siblings(d, t)
            n += 1
        return n

    async def _claim_cancel(self, d: dict, t) -> bool:
        """Race-safe ACCEPTED → CANCELLED. Returns False if already filled/cancelled."""
        before = await self.db.orders.find_one({
            "orderId": d["orderId"], "status": OrderStatus.ACCEPTED.value,
        })
        if not before:
            return False
        await self.db.orders.update_one(
            {"orderId": d["orderId"], "status": OrderStatus.ACCEPTED.value},
            {"$set": {
                "status": OrderStatus.CANCELLED.value,
                "updatedAt": t,
                "filledQuantity": before.get("filledQuantity") or money(0),
            }},
        )
        after = await self.db.orders.find_one({"orderId": d["orderId"]})
        return after is not None and _enum(after.get("status")) == OrderStatus.CANCELLED.value

    async def _release_buy_reserve(self, w: dict, d: dict, t) -> None:
        side = OrderSide(_enum(d["side"]))
        if side != OrderSide.BUY or not d.get("limitPrice"):
            return
        avail, reserved = Decimal(w["available"]), Decimal(w["reserved"])
        qty = Decimal(d["quantity"])
        avail, reserved = release_buy(avail, reserved, qty, Decimal(d["limitPrice"]))
        w["available"], w["reserved"], w["updatedAt"] = money(avail), money(reserved), t
        await self.db.wallets.update_one({"walletId": w["walletId"]}, {"$set": w})

    async def _cancel_oco_siblings(self, d: dict, t) -> None:
        oco = d.get("ocoGroupId")
        if not oco:
            return
        for sib in await self.db.orders.find({"ocoGroupId": oco, "status": OrderStatus.ACCEPTED.value}).to_list(10):
            if sib["orderId"] == d["orderId"]:
                continue
            await self._claim_cancel(sib, t)
            # SELL exits typically have no buy reserve; still attempt release if BUY+limit.
            try:
                w = await self._wallet(d["ownerId"], sib["walletId"])
                await self._release_buy_reserve(w, sib, t)
            except Exception:
                pass

    async def list_orders(
        self,
        owner: str,
        wallet_id: str | None,
        status: str | None,
        from_dt=None,
        to_dt=None,
    ) -> list[OrderResponse]:
        q: dict = {"ownerId": owner}
        if wallet_id:
            q["walletId"] = wallet_id
        if status:
            q["status"] = status
        if from_dt is not None or to_dt is not None:
            created: dict = {}
            if from_dt is not None:
                created["$gte"] = from_dt
            if to_dt is not None:
                created["$lte"] = to_dt
            q["createdAt"] = created
        rows = await self.db.orders.find(q).to_list(200)
        wallet_ids = {d["walletId"] for d in rows}
        wallets = {}
        for wid in wallet_ids:
            found = await self.db.wallets.find_one({"walletId": wid})
            wallets[wid] = found or {"available": "0", "reserved": "0"}
        return [_order(d, wallets.get(d["walletId"], {"available": "0", "reserved": "0"})) for d in rows]

    async def get_prefs(self, owner: str) -> PrefsResponse:
        doc = await self.db.prefs.find_one({"ownerId": owner})
        fav = (doc or {}).get("orderTypeFavorite") or "MARKET"
        return PrefsResponse(ownerId=owner, orderTypeFavorite=fav)

    async def put_prefs(self, owner: str, order_type_favorite: str) -> PrefsResponse:
        fav = (order_type_favorite or "MARKET").strip().upper()
        if fav not in {e.value for e in OrderType}:
            fav = "MARKET"
        t = now()
        existing = await self.db.prefs.find_one({"ownerId": owner})
        if existing:
            await self.db.prefs.update_one(
                {"ownerId": owner},
                {"$set": {"orderTypeFavorite": fav, "updatedAt": t}},
            )
        else:
            await self.db.prefs.insert_one({
                "ownerId": owner, "orderTypeFavorite": fav, "createdAt": t, "updatedAt": t,
            })
        return PrefsResponse(ownerId=owner, orderTypeFavorite=fav)

    async def get_order(self, owner: str, order_id: str) -> OrderResponse:
        d = await self.db.orders.find_one({"orderId": order_id, "ownerId": owner})
        if not d:
            raise NotFoundError("Order not found", error_code="NOT_FOUND")
        return _order(d, await self._wallet(owner, d["walletId"]))

    async def try_match_order(self, d: dict, ltp: Decimal | None = None) -> bool:
        if _enum(d["status"]) != OrderStatus.ACCEPTED.value:
            return False
        if _enum(d.get("venue", Venue.PAPER.value)) != Venue.PAPER.value:
            return False
        if ltp is None:
            ltp = await self.clients.ltp(d["symbol"], last_price_only=True, refresh=False)
        if ltp is None:
            return False
        ot = OrderType(_enum(d["orderType"]))
        side = OrderSide(_enum(d["side"]))
        if ot == OrderType.LIMIT:
            lim = Decimal(d["limitPrice"])
            if not limit_touchable(side, ltp, lim):
                return False
            fill_px = min(ltp, lim) if side == OrderSide.BUY else max(ltp, lim)
            return await self._complete_fill(d, fill_px, from_reserve=(side == OrderSide.BUY))
        if ot == OrderType.STOP:
            trig = Decimal(d.get("triggerPrice") or "0")
            if not stop_touchable(side, ltp, trig):
                return False
            return await self._complete_fill(d, ltp, from_reserve=False)
        if ot == OrderType.TRAIL:
            jump = Decimal(d.get("trailJump") or "0")
            peak = Decimal(d.get("trailPeak") or d.get("triggerPrice") or str(ltp))
            if side == OrderSide.SELL:
                if ltp > peak:
                    peak = ltp
                    await self.db.orders.update_one(
                        {"orderId": d["orderId"]},
                        {"$set": {"trailPeak": money(peak), "triggerPrice": money(peak - jump)}},
                    )
                    d["trailPeak"], d["triggerPrice"] = money(peak), money(peak - jump)
                trig = Decimal(d["triggerPrice"])
                if ltp > trig:
                    return False
                return await self._complete_fill(d, ltp, from_reserve=False)
            trough = Decimal(d.get("trailPeak") or str(ltp))
            if ltp < trough:
                trough = ltp
                await self.db.orders.update_one(
                    {"orderId": d["orderId"]},
                    {"$set": {"trailPeak": money(trough), "triggerPrice": money(trough + jump)}},
                )
                d["triggerPrice"] = money(trough + jump)
            if ltp < Decimal(d["triggerPrice"]):
                return False
            return await self._complete_fill(d, ltp, from_reserve=False)
        return False

    async def _create_market(self, owner, key, req, w, t, oid, engine: PaperExecutionEngine):
        code = engine.market_reject_code()
        if code:
            return await self._reject(oid, owner, key, req, t, w, code)
        ltp = await self.clients.ltp(req.symbol.upper(), last_price_only=True, refresh=True)
        if ltp is None:
            return await self._reject(oid, owner, key, req, t, w, "LTP_UNAVAILABLE")
        return await self._fill_now(oid, owner, key, req, w, t, ltp, reserve=False)

    async def _create_limit(self, owner, key, req, w, t, oid, engine: PaperExecutionEngine):
        if not req.limitPrice:
            return await self._reject(oid, owner, key, req, t, w, "VALIDATION")
        lim = Decimal(req.limitPrice)
        if not engine.can_immediate_touch_fill():
            return await self._rest(oid, owner, key, req, w, t, do_reserve=(req.side == OrderSide.BUY), limit=req.limitPrice)
        ltp = await self.clients.ltp(req.symbol.upper(), last_price_only=True, refresh=True)
        if ltp is None:
            return await self._reject(oid, owner, key, req, t, w, "LTP_UNAVAILABLE")
        if limit_touchable(req.side, ltp, lim):
            fill_px = min(ltp, lim) if req.side == OrderSide.BUY else max(ltp, lim)
            return await self._fill_now(oid, owner, key, req, w, t, fill_px, reserve=False, limit=req.limitPrice)
        return await self._rest(oid, owner, key, req, w, t, do_reserve=(req.side == OrderSide.BUY), limit=req.limitPrice)

    async def _create_stop(self, owner, key, req, w, t, oid, engine: PaperExecutionEngine):
        trig = req.triggerPrice or req.stopLoss
        if not trig:
            return await self._reject(oid, owner, key, req, t, w, "VALIDATION")
        if not engine.can_immediate_touch_fill():
            return await self._rest(oid, owner, key, req, w, t, do_reserve=False, trigger=trig)
        ltp = await self.clients.ltp(req.symbol.upper(), last_price_only=True, refresh=True)
        if ltp is None:
            return await self._reject(oid, owner, key, req, t, w, "LTP_UNAVAILABLE")
        if stop_touchable(req.side, ltp, Decimal(trig)):
            return await self._fill_now(oid, owner, key, req, w, t, ltp, reserve=False, trigger=trig)
        return await self._rest(oid, owner, key, req, w, t, do_reserve=False, trigger=trig)

    async def _create_trail(self, owner, key, req, w, t, oid, engine: PaperExecutionEngine):
        if not req.trailJump:
            return await self._reject(oid, owner, key, req, t, w, "VALIDATION")
        # Trail always rests; peak seeded when OPEN LTP available, else rest with jump only.
        ltp = None
        if engine.can_immediate_touch_fill():
            ltp = await self.clients.ltp(req.symbol.upper(), last_price_only=True, refresh=True)
        if ltp is None:
            # Rest without peak until matcher has LTP
            jump = Decimal(req.trailJump)
            doc = self._base_order(oid, owner, key, req, t, OrderStatus.ACCEPTED, None, None, money(0),
                                  trigger=None, trail=req.trailJump)
            doc["trailJump"] = money(jump)
            await self.db.orders.insert_one(doc)
            return _order(doc, w), 201
        jump = Decimal(req.trailJump)
        trig = money(ltp - jump if req.side == OrderSide.SELL else ltp + jump)
        doc = self._base_order(oid, owner, key, req, t, OrderStatus.ACCEPTED, None, None, money(0),
                              trigger=trig, trail=req.trailJump)
        doc["trailPeak"] = money(ltp)
        await self.db.orders.insert_one(doc)
        return _order(doc, w), 201

    async def _create_super(self, owner, key, req, w, t, oid, engine: PaperExecutionEngine):
        entry = req.entryType or OrderType.MARKET
        if entry not in (OrderType.MARKET, OrderType.LIMIT):
            entry = OrderType.MARKET
        child = req.model_copy(update={"orderType": entry})
        if entry == OrderType.LIMIT:
            resp, code = await self._create_limit(owner, key, child, w, t, oid, engine)
        else:
            resp, code = await self._create_market(owner, key, child, w, t, oid, engine)
        await self.db.orders.update_one(
            {"orderId": resp.orderId},
            {"$set": {
                "orderType": OrderType.SUPER.value,
                "targetPrice": req.targetPrice,
                "stopLoss": req.stopLoss,
                "entryType": entry.value,
            }},
        )
        d = await self.db.orders.find_one({"orderId": resp.orderId})
        w2 = await self._wallet(owner, req.walletId)
        if d and _enum(d["status"]) == OrderStatus.FILLED.value:
            await self._spawn_oco_exits(owner, d, req.targetPrice, req.stopLoss)
            d = await self.db.orders.find_one({"orderId": resp.orderId})
            w2 = await self._wallet(owner, req.walletId)
        elif d and _enum(d["status"]) == OrderStatus.ACCEPTED.value:
            await self.db.orders.update_one(
                {"orderId": d["orderId"]},
                {"$set": {"pendingOco": True, "targetPrice": req.targetPrice, "stopLoss": req.stopLoss}},
            )
        return _order(d, w2), code

    async def _spawn_oco_exits(self, owner: str, parent: dict, target: str | None, stop: str | None):
        if not target and not stop:
            return
        group, t = uid(), now()
        qty = parent["quantity"]
        sym = parent["symbol"]
        exit_side = OrderSide.SELL if _enum(parent["side"]) == OrderSide.BUY.value else OrderSide.BUY
        wid = parent["walletId"]
        if target:
            lim_req = OrderCreateRequest(
                walletId=wid, venue=Venue.PAPER, instrumentType=InstrumentType.EQUITY,
                symbol=sym, side=exit_side, orderType=OrderType.LIMIT, quantity=qty,
                limitPrice=target,
            )
            oid = uid()
            doc = self._base_order(oid, owner, f"oco-tgt-{parent['orderId']}", lim_req, t,
                                  OrderStatus.ACCEPTED, None, None, None, limit=target)
            doc["ocoGroupId"], doc["parentOrderId"] = group, parent["orderId"]
            await self.db.orders.insert_one(doc)
        if stop:
            stop_req = OrderCreateRequest(
                walletId=wid, venue=Venue.PAPER, instrumentType=InstrumentType.EQUITY,
                symbol=sym, side=exit_side, orderType=OrderType.STOP, quantity=qty,
                triggerPrice=stop, stopLoss=stop,
            )
            oid = uid()
            doc = self._base_order(oid, owner, f"oco-sl-{parent['orderId']}", stop_req, t,
                                  OrderStatus.ACCEPTED, None, None, None, trigger=stop)
            doc["ocoGroupId"], doc["parentOrderId"] = group, parent["orderId"]
            await self.db.orders.insert_one(doc)
        await self.db.orders.update_one({"orderId": parent["orderId"]}, {"$set": {"ocoGroupId": group}})

    async def _fill_now(self, oid, owner, key, req, w, t, px, reserve=False, limit=None, trigger=None):
        qty = Decimal(req.quantity)
        avail, reserved = Decimal(w["available"]), Decimal(w["reserved"])
        pos = await self.db.positions.find_one({"walletId": w["walletId"], "symbol": req.symbol.upper()}) or {"qty": "0"}
        pq = Decimal(pos["qty"])
        if req.side == OrderSide.BUY and qty * px > avail:
            return await self._reject(oid, owner, key, req, t, w, "INSUFFICIENT_CASH", limit=limit, trigger=trigger)
        if req.side == OrderSide.SELL and qty > pq:
            return await self._reject(oid, owner, key, req, t, w, "INSUFFICIENT_QTY", limit=limit, trigger=trigger)
        avail, reserved, pq = apply_fill(avail, reserved, pq, req.side, qty, px)
        return await self._persist_fill(oid, owner, key, req, w, t, px, qty, avail, reserved, pq, limit=limit, trigger=trigger)

    async def _rest(self, oid, owner, key, req, w, t, do_reserve=False, limit=None, trigger=None):
        qty = Decimal(req.quantity)
        avail, reserved = Decimal(w["available"]), Decimal(w["reserved"])
        pos = await self.db.positions.find_one({"walletId": w["walletId"], "symbol": req.symbol.upper()}) or {"qty": "0"}
        pq = Decimal(pos["qty"])
        if do_reserve:
            px = Decimal(limit)
            if qty * px > avail:
                return await self._reject(oid, owner, key, req, t, w, "INSUFFICIENT_CASH", limit=limit)
            avail, reserved = reserve_buy(avail, reserved, qty, px)
            w["available"], w["reserved"], w["updatedAt"] = money(avail), money(reserved), t
            await self.db.wallets.update_one({"walletId": w["walletId"]}, {"$set": w})
        elif req.side == OrderSide.SELL and qty > pq:
            return await self._reject(oid, owner, key, req, t, w, "INSUFFICIENT_QTY", limit=limit, trigger=trigger)
        doc = self._base_order(oid, owner, key, req, t, OrderStatus.ACCEPTED, None, None, None, limit=limit, trigger=trigger)
        await self.db.orders.insert_one(doc)
        return _order(doc, w), 201

    async def _complete_fill(self, d: dict, fill_px: Decimal, from_reserve: bool) -> bool:
        w = await self._wallet(d["ownerId"], d["walletId"])
        t = now()
        qty = Decimal(d["quantity"])
        side = OrderSide(_enum(d["side"]))
        avail, reserved = Decimal(w["available"]), Decimal(w["reserved"])
        pos = await self.db.positions.find_one({"walletId": w["walletId"], "symbol": d["symbol"]}) or {"qty": "0"}
        pq = Decimal(pos["qty"])
        if from_reserve and side == OrderSide.BUY:
            res_px = Decimal(d["limitPrice"])
            avail, reserved, pq = fill_buy_reserved(avail, reserved, pq, qty, res_px, fill_px)
        else:
            if side == OrderSide.BUY and qty * fill_px > avail:
                return False
            if side == OrderSide.SELL and qty > pq:
                return False
            avail, reserved, pq = apply_fill(avail, reserved, pq, side, qty, fill_px)
        w["available"], w["reserved"], w["updatedAt"] = money(avail), money(reserved), t
        await self.db.wallets.update_one({"walletId": w["walletId"]}, {"$set": w})
        await self.db.positions.update_one(
            {"walletId": w["walletId"], "symbol": d["symbol"]},
            {"$set": {"walletId": w["walletId"], "symbol": d["symbol"], "qty": money(pq)}},
            upsert=True,
        )
        d["status"], d["fillPrice"], d["filledQuantity"], d["updatedAt"] = (
            OrderStatus.FILLED.value, money(fill_px), money(qty), t,
        )
        await self.db.orders.update_one(
            {"orderId": d["orderId"]},
            {"$set": {"status": d["status"], "fillPrice": d["fillPrice"], "filledQuantity": d["filledQuantity"], "updatedAt": t}},
        )
        await self.clients.publish_fill({
            "eventId": uid(), "source": "am-oms", "dataVersion": "1.0", "orderId": d["orderId"],
            "walletId": w["walletId"], "ownerId": d["ownerId"], "action": side.value,
            "symbol": d["symbol"], "quantity": money(qty), "price": money(fill_px),
            "instrumentType": "EQUITY", "portfolioKind": "PAPER",
            "id": w["portfolioUuid"], "portfolioId": w["portfolioId"], "timestamp": t.isoformat(),
        })
        oco = d.get("ocoGroupId")
        if oco:
            for sib in await self.db.orders.find({"ocoGroupId": oco, "status": OrderStatus.ACCEPTED.value}).to_list(10):
                if sib["orderId"] == d["orderId"]:
                    continue
                await self.db.orders.update_one(
                    {"orderId": sib["orderId"]},
                    {"$set": {"status": OrderStatus.CANCELLED.value, "updatedAt": t}},
                )
        if d.get("pendingOco"):
            await self._spawn_oco_exits(d["ownerId"], d, d.get("targetPrice"), d.get("stopLoss"))
        return True

    async def _persist_fill(self, oid, owner, key, req, w, t, px, qty, avail, reserved, pq, limit=None, trigger=None):
        w["available"], w["reserved"], w["updatedAt"] = money(avail), money(reserved), t
        await self.db.wallets.update_one({"walletId": w["walletId"]}, {"$set": w})
        await self.db.positions.update_one(
            {"walletId": w["walletId"], "symbol": req.symbol.upper()},
            {"$set": {"walletId": w["walletId"], "symbol": req.symbol.upper(), "qty": money(pq)}},
            upsert=True,
        )
        doc = self._base_order(oid, owner, key, req, t, OrderStatus.FILLED, None, money(px), money(qty),
                               limit=limit, trigger=trigger)
        await self.db.orders.insert_one(doc)
        await self.clients.publish_fill({
            "eventId": uid(), "source": "am-oms", "dataVersion": "1.0", "orderId": oid,
            "walletId": w["walletId"], "ownerId": owner, "action": req.side.value,
            "symbol": req.symbol.upper(), "quantity": money(qty), "price": money(px),
            "instrumentType": req.instrumentType.value, "portfolioKind": "PAPER",
            "id": w["portfolioUuid"], "portfolioId": w["portfolioId"], "timestamp": t.isoformat(),
        })
        return _order(doc, w), 201

    async def _reject(self, oid, owner, key, req, t, w, reason, limit=None, trigger=None):
        doc = self._base_order(oid, owner, key, req, t, OrderStatus.REJECTED, reason, None, None,
                              limit=limit, trigger=trigger)
        await self.db.orders.insert_one(doc)
        return _order(doc, w), 201

    async def _wallet(self, owner: str, wallet_id: str) -> dict:
        w = await self.db.wallets.find_one({"walletId": wallet_id, "ownerId": owner})
        if not w:
            raise NotFoundError("Wallet not found", error_code="NOT_FOUND")
        return w

    def _base_order(self, oid, owner, key, req, t, status, reason, fill, filled, limit=None, trigger=None, trail=None):
        st = status.value if isinstance(status, OrderStatus) else status
        filled_qty = filled if filled is not None else money(0)
        return {
            "orderId": oid, "walletId": req.walletId, "ownerId": owner, "venue": _enum(req.venue),
            "instrumentType": _enum(req.instrumentType), "symbol": req.symbol.upper(), "side": _enum(req.side),
            "orderType": _enum(req.orderType), "quantity": req.quantity, "status": st,
            "fillPrice": fill, "filledQuantity": filled_qty, "rejectReason": reason,
            "limitPrice": limit or req.limitPrice, "triggerPrice": trigger or req.triggerPrice,
            "targetPrice": req.targetPrice, "stopLoss": req.stopLoss,
            "trailJump": trail or req.trailJump,
            "option": req.option.model_dump() if req.option else None,
            "createdAt": t, "updatedAt": t, "idempotencyKey": key,
        }
