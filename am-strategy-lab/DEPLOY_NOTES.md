# am-strategy-lab — away-mode deploy notes

## Done locally (API DoD)

- FillEngine + APIs implemented on `feature/strategy-lab-mvp`
- `pytest`: 5 passed (fill + memory API)
- Local server `:8090` with `AM_STRATEGY_LAB_MEMORY_DB=true`:
  - `GET /health` → ok
  - `POST /v1/strategies` ZONE_SCALE → ok
  - `POST .../backtests` + poll → `DONE`
  - `GET .../report` → summary + calendar + trades
  - unsupported `ORB` → **501**
- Postman cloud: collection **AM Strategy Lab** created in workspace Admin LNU (`3526384-9ca6dc76-8824-43c3-8213-00c28f9e1fba`)
- am-specs: catalog + `services/am-strategy-lab/*` + `postman/am-strategy-lab.postman_collection.json` (branch `feature/accounts-hub-openapi`)
- am-gitops: catalog row added for `am-strategy-lab` (local dirty branch)
- OMS unit tests (venv): **14 passed**

## Blocked for Contabo/dev URL

1. GitHub Actions **AM Strategy Lab Publish** runs are stuck in **waiting** (approval / concurrency) — image not in GHCR yet.
2. Until image + Argo Application exist, `https://am-dev.asrax.in/strategy-lab` will not respond.
3. Public `https://am-dev.asrax.in/oms/health` currently returns Flutter HTML (ingress/strip-prefix), so remote OMS smoke via browser URL is inconclusive; use MCP/`/v1/orders` paths once routing confirmed.
4. Vault mongo path `apps/data/dev/runtime/am-strategy-lab/mongodb` not provisioned — `values.dev.yaml` uses `AM_STRATEGY_LAB_MEMORY_DB=true` for first boot.

## Your approve steps when back

1. Approve/run workflow `am-strategy-lab-publish.yml` on `feature/strategy-lab-mvp` (or merge to `develop`).
2. Merge/PR trade-management → develop.
3. Commit/push am-gitops catalog + add `dev/apps/am-strategy-lab.yaml` Application (mirror `am-oms-dev`).
4. `am deploy --env dev` / `am gitops sync am-strategy-lab --env dev`.
5. Point Postman `baseUrl` to `https://am-dev.asrax.in/strategy-lab` and re-run collection.
