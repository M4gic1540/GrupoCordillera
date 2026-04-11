#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
GATEWAY_DIR="$ROOT_DIR/gateway"

echo "[INFO] Running Spring Gateway test suite (unit + integration + e2e)."

cd "$GATEWAY_DIR"

./mvnw test -Dtest="GatewayTestCasesSuiteTest,GatewaySecurityTest,GatewayIntegrationTest,GatewayPerformanceTest,GatewayE2ETest"

echo "[INFO] Running compose E2E checks against real microservices via gateway."

cleanup() {
  cd "$ROOT_DIR"
  docker compose down -v >/dev/null 2>&1 || true
}

trap cleanup EXIT

cd "$ROOT_DIR"
docker compose up -d --build postgres postgres-events postgres-kpi authservice-1 authservice-2 data-ingestion-service-1 data-ingestion-service-2 kpi-engine gateway

for _ in {1..60}; do
  status="$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health || true)"
  if [[ "$status" == "200" ]]; then
    break
  fi
  sleep 2
done

if [[ "${status:-000}" != "200" ]]; then
  echo "[ERROR] Gateway did not become healthy on http://localhost:8080/actuator/health"
  exit 1
fi

register_response="$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"gateway.e2e@test.com","password":"Password123"}')"

access_token="$(echo "$register_response" | sed -n 's/.*"accessToken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"

if [[ -z "$access_token" ]]; then
  echo "[ERROR] Could not obtain access token from /api/auth/register response"
  echo "$register_response"
  exit 1
fi

ingestion_status="$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/ingestion/health \
  -H "Authorization: Bearer $access_token")"

if [[ "$ingestion_status" != "200" ]]; then
  echo "[ERROR] /api/ingestion/health returned HTTP $ingestion_status"
  exit 1
fi

kpi_status="$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/kpi/health \
  -H "Authorization: Bearer $access_token")"

if [[ "$kpi_status" != "200" ]]; then
  echo "[ERROR] /api/kpi/health returned HTTP $kpi_status"
  exit 1
fi

docs_status="$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/kpi/docs)"

if [[ "$docs_status" != "200" ]]; then
  echo "[ERROR] /kpi/docs returned HTTP $docs_status"
  exit 1
fi

echo "[OK] Gateway unit/integration/E2E tests passed and compose E2E checks succeeded."
