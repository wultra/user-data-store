# Migration from 1.2.0 to 1.3.0

This guide contains instructions for migration from User Data Store version `1.2.x` to version `1.3.0`.

## Database Changes

For convenience, you can use liquibase for your database migration.

Note that data from table `uds_user_claims` is migrated into table `uds_document` during this upgrade.

For manual changes use SQL scripts:

- [PostgreSQL script](./sql/postgresql/migration_1.2.0-1.3.0.sql)
- [Oracle script](./sql/oracle/migration_1.2.0-1.3.0.sql)
- [MSSQL script](./sql/mssql/migration_1.2.0-1.3.0.sql)
