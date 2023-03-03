# Docker image of the powerauth cloud application
FROM ibm-semeru-runtimes:open-17.0.5_8-jre

LABEL maintainer="Lubos Racansky <lubos.racansky@wultra.com>"

# Prepare environment variables
ENV JAVA_HOME=/opt/java/openjdk \
    PKG_RELEASE=1~jammy \
    TZ=UTC

# Init
RUN apt-get -y update  \
    && apt-get -y upgrade \
    && apt-get -y install bash curl wget

# Deploy and run applications

COPY target/user-data-store.war user-data-store.war

# Docker configuration
EXPOSE 8080
STOPSIGNAL SIGQUIT

# Add PowerAuth User
RUN groupadd -r powerauth \
    && useradd -r -g powerauth -s /sbin/nologin powerauth
USER powerauth

# Define entry point with mandatory commands (nginx)
COPY deploy/docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]
