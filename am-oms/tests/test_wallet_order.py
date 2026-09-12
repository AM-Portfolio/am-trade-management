from decimal import Decimal
import httpx
import pytest
from am_platform_common import APIException
from am_oms.matcher import match_once
from am_oms.schemas import InstrumentType, OrderCreateRequest, OrderSide, OrderType, Venue, WalletCreateRequest, WalletKind
from am_oms.services import Oms


class Cur:
    def __init__(self, rows):
        self.rows = rows

    async def to_list(self, n):
        return self.rows[:n]


class Col:
    def __init__(self):
        self.rows = []

    async def find_one(self, q):
        for d in self.rows:
            if all(d.get(k) == v for k, v in q.items()):
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
        return Cur([d for d in self.rows if all(d.get(k) == v for k, v in q.items())])


class DB:
    def __init__(self):
        self.wallets, self.orders, self.positions = Col(), Col(), Col()


class Fake:
    def __init__(self, ltp=Decimal("100"), fail_journal=False):
        self.ltp_v, self.fail_journal, self.events = ltp, fail_journal, []

    async def create_journal_portfolio(self, token, seed):
        if self.fail_journal:
            raise httpx.ConnectError("down")
        return "pf-1", "Paper"

    async def ltp(self, symbol):
        return None if symbol == "BAD" else self.ltp_v

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


@pytest.mark.asyncio
async def test_limit_touch_and_rest_match():
    db, fake = DB(), Fake(ltp=Decimal("100"))
    oms = Oms(db, fake)
    w, _ = await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.PAPER))
    o, _ = await oms.create_order("u1", "tok", "l1", _req(w, orderType=OrderType.LIMIT, limitPrice="100", quantity="1"))
    assert o.status.value == "FILLED"
    o2, _ = await oms.create_order("u1", "tok", "l2", _req(w, orderType=OrderType.LIMIT, limitPrice="90", quantity="1"))
    assert o2.status.value == "ACCEPTED"
    assert Decimal((await oms.get_wallet("u1", w.walletId)).reserved) == Decimal("90.00")
    fake.ltp_v = Decimal("85")
    assert await match_once(oms) >= 1
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
async def test_ltp_parses_market_quotes_map():
    from am_oms.clients import Clients
    from am_oms.core.config import Settings

    def handler(request: httpx.Request) -> httpx.Response:
        return httpx.Response(200, json={"RELIANCE": {"lastPrice": 0.0, "previousClose": 1400.5, "ohlc": {"close": 0.0}}})

    s = Settings.model_construct(market_base_url="http://m", market_ohlc_path="/v1/market-data/ohlc")
    c = Clients(s, httpx.AsyncClient(transport=httpx.MockTransport(handler)))
    assert await c.ltp("RELIANCE") == Decimal("1400.5")


@pytest.mark.asyncio
async def test_journal_down_no_wallet():
    db, fake = DB(), Fake(fail_journal=True)
    oms = Oms(db, fake)
    with pytest.raises(APIException) as e:
        await oms.create_wallet("u1", "tok", WalletCreateRequest(kind=WalletKind.PAPER))
    assert e.value.status_code == 502 and e.value.error_code == "JOURNAL_UNAVAILABLE"
    assert not db.wallets.rows
