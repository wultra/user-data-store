# Migration from 1.5.0 to 1.6.0

This guide contains instructions for migration from User Data Store version `1.5.x` to version `1.6.0`.


## Database Changes

For convenience, you can use liquibase for your database migration.

The main Liquibase script is located in path [./docs/db/changelog/db.changelog-master.xml](db/changelog/db.changelog-master.xml).

For manual changes use SQL scripts:

- [PostgreSQL script](./sql/postgresql/migration_1.5.0-1.6.0.sql)
- [Oracle script](./sql/oracle/migration_1.5.0-1.6.0.sql)
- [MSSQL script](./sql/mssql/migration_1.5.0-1.6.0.sql)

### `audit_log` Table

Added a new indexed column `subject_id` holding an identifier linking the audit record to an entity it is related to (e.g. user ID for user-related audit records).

<!-- begin box warning -->
The auditing tables may be already updated in your database schema if the database schema is not separated for different PowerAuth applications. In case the column `audit_log.subject_id` and its index `audit_log_subject_id_idx` are already present, you can safely skip this migration step.
<!-- end -->


## REST API Changes

### GET `/documents` Filtering

The `GET /documents` endpoint now supports two additional optional query parameters used to filter the returned documents:

- `documentType` - return only documents of the given document type.
- `attributes` - return only the listed attribute keys in the `attributes` map of each document. The parameter can be repeated or provided as a comma-separated list (e.g. `attributes=firstName,lastName`).

Both parameters are optional and can be combined. When neither is provided, the endpoint behaves as in previous versions and returns all documents with all attributes.
