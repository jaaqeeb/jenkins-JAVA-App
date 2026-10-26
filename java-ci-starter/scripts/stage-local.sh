#!/usr/bin/env bash
set -euo pipefail

# Local staging smoke test. This does not create cloud infrastructure.
# Requires Java 17 and Maven. Run from the repository root.
APP_PORT="${APP_PORT:-8080}"
APP_PID=""

cleanup() {
  if [[ -n "${APP_PID}" ]]; then
    kill "${APP_PID}" >/dev/null 2>&1 || true
    wait "${APP_PID}" 2>/dev/null || true
  fi
}
trap cleanup EXIT

echo "1/4 Build and test"
mvn -B clean verify

echo "2/4 Start app with greeting feature disabled (safe default)"
FEATURE_GREETING_ENABLED=false PORT="${APP_PORT}" \
  java -jar target/ci-demo-app-0.1.0-SNAPSHOT.jar &
APP_PID=$!

echo "3/4 Wait for health endpoint"
for attempt in $(seq 1 30); do
  if curl --silent --fail "http://localhost:${APP_PORT}/actuator/health" >/dev/null; then
    break
  fi
  sleep 2
done
curl --fail "http://localhost:${APP_PORT}/actuator/health"
echo
curl --fail "http://localhost:${APP_PORT}/api/features/greeting"
echo

echo "4/4 Verify disabled feature responds with HTTP 501"
status="$(curl --silent --output /tmp/ci-demo-feature-response.json --write-out '%{http_code}' \
  "http://localhost:${APP_PORT}/api/greet?name=Ada")"
cat /tmp/ci-demo-feature-response.json
echo
if [[ "${status}" != "501" ]]; then
  echo "Expected HTTP 501 while feature is disabled, got ${status}" >&2
  exit 1
fi

cleanup
APP_PID=""

echo "Start again with FEATURE_GREETING_ENABLED=true to test the enabled path:"
echo "FEATURE_GREETING_ENABLED=true java -jar target/ci-demo-app-0.1.0-SNAPSHOT.jar"
echo "Then call: curl 'http://localhost:${APP_PORT}/api/greet?name=Ada'"
