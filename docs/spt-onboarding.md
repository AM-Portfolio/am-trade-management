# SPT registration (load testing)

Each service declares a **minimal** `spt.yaml` next to `observability.yaml`.
SPT discovers APIs from the live OpenAPI document and owns auth.

## Java / Spring — expose OpenAPI (required)

Use **springdoc-openapi** (already on gateway / mcp / analysis).

Standard path (match SPT default for `runtime: java`):

```yaml
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
```

Keep docs **auth-protected** (same JWT as APIs). SPT fetches with platform identity.

Spec quality (enums, examples, `operationId`): see [`openapi-spec-guidelines.md`](openapi-spec-guidelines.md).
am-analysis gap audit: [`openapi-gap-am-analysis.md`](openapi-gap-am-analysis.md).

| Service | Live today | After next deploy |
|---------|------------|-------------------|
| am-analysis | `/v3/api-docs` | same |
| am-gateway | `/api-docs` (spt.yaml points here until redeploy) | `/v3/api-docs` |
| am-mcp-server | config → `/v3/api-docs` | same |

## File: `services/<name>/spt.yaml`

```yaml
apiVersion: am.spt/v1
kind: ServiceLoadTest
service: am-analysis
label: am-analysis
enabled: true
runtime: java                 # java | python
owners: [core-services]
createdBy: core-services
updatedBy: core-services
createdAt: "2026-07-22"
updatedAt: "2026-07-22"
source:
  repo: am-core-services
  path: services/am-analysis/spt.yaml
traces:
  - { name: configmap, ref: spt-catalog-am-analysis }
  - { name: onboarding, ref: docs/spt-onboarding.md }
targets:
  dev: "https://am-dev.asrax.in/analysis"
  preprod: "https://am-preprod.asrax.in/analysis"
  prod: "https://am.asrax.in/analysis"
openapi:
  path: /v3/api-docs          # java; python: /openapi.json
```

### Rules

- Pass **three public HTTPS bases** in `targets` (`dev` / `preprod` / `prod`) — SPT picks `targets[environment]`.
  Do **not** duplicate cluster DNS + `public_*` keys; one URL per env is enough.
- Set `runtime: java` or `python`.
- Prefer `owners`, `createdBy`/`updatedBy`, `source.repo`/`path`, and `traces` for Specs **Traceability**.
- **Do not** put auth/tokens here — SPT uses platform identity login.
- **Do not** hand-maintain `apis: []` — SPT loads OpenAPI from `{target}{openapi.path}`.

## Publish to SPT

```bash
python scripts/publish-spt-catalogs.py
```

Creates ConfigMaps `spt-catalog-<service>` in namespace `load-testing`.

Validate:

```bash
python ../am-agents/poc/spt/scripts/validate-spt-yaml.py services
```

## Agent / MCP execute

See `am-agents/poc/spt/docs/agent-execute.md` and `docs/store-cutover.md`.
Profiles: `GET /api/profiles`. Control plane MCP: `/mcp`.
