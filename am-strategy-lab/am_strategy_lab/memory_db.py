"""Minimal async in-memory collections for local/tests without Mongo."""

from __future__ import annotations

import copy
from typing import Any


class _Cursor:
    def __init__(self, rows: list[dict[str, Any]]):
        self._rows = rows

    def __aiter__(self):
        self._i = 0
        return self

    async def __anext__(self):
        if self._i >= len(self._rows):
            raise StopAsyncIteration
        row = self._rows[self._i]
        self._i += 1
        return row


class MemoryCollection:
    def __init__(self):
        self._rows: list[dict[str, Any]] = []

    async def create_index(self, *args, **kwargs):
        return None

    async def insert_one(self, doc: dict[str, Any]):
        self._rows.append(copy.deepcopy(doc))
        return type("R", (), {"inserted_id": id(doc)})()

    async def find_one(self, filt: dict[str, Any], projection: dict[str, Any] | None = None):
        for row in self._rows:
            if all(row.get(k) == v for k, v in filt.items()):
                out = copy.deepcopy(row)
                if projection:
                    for k, flag in projection.items():
                        if flag == 0 and k in out:
                            del out[k]
                return out
        return None

    async def update_one(self, filt: dict[str, Any], update: dict[str, Any]):
        for i, row in enumerate(self._rows):
            if all(row.get(k) == v for k, v in filt.items()):
                if "$set" in update:
                    row.update(update["$set"])
                self._rows[i] = row
                return type("R", (), {"modified_count": 1})()
        return type("R", (), {"modified_count": 0})()

    def find(self, filt: dict[str, Any] | None = None, projection: dict[str, Any] | None = None):
        filt = filt or {}
        rows = []
        for row in self._rows:
            if all(row.get(k) == v for k, v in filt.items()):
                out = copy.deepcopy(row)
                if projection:
                    for k, flag in projection.items():
                        if flag == 0 and k in out:
                            del out[k]
                rows.append(out)
        return _Cursor(rows)


class MemoryDatabase:
    def __init__(self):
        self.strategies = MemoryCollection()
        self.backtests = MemoryCollection()
