#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
NGINX_CONF="$ROOT_DIR/gateway/nginx.conf"
REPORT_DIR="$ROOT_DIR/gateway/test-results"
REPORT_FILE="$REPORT_DIR/gateway-tests.xml"

mkdir -p "$REPORT_DIR"

TOTAL=0
FAILURES=0
TEST_NAMES=()
TEST_STATUSES=()
TEST_MESSAGES=()

record_test() {
  local name="$1"
  local status="$2"
  local message="${3:-}"

  TOTAL=$((TOTAL + 1))
  TEST_NAMES+=("$name")
  TEST_STATUSES+=("$status")
  TEST_MESSAGES+=("$message")

  if [[ "$status" != "passed" ]]; then
    FAILURES=$((FAILURES + 1))
    echo "[FAIL] $name -> $message"
  else
    echo "[PASS] $name"
  fi
}

write_junit_report() {
  {
    echo '<?xml version="1.0" encoding="UTF-8"?>'
    echo "<testsuite name=\"gateway-nginx-suite\" tests=\"$TOTAL\" failures=\"$FAILURES\">"

    for i in "${!TEST_NAMES[@]}"; do
      local name="${TEST_NAMES[$i]}"
      local status="${TEST_STATUSES[$i]}"
      local message="${TEST_MESSAGES[$i]}"

      if [[ "$status" == "passed" ]]; then
        echo "  <testcase classname=\"gateway.nginx\" name=\"$name\"/>"
      else
        echo "  <testcase classname=\"gateway.nginx\" name=\"$name\">"
        echo "    <failure message=\"$message\"/>"
        echo '  </testcase>'
      fi
    done

    echo '</testsuite>'
  } > "$REPORT_FILE"
}

expect_contains() {
  local name="$1"
  local output="$2"
  local expected="$3"

  if [[ "$output" == *"$expected"* ]]; then
    record_test "$name" "passed"
  else
    record_test "$name" "failed" "Expected response to contain '$expected'"
  fi
}

expect_status() {
  local name="$1"
  local status="$2"
  local expected="$3"

  if [[ "$status" == "$expected" ]]; then
    record_test "$name" "passed"
  else
    record_test "$name" "failed" "Expected HTTP $expected, got $status"
  fi
}

safe_curl_body() {
  local url="$1"
  curl -s "$url" || true
}

safe_curl_status() {
  local url="$1"
  shift
  curl -s -o /dev/null -w "%{http_code}" "$@" "$url" || echo "000"
}

cleanup() {
  docker rm -f gw-nginx-test gw-auth-1 gw-auth-2 gw-ingestion-1 gw-ingestion-2 gw-kpi >/dev/null 2>&1 || true
  docker network rm gw-test-net >/dev/null 2>&1 || true
}

trap cleanup EXIT
cleanup

if [[ ! -f "$NGINX_CONF" ]]; then
  record_test "nginx.conf exists" "failed" "File not found: $NGINX_CONF"
  write_junit_report
  exit 1
fi

if grep -Eq 'limit_req_zone\s+\$binary_remote_addr\s+zone=api_limit:10m\s+rate=20r/s;' "$NGINX_CONF"; then
  record_test "Rate limit configured" "passed"
else
  record_test "Rate limit configured" "failed" "Missing limit_req_zone api_limit"
fi

if grep -Eq 'auth_request\s+/api/auth/validate;' "$NGINX_CONF"; then
  record_test "auth_request configured" "passed"
else
  record_test "auth_request configured" "failed" "Missing auth_request /api/auth/validate"
fi

if grep -Eq 'add_header\s+X-Correlation-ID\s+\$correlation_id\s+always;' "$NGINX_CONF"; then
  record_test "Correlation header configured" "passed"
else
  record_test "Correlation header configured" "failed" "Missing X-Correlation-ID header"
fi

if grep -Eq '~\*\(sqlmap\|nikto\|nmap\|acunetix\|masscan\|dirbuster\)' "$NGINX_CONF"; then
  record_test "WAF user-agent signatures configured" "passed"
else
  record_test "WAF user-agent signatures configured" "failed" "Missing WAF user-agent map rules"
fi

docker network create gw-test-net >/dev/null

docker run -d --name gw-auth-1 --network gw-test-net --network-alias authservice-1 hashicorp/http-echo:1.0.0 -listen=:8080 -text='AUTH_OK' >/dev/null
docker run -d --name gw-auth-2 --network gw-test-net --network-alias authservice-2 hashicorp/http-echo:1.0.0 -listen=:8080 -text='AUTH_OK_2' >/dev/null
docker run -d --name gw-ingestion-1 --network gw-test-net --network-alias data-ingestion-service-1 hashicorp/http-echo:1.0.0 -listen=:8081 -text='INGESTION_OK' >/dev/null
docker run -d --name gw-ingestion-2 --network gw-test-net --network-alias data-ingestion-service-2 hashicorp/http-echo:1.0.0 -listen=:8081 -text='INGESTION_OK_2' >/dev/null
docker run -d --name gw-kpi --network gw-test-net --network-alias kpi-engine hashicorp/http-echo:1.0.0 -listen=:8082 -text='KPI_OK' >/dev/null

docker run -d --name gw-nginx-test --network gw-test-net -p 18080:8080 nginx:1.27-alpine >/dev/null
docker cp "$NGINX_CONF" gw-nginx-test:/etc/nginx/nginx.conf >/dev/null
if docker exec gw-nginx-test nginx -t >/dev/null 2>&1; then
  record_test "Nginx syntax is valid" "passed"
else
  lint_error="$(docker exec gw-nginx-test nginx -t 2>&1 | tr '\n' ' ' | sed 's/"/\x27/g')"
  record_test "Nginx syntax is valid" "failed" "nginx -t failed: $lint_error"
fi
docker exec gw-nginx-test nginx -s reload >/dev/null

for _ in {1..30}; do
  if curl -sSf "http://localhost:18080/" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

if ! curl -s "http://localhost:18080/" >/dev/null 2>&1; then
  record_test "Gateway reachable on localhost:18080" "failed" "Gateway not reachable after startup wait"
  write_junit_report
  exit 1
fi

auth_body="$(safe_curl_body "http://localhost:18080/api/auth/ping")"
expect_contains "Route /api/auth/* reaches auth upstream" "$auth_body" "AUTH_OK"

ingestion_body="$(safe_curl_body "http://localhost:18080/api/ingestion/ping")"
expect_contains "Route /api/ingestion/* reaches ingestion upstream" "$ingestion_body" "INGESTION_OK"

kpi_body="$(safe_curl_body "http://localhost:18080/kpi/docs")"
expect_contains "Route /kpi/docs rewrites and proxies to kpi upstream" "$kpi_body" "KPI_OK"

status_waf_query="$(safe_curl_status "http://localhost:18080/api/ingestion/ping?x=union+select")"
expect_status "WAF blocks SQLi-like query strings" "$status_waf_query" "403"

status_waf_ua="$(safe_curl_status "http://localhost:18080/api/kpi/ping" -H "User-Agent: sqlmap")"
expect_status "WAF blocks malicious user-agent" "$status_waf_ua" "403"

write_junit_report

echo "Gateway test suite: $TOTAL tests, $FAILURES failures"

if [[ "$FAILURES" -gt 0 ]]; then
  exit 1
fi
