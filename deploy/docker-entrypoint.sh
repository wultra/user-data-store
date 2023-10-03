#!/usr/bin/env sh

liquibase --headless=true --log-level=INFO --changeLogFile=$LB_HOME/db/changelog/db.changelog-master.xml \
  --username=$USER_DATA_STORE_DATASOURCE_USERNAME \
  --password=$USER_DATA_STORE_DATASOURCE_PASSWORD \
  --url=$USER_DATA_STORE_DATASOURCE_URL \
  update

java -jar user-data-store.war
