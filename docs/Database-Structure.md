# Database Structure

<!-- TEMPLATE database -->

User Data Store supports database changes using Liquibase.
But you may download DDL scripts for supported databases:

- [PostgreSQL - Create Database Schema](./sql/postgresql/create-schema.sql)

## Auditing

The DDL files contain an `audit_log` table definition. The table differs slightly per database. 

Only one `audit_log` table is required per PowerAuth stack in case the same schema is used for all deployed applications.

For more information about auditing library, see the [Wultra auditing library documentation](https://github.com/wultra/lime-java-core#wultra-auditing-library).

## Authentication

See [JDBC-based Spring Security Schema](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jdbc.html#servlet-authentication-jdbc-schema).
In our case, the authentication tables are prefixed by `uds_`.

## Table Documentation

This chapter explains individual tables and their columns. The column types are used from PostgreSQL dialect, other databases use types that are equivalent (mapping is usually straight-forward).

<!-- begin database table uds_user_claims -->
### User Claims Table

Stores user claims.

#### Schema

| Name                     | Type                          | Info                      | Note                                                                                                           |
|--------------------------|-------------------------------|---------------------------|----------------------------------------------------------------------------------------------------------------|
| `user_id`                | `VARCHAR(255)`                | `NOT NULL PRIMARY KEY`    | Record identifier taken over from the creator.                                                                 |
| `claims`                 | `TEXT`                        | `NOT NULL PRIMARY KEY`    | JSON with claims. Format depends on value of `encryption_mode`.                                                |
| `encryption_mode`        | `VARCHAR(255)`                | `DEFAULT 'NO_ENCRYPTION'` | Drives format of claims. `NO_ENCRYPTION` means plaintext, `AES_HMAC` for AES encryption with HMAC-based index. |
| `timestamp_created`      | `TIMESTAMP WITHOUT TIME ZONE` | `DEFAULT NOW()'`          | Timestamp of creation.                                                                                         |
| `timestamp_last_updated` | `TIMESTAMP WITHOUT TIME ZONE` |                           | Timestamp of last update if any.                                                                               |

<!-- end -->
