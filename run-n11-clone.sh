#!/usr/bin/env bash

set -euo pipefail

PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$PROJECT_DIR/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Error: $ENV_FILE bulunamadı." >&2
  echo "Önce .env.example dosyasını .env olarak kopyalayıp değerleri doldur." >&2
  exit 1
fi

set -a
source "$ENV_FILE"
set +a

exec "$PROJECT_DIR/mvnw" spring-boot:run
