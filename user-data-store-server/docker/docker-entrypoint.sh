#!/usr/bin/env bash
set -euo pipefail

if [ "${LQ_ENABLED}" = "true" ]; then
  liquibase --headless=true --log-level=INFO --changeLogFile=$LB_HOME/db/changelog/db.changelog-master.xml \
    --username="${USER_DATA_STORE_DATASOURCE_USERNAME:-}" \
    --password="${USER_DATA_STORE_DATASOURCE_PASSWORD:-}" \
    --url="${USER_DATA_STORE_DATASOURCE_URL}" \
    update
fi

java ${JAVA_OPTS:-} -jar user-data-store.war
