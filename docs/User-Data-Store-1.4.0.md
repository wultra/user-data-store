# Migration from 1.3.0 to 1.4.0

This guide contains instructions for migration from User Data Store version `1.3.x` to version `1.4.0`.


## Database Changes

For convenience, you can use liquibase for your database migration.

The main Liquibase script is located in path [db/changelog/db.changelog-master.xml](db/changelog/db.changelog-master.xml).

For manual changes use SQL scripts:

- [PostgreSQL script](./sql/postgresql/migration_1.3.0-1.4.0.sql)
- [Oracle script](./sql/oracle/migration_1.3.0-1.4.0.sql)
- [MSSQL script](./sql/mssql/migration_1.3.0-1.4.0.sql)


### Shedlock

Added table `shedlock` to prevent execution of the same task from another node.
