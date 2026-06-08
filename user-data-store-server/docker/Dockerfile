# Docker image of the powerauth cloud application
FROM ibm-semeru-runtimes:open-jdk-25.0.3.0-jre-noble

LABEL maintainer="Wultra <hello@wultra.com>"

# Prepare environment variables
ENV JAVA_HOME=/opt/java/openjdk \
    LB_HOME=/usr/local/liquibase \
    LB_VERSION=4.30.0 \
    PKG_RELEASE=1~jammy \
    LOGBACK_CONF=/opt/logback/conf \
    TZ=UTC \
    APP_PATH="/app/user-data-store.war" \
    LQ_ENABLED=true

ENV PATH=$PATH:$LB_HOME

# Upgrade OS and dependencies
RUN apt-get -y update  \
    && apt-get -y upgrade \
    && apt-get -y install bash wget \
# Install Liquibase, inspired by https://github.com/mobtitude/liquibase/blob/master/Dockerfile
    && set -x \
    && wget -q -O /tmp/liquibase.tar.gz "https://github.com/liquibase/liquibase/releases/download/v$LB_VERSION/liquibase-$LB_VERSION.tar.gz" \
    && echo "184ffd609518091da42d6cd75e883b4f6ff1763cce8883e95fc99f7f05ca262d  /tmp/liquibase.tar.gz" | sha256sum -c - \
    && mkdir -p "$LB_HOME" \
    && tar -xzf /tmp/liquibase.tar.gz -C "$LB_HOME" \
    && rm -rf "$LB_HOME/sdk" "$LB_HOME/examples" \
# Uninstall packages which are no longer needed and clean apt caches
    && apt-get -y remove wget gettext-base \
    && apt-get -y purge --auto-remove \
    && rm -rf /tmp/* /var/cache/apt/* \
# Remove default Liquibase data
    && rm -rf $LB_HOME/data \
# Add PowerAuth user
    && groupadd -r powerauth && useradd -r -g powerauth -s /sbin/nologin powerauth

# Copy new Liquibase data
COPY docs/db/changelog/changesets/user-data-store $LB_HOME/db/changelog

# Deploy and run applications
COPY ./user-data-store-server/target/user-data-store-server*.war "${APP_PATH}"

# Docker configuration
EXPOSE 8080
STOPSIGNAL SIGQUIT

# Add PowerAuth User
USER powerauth

COPY ./user-data-store-server/docker/deploy/conf/logback/* $LOGBACK_CONF/

# Define entry point with mandatory commands (nginx)
COPY ./user-data-store-server/docker/docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
