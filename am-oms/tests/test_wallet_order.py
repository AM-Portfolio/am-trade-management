from decimal import Decimal
from datetime import datetime, timezone

import httpx
import pytest
from am_platform_common import APIException
from am_oms.matcher import match_once
from am_oms.schemas import InstrumentType, OrderCreateRequest, OrderSide, OrderType, Venue, WalletCreateRequest, WalletKind
from am_oms.services import Oms
from am_oms.session import MarketStatusView, SessionPhase, phase_from_status, status_from_payload


class Cur:
    def __init__(self, rows):
        self.rows = rows

    async def to_list(self, n):
        return self.rows[:n]


def _match(d, q):
    for k, v in q.items():
        if isinstance(v, dict) and any(str(op).startswith("$") for op in v):
            val = d.get(k)
            if "$gte" in v and not (val is not None and val >= v["$gte"]):
                return False
            if "$lte" in v and not (val is not None and val <= v["$lte"]):
                return False
        elif d.get(k) != v:
            return False
    return True


class Col:
    def __init__(self):
        self.rows = []

    async def find_one(self, q):
        for d in self.rows:
            if _match(d, q):
                return d
        return None

    async def insert_one(self, doc):
        self.rows.append(doc)

    async def update_one(self, q, upd, upsert=False):
        d = await self.find_one(q)
        if d:
            d.update(upd.get("$set", {}))
        elif upsert:
            self.rows.append({**q, **upd.get("$set", {})})

    def find(self, q):
        return Cur([d for d in self.rows if _match(d, q)])


class DB:
    def __init__(self):
        self.wallets, self.orders, self.positions, self.prefs = Col(), Col(), Col(), Col()


class Fake:
    def __init__(self, ltp=Decimal("100"), fail_journal=False, phase=SessionPhase.OPEN, calendar_ok=True):
        self.ltp_v, self.fail_journal, self.events = ltp, fail_journal, []
        self.phase = phase
        self.calendar_ok = calendar_ok
        self.ohlc_calls = 0
        self.last_price_only_calls = []

    async def create_journal_portfolio(self, token, seed):
        if self.fail_journal:
            raise httpx.ConnectError("down")
        return "pf-1", "Paper"

    async def market_status(self, exchange="NSE"):
        if not self.calendar_ok:
            return MarketStatusView(open=False, reason="CALENDAR_UNAVAILABLE", available=False)
        if self.phase == SessionPhase.OPEN:
            return MarketStatusView(open=True, reason="OPEN", available=True)
        if self.phase == SessionPhase.PREOPEN:
            # session_start late so OUTSIDE_SESSION maps to PREOPEN regardless of wall clock
            return MarketStatusView(
                open=False, reason="OUTSIDE_SESSION",
                session_start=__import__("datetime").time(23, 59, 59),
                session_end=__import__("datetime").time(23, 59, 59),
                available=True,
            )
        return MarketStatusView(open=False, reason="WEEKEND", available=True)

    async def ltp(self, symbol, *, last_price_only=False, refresh=True):
        self.last_price_only_calls.append(last_price_only)
        prices = await self.ltp_many([symbol], last_price_only=last_price_only, refresh=refresh, matcher=False)
        return prices.get(symbol.upper())

    async def ltp_many(self, symbols, *, last_price_only=False, refresh=False, matcher=True):
        self.ohlc_calls += 1
        out = {}
        for s in symbols:
            u = s.upper()
            if u == "BAD":
                continue
            out[u] = self.ltp_v
        return out

    async def publish_fill(self, event):
        self.events.append(event)


def _req(w, **kw):
    base = dict(
        walletId=w.walletId, venue=Venue.PAPER, instrumentType=InstrumentType.EQUITY,
        symbol="RELIANCE", side=OrderSide.BUY, orderType=OrderType.MARKET, quantity="2",
    )
    base.update(kw)
    return OrderCreateRequest(**base)


@pytest.mark.asyncio
async def test_seed_and_second_wallet():
    oms = Oms(DB(), Fake())
    w, c = await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.PAPER))
    assert c == 201 and w.available == "1000000.00"
    w2, c2 = await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.PAPER))
    assert c2 == 200 and w2.walletId == w.walletId


@pytest.mark.asyncio
async def test_live_wallet_rejected():
    oms = Oms(DB(), Fake())
    with pytest.raises(Exception) as e:
        await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.LIVE))
    assert "LIVE" in str(e.value.error_code)


@pytest.mark.asyncio
async def test_buy_sell_and_rejects():
    db, fake = DB(), Fake()
    oms = Oms(db, fake)
    w, _ = await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.PAPER))
    req = _req(w)
    o, _ = await oms.create_order("u1", "tok", "k1", req)
    assert o.status.value == "FILLED" and o.fillPrice == "100.00"
    assert o.filledQuantity == "2.00"
    assert (await oms.get_wallet("u1", w.walletId)).available == "999800.00"
    o2, _ = await oms.create_order("u1", "tok", "k1", req)
    assert o2.orderId == o.orderId
    o3, _ = await oms.create_order("u1", "tok", "k2", _req(w, quantity="999999"))
    assert o3.rejectReason == "INSUFFICIENT_CASH"
    o4, _ = await oms.create_order("u1", "tok", "k3", _req(w, side=OrderSide.SELL, quantity="2"))
    assert o4.status.value == "FILLED"
    o5, _ = await oms.create_order("u1", "tok", "k4", _req(w, side=OrderSide.SELL, quantity="9"))
    assert o5.rejectReason == "INSUFFICIENT_QTY"
    o6, _ = await oms.create_order("u1", "tok", "k5", _req(w, symbol="BAD"))
    assert o6.rejectReason == "LTP_UNAVAILABLE"
    o7, _ = await oms.create_order("u1", "tok", "k6", _req(w, instrumentType=InstrumentType.OPTION))
    assert o7.rejectReason == "OPTIONS_NOT_ENABLED"
    o8, _ = await oms.create_order("u1", "tok", "k7", _req(w, venue=Venue.LIVE))
    assert o8.rejectReason == "LIVE_NOT_ENABLED"
    assert fake.events
    assert True in fake.last_price_only_calls


@pytest.mark.asyncio
async def test_limit_touch_and_rest_match():
    db, fake = DB(), Fake(ltp=Decimal("100"))
    oms = Oms(db, fake)
    w, _ = await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.PAPER))
    o, _ = await oms.create_order("u1", "tok", "l1", _req(w, orderType=OrderType.LIMIT, limitPrice="100", quantity="1"))
    assert o.status.value == "FILLED"
    o2, _ = await oms.create_order("u1", "tok", "l2", _req(w, orderType=OrderType.LIMIT, limitPrice="90", quantity="1"))
    assert o2.status.value == "ACCEPTED"
    assert o2.filledQuantity == "0.00"
    assert Decimal((await oms.get_wallet("u1", w.walletId)).reserved) == Decimal("90.00")
    fake.ltp_v = Decimal("85")
    before_calls = fake.ohlc_calls
    assert await match_once(oms) >= 1
    assert fake.ohlc_calls == before_calls + 1
    o2b = await oms.get_order("u1", o2.orderId)
    assert o2b.status.value == "FILLED"


@pytest.mark.asyncio
async def test_super_and_trail_and_cancel():
    db, fake = DB(), Fake(ltp=Decimal("100"))
    oms = Oms(db, fake)
    w, _ = await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.PAPER))
    o, _ = await oms.create_order(
        "u1", "tok", "s1",
        _req(w, orderType=OrderType.SUPER, quantity="1", targetPrice="110", stopLoss="90"),
    )
    assert o.status.value == "FILLED"
    working = [x for x in await oms.list_orders("u1", w.walletId, "ACCEPTED")]
    assert len(working) == 2
    t, _ = await oms.create_order(
        "u1", "tok", "t1",
        _req(w, orderType=OrderType.TRAIL, side=OrderSide.SELL, quantity="1", trailJump="5"),
    )
    assert t.status.value == "ACCEPTED"
    c = await oms.cancel_order("u1", t.orderId)
    assert c.status.value == "CANCELLED"
    lim, _ = await oms.create_order("u1", "tok", "l3", _req(w, orderType=OrderType.LIMIT, limitPrice="50", quantity="1"))
    assert lim.status.value == "ACCEPTED"
    before = (await oms.get_wallet("u1", w.walletId)).available
    await oms.cancel_order("u1", lim.orderId)
    after = (await oms.get_wallet("u1", w.walletId)).available
    assert Decimal(after) > Decimal(before)


@pytest.mark.asyncio
async def test_market_closed_and_preopen():
    db, fake = DB(), Fake(phase=SessionPhase.CLOSED)
    oms = Oms(db, fake)
    w, _ = await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.PAPER))
    m, _ = await oms.create_order("u1", "tok", "mc1", _req(w))
    assert m.rejectReason == "MARKET_CLOSED"
    lim, _ = await oms.create_order("u1", "tok", "mc2", _req(w, orderType=OrderType.LIMIT, limitPrice="90", quantity="1"))
    assert lim.status.value == "ACCEPTED"
    assert await match_once(oms) == 0
    fake.phase = SessionPhase.PREOPEN
    m2, _ = await oms.create_order("u1", "tok", "mc3", _req(w))
    assert m2.rejectReason == "MARKET_PREOPEN"


@pytest.mark.asyncio
async def test_calendar_down_failsafe():
    db, fake = DB(), Fake(calendar_ok=False)
    oms = Oms(db, fake)
    w, _ = await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.PAPER))
    m, _ = await oms.create_order("u1", "tok", "cd1", _req(w))
    assert m.rejectReason == "MARKET_CLOSED"
    lim, _ = await oms.create_order("u1", "tok", "cd2", _req(w, orderType=OrderType.LIMIT, limitPrice="90", quantity="1"))
    assert lim.status.value == "ACCEPTED"


@pytest.mark.asyncio
async def test_amo_and_product_mode():
    db, fake = DB(), Fake()
    oms = Oms(db, fake)
    w, _ = await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.PAPER))
    a, _ = await oms.create_order("u1", "tok", "am1", _req(w, amo=True))
    assert a.rejectReason == "AMO_NOT_SUPPORTED"
    p, _ = await oms.create_order("u1", "tok", "am2", _req(w, productMode="MIS"))
    assert p.rejectReason == "PRODUCT_NOT_SUPPORTED"


@pytest.mark.asyncio
async def test_cancel_all_and_prefs():
    db, fake = DB(), Fake()
    oms = Oms(db, fake)
    w, _ = await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.PAPER))
    await oms.create_order("u1", "tok", "ca1", _req(w, orderType=OrderType.LIMIT, limitPrice="50", quantity="1"))
    await oms.create_order("u1", "tok", "ca2", _req(w, orderType=OrderType.LIMIT, limitPrice="40", quantity="1"))
    n = await oms.cancel_all_orders("u1", w.walletId)
    assert n == 2
    assert await oms.cancel_all_orders("u1", w.walletId) == 0
    prefs = await oms.get_prefs("u1")
    assert prefs.orderTypeFavorite == "MARKET"
    prefs2 = await oms.put_prefs("u1", "LIMIT")
    assert prefs2.orderTypeFavorite == "LIMIT"
    assert (await oms.get_prefs("u1")).orderTypeFavorite == "LIMIT"


@pytest.mark.asyncio
async def test_list_orders_from_to():
    db, fake = DB(), Fake()
    oms = Oms(db, fake)
    w, _ = await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.PAPER))
    await oms.create_order("u1", "tok", "ft1", _req(w))
    now = datetime.now(timezone.utc)
    items = await oms.list_orders("u1", w.walletId, None, from_dt=now.replace(year=2000), to_dt=now.replace(year=2099))
    assert len(items) >= 1


@pytest.mark.asyncio
async def test_batch_matcher_one_ohlc_for_n_symbols():
    db, fake = DB(), Fake(ltp=Decimal("100"))
    oms = Oms(db, fake)
    w, _ = await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.PAPER))
    for i, sym in enumerate(["AAA", "BBB", "CCC"]):
        await oms.create_order(
            "u1", "tok", f"b{i}",
            _req(w, symbol=sym, orderType=OrderType.LIMIT, limitPrice="90", quantity="1"),
        )
    fake.ohlc_calls = 0
    fake.ltp_v = Decimal("80")
    await match_once(oms)
    assert fake.ohlc_calls == 1


@pytest.mark.asyncio
async def test_phase_from_calendar_payload():
    open_v = status_from_payload({"open": True, "reason": "OPEN", "sessionStart": "09:15:00", "sessionEnd": "15:30:00"})
    assert phase_from_status(open_v) == SessionPhase.OPEN
    closed = status_from_payload({"open": False, "reason": "WEEKEND"})
    assert phase_from_status(closed) == SessionPhase.CLOSED
    miss = MarketStatusView(open=False, reason="CALENDAR_UNAVAILABLE", available=False)
    assert phase_from_status(miss) == SessionPhase.CLOSED


@pytest.mark.asyncio
async def test_ltp_parses_market_quotes_map():
    from am_oms.clients import Clients
    from am_oms.core.config import Settings

    def handler(request: httpx.Request) -> httpx.Response:
        return httpx.Response(200, json={"RELIANCE": {"lastPrice": 0.0, "previousClose": 1400.5, "ohlc": {"close": 0.0}}})

    s = Settings.model_construct(market_base_url="http://m", market_ohlc_path="/v1/market-data/ohlc")
    c = Clients(s, httpx.AsyncClient(transport=httpx.MockTransport(handler)))
    assert await c.ltp("RELIANCE") == Decimal("1400.5")
    assert await c.ltp("RELIANCE", last_price_only=True) is None


@pytest.mark.asyncio
async def test_journal_down_no_wallet():
    db, fake = DB(), Fake(fail_journal=True)
    oms = Oms(db, fake)
    with pytest.raises(APIException) as e:
        await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.PAPER))
    assert e.value.status_code == 502 and e.value.error_code == "JOURNAL_UNAVAILABLE"
    assert not db.wallets.rows
