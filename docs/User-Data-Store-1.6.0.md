# Migration from 1.5.0 to 1.6.0

This guide contains instructions for migration from User Data Store version `1.5.x` to version `1.6.0`.


## Database Changes

For convenience, you can use liquibase for your database migration.

The main Liquibase script is located in path [db/changelog/db.changelog-master.xml](db/changelog/db.changelog-master.xml).

For manual changes use SQL scripts:

- [PostgreSQL script](./sql/postgresql/migration_1.5.0-1.6.0.sql)
- [Oracle script](./sql/oracle/migration_1.5.0-1.6.0.sql)
- [MSSQL script](./sql/mssql/migration_1.5.0-1.6.0.sql)

### `audit_log` Table

Added a new indexed column `subject_id` holding an identifier linking the audit record to an entity it is related to (e.g. user ID for user-related audit records).

<!-- begin box warning -->
The auditing tables may be already updated in your database schema if the database schema is not separated for different PowerAuth applications. In case the column `audit_log.subject_id` and its index `audit_log_subject_id_idx` are already present, you can safely skip this migration step.
<!-- end -->
