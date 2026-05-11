# Database Setup

User Data Store runs on PostgreSQL, Oracle, and MS SQL Server databases.

This guide shows how to set up the database for the first time with PostgreSQL, the steps are similar for Oracle and MS SQL Server.

<!-- begin box info -->
User Data Store automatically keeps the database schema up-to-date using [Liquibase](https://www.liquibase.org/). Whenever you install the application or update it to a new version, the application updates the DB schema on startup. **Therefore, you only need to follow this documentation when you are installing and setting up the database from scratch**.
<!-- end -->

## Create Database

Create a new database `user_data_store` on your PostgreSQL server. You can use the following SQL command:

```sql
CREATE DATABASE user_data_store;
```

## Create User

Continue by creating the `user_data_store` user in the database and by setting the user a strong password.

<!-- begin box info -->
**Tip:** You can generate a strong password locally on your computer using `openssl rand -base64 12`.
<!-- end -->

```sql
CREATE USER user_data_store;
ALTER USER user_data_store WITH PASSWORD '$PASSWORD$';
```

## Grant Privileges

Grant privileges to the user on the database (connect as a superuser to the default `postgres` database):

```sql
GRANT ALL PRIVILEGES ON DATABASE user_data_store TO user_data_store;
```

Then connect to the `user_data_store` database and grant schema privileges. Make sure to run this script connected to database `user_data_store`.

```sql
GRANT ALL ON SCHEMA public TO user_data_store;
```

<!-- begin box info -->
PostgreSQL 15 and later no longer grant `CREATE` on the `public` schema to all users by default. The schema grant must be executed while connected to the `user_data_store` database — it is a separate step from the database-level grant above.
<!-- end -->

You can assign more granular privileges instead of using `ALL PRIVILEGES`, if required for security reasons.

## Install Required Extensions

User Data Store requires the `uuid-ossp` extension to generate UUIDs. Install it in your database:

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

<!-- begin box warning -->
**Azure Database for PostgreSQL:** The `uuid-ossp` extension is not enabled by default and must be explicitly allowed. In the Azure Portal, navigate to your PostgreSQL server → **Server parameters** → search for `azure.extensions` → add `UUID-OSSP` to the allowed list. Then create the extension in the database as shown above.

Without this step, the application will fail to start during liquibase migration.
<!-- end -->

## Run Liquibase

User Data Store uses [Liquibase](https://www.liquibase.org/) to manage the database schema. The schema is created and kept up-to-date automatically when the application starts.

You can also run Liquibase manually using the [Liquibase CLI](https://docs.liquibase.com/tools-integrations/cli/home.html). The main changelog file is located at `docs/db/changelog/db.changelog-master.xml` in the User Data Store distribution:

```shell
liquibase \
  --url=jdbc:postgresql://localhost:5432/user_data_store \
  --username=user_data_store \
  --password=$PASSWORD$ \
  --changeLogFile=docs/db/changelog/db.changelog-master.xml \
  update
```

## Read Next

- [Deploying User Data Store](./Deploying-User-Data-Store.md)
