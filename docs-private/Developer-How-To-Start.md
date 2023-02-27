# Developer - How to Start Guide


## Standalone Run

- Enable maven profile `standalone`
- Use IntelliJ Idea run configuration at `../.run/UserDataStoreApplication.run.xml`
- Open [http://localhost:8091/user-data-store/actuator/health](http://localhost:8091/user-data-store/actuator/health) and you should get `{"status":"UP"}`


## Database

Database changes are driven by Liquibase.

This is an example how to manually check the Liquibase status.
Important and fixed parameter is `changeloge-file`.
Others (like URL, username, password) depend on your environment.
Mind that the working directory must be `src/main/resources` to be consistent with Spring Boot naming (file names are part of the checksum).

```shell
cd src/main/resources
liquibase --changelog-file=db/changelog/db.changelog-master.xml --url=jdbc:postgresql://localhost:5432/powerauth --username=powerauth --hub-mode=off status
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


### Prepare environment variables

* Copy `docker/env.list.tmp` to `./env.list`
* Set your database credential values for the `$USERNAME$` and `$PASSWORD$` to the values from the previous step.


### Build the docker image

```shell
docker build . -t user-data-store:0.1.0-SNAPSHOT
```


### Run the docker image

```shell
docker run -p 80:8080 user-data-store:0.1.0-SNAPSHOT 
```
