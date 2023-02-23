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
Mind that the working directory MUST be `src/main/resources` to be consistent with Spring Boot naming (file names are part of the checksum).

```shell
cd src/main/resources
liquibase --changelog-file=db/changelog/db.changelog-master.xml --url=jdbc:postgresql://localhost:5432/powerauth --username=powerauth --hub-mode=off status
```


### Users

To create a testing user, execute the following sql.
To get the salted hash, call `new StandardPasswordEncoder().encode("my password");` 

```sql
INSERT INTO ud_users (username, password, enabled) VALUES ('user', '{sha256}48c13b0404540c2f2b0952ab4580f82213605e0aed7edf8979addeddfd9a3e70185688cdcdb9b3dc', true);

INSERT INTO ud_authorities (username, authority) VALUES ('user', 'ROLE_READ');
INSERT INTO ud_authorities (username, authority) VALUES ('user', 'ROLE_WRITE');
```
