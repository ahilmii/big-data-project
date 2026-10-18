#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ -f .env ]]; then
  set -a
  source <(tr -d '\r' < .env)
  set +a
fi
: "${DB_URL:?DB_URL must be set in .env or the environment}"
: "${DB_USERNAME:?DB_USERNAME must be set in .env or the environment}"
: "${DB_PASSWORD:?DB_PASSWORD must be set in .env or the environment}"
exec ./gradlew bootRun "$@"
