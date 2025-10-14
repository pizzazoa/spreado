#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

docker compose \
  -p spreado-be \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.local.yml \
  down
