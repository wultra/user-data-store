#!/usr/bin/env bash
set -euo pipefail
KEEP_RUNNING=${KEEP_RUNNING:-false}
KEEP_RUNNING_PORT=${KEEP_RUNNING_PORT:-666}

liquibase --headless=true --log-level=INFO \
    --changeLogFile="changelog/db.changelog-master.xml" \
    --username="${USER_DATA_STORE_DATASOURCE_USERNAME:-}" \
    --password="${USER_DATA_STORE_DATASOURCE_PASSWORD:-}" \
    --url="${USER_DATA_STORE_DATASOURCE_URL}" \
    update

if [[ -n "${KEEP_RUNNING}" && "${KEEP_RUNNING}" == "true" ]]; then
    echo "Container kept alive via KEEP_RUNNING and KEEP_RUNNING_PORT flag"
    RESPONSE="HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: 15\r\n\r\nHello from nc!\n"

    while true; do
      echo -e "$RESPONSE" | nc -l -p "$KEEP_RUNNING_PORT"
    done
fi