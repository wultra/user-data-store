FROM ghcr.io/wultra/wultra-base-images:jre-semeru-25_3

# Prepare environment variables
ENV LOGBACK_CONF=/opt/logback/conf \
    APP_PATH="/app/user-data-store.war" \
    CONTEXT_PATH="/user-data-store"

# Deploy and run applications
COPY ./user-data-store-server/target/user-data-store-server*.war "${APP_PATH}"

COPY ./user-data-store-server/docker/deploy/conf/logback/* $LOGBACK_CONF/
