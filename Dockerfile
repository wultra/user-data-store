# Docker image of the powerauth cloud application
FROM ibm-semeru-runtimes:open-17.0.5_8-jre

LABEL maintainer="Lubos Racansky <lubos.racansky@wultra.com>"

# Prepare environment variables
ENV JAVA_HOME=/opt/java/openjdk \
    LB_HOME=/usr/local/liquibase \
    LB_VERSION=4.20.0 \
    PKG_RELEASE=1~jammy \
    TZ=UTC

ENV PATH=$PATH:$LB_HOME

# Upgrade OS and dependencies
RUN apt-get -y update  \
    && apt-get -y upgrade \
    && apt-get -y install bash wget \
# Install Liquibase, inspired by https://github.com/mobtitude/liquibase/blob/master/Dockerfile
    && set -x \
    && wget -q -O /tmp/liquibase.tar.gz "https://github.com/liquibase/liquibase/releases/download/v$LB_VERSION/liquibase-$LB_VERSION.tar.gz" \
    && [ "cd1b5004d1d9c19f367eb973443204e85938e25e39a1ccf3860046d720874626  /tmp/liquibase.tar.gz" = "$(sha256sum /tmp/liquibase.tar.gz)" ] \
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
COPY docs/db/changelog $LB_HOME/db/changelog

# Deploy and run applications
COPY target/user-data-store.war user-data-store.war

# Docker configuration
EXPOSE 8080
STOPSIGNAL SIGQUIT

# Add PowerAuth User
USER powerauth

# Define entry point with mandatory commands (nginx)
COPY deploy/docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]
