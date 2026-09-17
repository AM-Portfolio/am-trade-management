# Grafana Metrics Fix — am-trade-management

**Date:** 2026-09-17
**Status:** Implemented

## Problem

The Grafana Technical dashboard for `am-trade-management` was missing p90/p95 latency
panels and SLO bucket panels that are visible in `am-portfolio`.

## Root Cause

Spring Boot / Micrometer does **not** emit `http_server_requests_seconds_bucket` metrics
unless `management.metrics.distribution.percentiles-histogram.http.server.requests: true`
is explicitly set. Without these histogram buckets, Prometheus cannot compute
`histogram_quantile` — so all p90/p95/SLO panels in Grafana show "No data".

Additional gaps:
- `management.endpoint.prometheus.enabled: true` was missing (defensive).
- `observability.yaml` had `domain: []` — no domain signals registered.
- `observability.yaml` had `rows.collapse: [row_jvm, row_k8s]` — panels collapsed.
- `am-trade-management` was not in `am-observability/services/registry.yaml`.

## Changes Made

### am-trade-app/src/main/resources/application.yml

Added `management.metrics.distribution.percentiles-histogram` and `slo` blocks,
plus explicit `management.endpoint.prometheus.enabled: true`.

### observability.yaml

Added domain signals (`trade_processed_total`, `trade_processing_duration_seconds`)
from `TradeBusinessMetrics`. Removed `dashboard.technical.rows.collapse`.

### am-observability/services/registry.yaml

Registered `am-trade-management` with `enabled: true`.

## Verification

- `mvn -pl am-trade-app -am package -DskipTests` — build passes.
- `curl http://localhost:8080/actuator/prometheus | grep http_server_requests_seconds_bucket`
  should return bucket lines after any HTTP request.
- After cluster deploy: Grafana -> AM / Technical -> Technical / am-trade-management
  — HTTP p90/p95 and SLO panels populate.
