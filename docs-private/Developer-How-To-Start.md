# Developer - How to Start Guide


## Standalone Run

- Use IntelliJ Idea run configuration at `../.run/UserDataStoreApplication.run.xml`
- Open [http://localhost:8091/user-data-store/actuator/health](http://localhost:8091/user-data-store/actuator/health) and you should get `{"status":"UP"}`


## Database

Database changes are driven by Liquibase.

This is an example how to manually check the Liquibase status.
Important and fixed parameter is `changelog-file`.
Others (like URL, username, password) depend on your environment.

```shell
liquibase --changelog-file=./docs/db/changelog/db.changelog-master.xml --url=jdbc:postgresql://localhost:5432/powerauth --username=powerauth status
```

### PostgreSQL

```shell
liquibase --changeLogFile=./docs/db/changelog/changesets/user-data-store/db.changelog-module.xml --output-file=./docs/sql/postgresql/generated-postgresql-script.sql updateSQL --url=offline:postgresql
```


### Oracle

```shell
liquibase --changeLogFile=./docs/db/changelog/changesets/user-data-store/db.changelog-module.xml --output-file=./docs/sql/oracle/generated-oracle-script.sql updateSQL --url=offline:oracle
```


### MS SQL

```shell
liquibase --changeLogFile=./docs/db/changelog/changesets/user-data-store/db.changelog-module.xml --output-file=./docs/sql/mssql/generated-mssql-script.sql updateSQL --url=offline:mssql
```


### Users

To create a testing user, execute the following sql.
To get the hash, call `new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-256").encode("password");` 

```sql
INSERT INTO uds_users (username, password, enabled) VALUES ('user', '{O35pWc2gYBen1x6fdP8jxc4knxkOdwwhW4nobRrZ/m4=}ce0f5a243469ffe3371432b2c6970d33ef0403fc3a839b1d19c19d395ff53695', true);

INSERT INTO uds_authorities (username, authority) VALUES ('user', 'ROLE_READ');
INSERT INTO uds_authorities (username, authority) VALUES ('user', 'ROLE_WRITE');
```


## Docker


### Build War

```shell
mvn clean package
```


### Build the docker image

```shell
docker build . -t user-data-store:0.3.0
```


### Prepare environment variables

* Copy `deploy/env.list.tmp` to `./env.list` and edit the values to use it via `docker run --env-file env.list IMAGE`
* Or set environment variables via `docker run -e USER_DATA_STORE_DATASOURCE_USERNAME='powerauth' IMAGE`


### Run the docker image

```shell
docker run -p 80:8080 -e USER_DATA_STORE_DATASOURCE_USERNAME='powerauth' -e USER_DATA_STORE_DATASOURCE_PASSWORD='' user-data-store:0.3.0 
```
