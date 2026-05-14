# Migration from 1.2.0 to 1.3.0

This guide contains instructions for migration from User Data Store version `1.2.x` to version `1.3.0`.

## Database Changes

For convenience, you can use liquibase for your database migration.

The main Liquibase script is located in path [./docs/db/changelog/db.changelog-master.xml](db/changelog/db.changelog-master.xml).

Note that data from table `uds_user_claims` is migrated into table `uds_document` during this upgrade.

For manual changes use SQL scripts:

- [PostgreSQL script](./sql/postgresql/migration_1.2.0-1.3.0.sql)
- [Oracle script](./sql/oracle/migration_1.2.0-1.3.0.sql)
- [MSSQL script](./sql/mssql/migration_1.2.0-1.3.0.sql)

### PostgreSQL Migration Specifics for Azure

The migration script uses `uuid_generate_v4()` which requires the `uuid-ossp` extension. In Azure Database for PostgreSQL, this extension may not be allow-listed by default.

If you use Azure Database for PostgreSQL, add `uuid-ossp` to the server's `azure.extensions` setting before running the migration, and then enable the extension in the database.

For the detailed Azure setup steps, see [Database Setup](./Database-Setup.md).
