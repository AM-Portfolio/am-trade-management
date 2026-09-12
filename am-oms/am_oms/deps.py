from am_oms.clients import Clients
from am_oms.core.database import get_database
from am_oms.services import Oms

_clients: Clients | None = None


def get_clients() -> Clients:
    global _clients
    if _clients is None:
        _clients = Clients()
    return _clients


def get_oms() -> Oms:
    return Oms(get_database(), get_clients())
