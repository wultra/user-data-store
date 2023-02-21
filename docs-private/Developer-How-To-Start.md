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
