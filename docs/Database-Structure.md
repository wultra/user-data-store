# Database Structure

<!-- TEMPLATE database -->

User Data Store supports database changes using Liquibase.
But you may download DDL scripts for supported databases:

- [PostgreSQL - Create Database Schema](./sql/postgresql/create-schema.sql)
- [Oracle - Create Database Schema](./sql/oracle/create-schema.sql)
- [MSSQL - Create Database Schema](./sql/mssql/create-schema.sql)

## Auditing

The DDL files contain an `audit_log` table definition. The table differs slightly per database. 

Only one `audit_log` table is required per PowerAuth stack in case the same schema is used for all deployed applications.

For more information about auditing library, see the [Wultra auditing library documentation](https://github.com/wultra/lime-java-core#wultra-auditing-library).

## Authentication

See [JDBC-based Spring Security Schema](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jdbc.html#servlet-authentication-jdbc-schema).
In our case, the authentication tables are prefixed by `uds_`.

## Table Documentation

This chapter explains individual tables and their columns. The column types are used from PostgreSQL dialect, other databases use types that are equivalent (mapping is usually straight-forward).

<!-- begin database table uds_document -->
### Documents Table

Stores documents.

#### Schema

| Name                     | Type                          | Info                               | Note                                                                                                                |
|--------------------------|-------------------------------|------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| `id`                     | `VARCHAR(36)`                 | `NOT NULL PRIMARY KEY`             | UUID identifier of the document, generated when the document is created.                                            |
| `user_id`                | `VARCHAR(255)`                | `NOT NULL`                         | User identifier, owner of the document.                                                                             |
| `document_type`          | `VARCHAR(32)`                 | `NOT NULL`                         | Document type, one of: `profile`, `personal_id`, `passport`, `drivers_license`, `payment_card`, `loyalty`, `photo`. |
| `data_type`              | `VARCHAR(32)`                 | `NOT NULL`                         | Data type, one of: `claims`, `jwt`, `vc`, `image_base64`, `binary_base64`, `url`.                                   |
| `document_data_id`       | `VARCHAR(255)`                |                                    | Optional identifier of the stored document (e.g. ID card number).                                                   |
| `external_id`            | `VARCHAR(255)`                |                                    | Optional external identifier of the stored document (e.g. ID in an external database).                              |
| `document_data`          | `TEXT`                        | `NOT NULL`                         | Data of the document, encrypted in case encryption is enabled.                                                      |
| `attributes`             | `TEXT`                        |                                    | Optional map of attributes related to the document, a key-value map serialized into JSON.                           |
| `encryption_mode`        | `VARCHAR(255)`                | `DEFAULT 'NO_ENCRYPTION' NOT NULL` | Encryption of document data: `NO_ENCRYPTION` means plaintext, `AES_HMAC` for AES encryption with HMAC-based index.  |
| `timestamp_created`      | `TIMESTAMP WITHOUT TIME ZONE` | `DEFAULT NOW()`                    | Timestamp of creation of the document.                                                                              |
| `timestamp_last_updated` | `TIMESTAMP WITHOUT TIME ZONE` |                                    | Optional timestamp of last update of the document.                                                                  |

<!-- end -->

<!-- begin database table uds_document_history -->
### Document History Table

Stores document history.

#### Schema

| Name                     | Type                          | Info                               | Note                                                                                                                |
|--------------------------|-------------------------------|------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| `id`                     | `VARCHAR(36)`                 | `NOT NULL PRIMARY KEY`             | UUID identifier of the document history, generated when the record is created.                                      |
| `document_id`            | `VARCHAR(36)`                 | `NOT NULL`                         | UUID identifier of the document.                                                                                    |
| `user_id`                | `VARCHAR(255)`                | `NOT NULL`                         | User identifier, owner of the document.                                                                             |
| `document_type`          | `VARCHAR(32)`                 | `NOT NULL`                         | Document type, one of: `profile`, `personal_id`, `passport`, `drivers_license`, `payment_card`, `loyalty`, `photo`. |
| `data_type`              | `VARCHAR(32)`                 | `NOT NULL`                         | Data type, one of: `claims`, `jwt`, `vc`, `image_base64`, `binary_base64`, `url`.                                   |
| `document_data_id`       | `VARCHAR(255)`                |                                    | Optional identifier of the stored document (e.g. ID card number).                                                   |
| `external_id`            | `VARCHAR(255)`                |                                    | Optional external identifier of the stored document (e.g. ID in an external database).                              |
| `document_data`          | `TEXT`                        | `NOT NULL`                         | Data of the document, encrypted in case encryption is enabled.                                                      |
| `attributes`             | `TEXT`                        |                                    | Optional map of attributes related to the document, a key-value map serialized into JSON.                           |
| `encryption_mode`        | `VARCHAR(255)`                | `DEFAULT 'NO_ENCRYPTION' NOT NULL` | Encryption of document data: `NO_ENCRYPTION` means plaintext, `AES_HMAC` for AES encryption with HMAC-based index.  |
| `timestamp_created`      | `TIMESTAMP WITHOUT TIME ZONE` | `DEFAULT NOW()'`                   | Timestamp of creation of the record.                                                                                |

<!-- end -->

<!-- begin database table uds_photo -->
### Photos Table

Stores photos.

#### Schema

| Name                     | Type                          | Info                               | Note                                                                                                            |
|--------------------------|-------------------------------|------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| `id`                     | `VARCHAR(36)`                 | `NOT NULL PRIMARY KEY`             | UUID identifier of the photo, generated when the record is created.                                             |
| `document_id`            | `VARCHAR(36)`                 | `NOT NULL`                         | UUID identifier of the document.                                                                                |
| `user_id`                | `VARCHAR(255)`                | `NOT NULL`                         | User identifier, owner of the photo.                                                                            |
| `external_id`            | `VARCHAR(255)`                |                                    | Optional external identifier of the stored photo (e.g. ID in an external database).                             |
| `photo_type`             | `VARCHAR(32)`                 | `NOT NULL`                         | Photo type, one of: `person`, `document_front_side`, `document_back_side`, `person_with_document`.              |
| `photo_data`             | `TEXT`                        | `NOT NULL`                         | Data of the photo, encrypted in case encryption is enabled.                                                     |
| `encryption_mode`        | `VARCHAR(255)`                | `DEFAULT 'NO_ENCRYPTION' NOT NULL` | Encryption of photo data: `NO_ENCRYPTION` means plaintext, `AES_HMAC` for AES encryption with HMAC-based index. |
| `timestamp_created`      | `TIMESTAMP WITHOUT TIME ZONE` | `DEFAULT NOW()`                    | Timestamp of creation of the photo.                                                                             |
| `timestamp_last_updated` | `TIMESTAMP WITHOUT TIME ZONE` |                                    | Optional timestamp of last update of the photo.                                                                 |

<!-- end -->

<!-- begin database table uds_attachment -->
### Attachments Table

Stores attachments.

#### Schema

| Name                     | Type                          | Info                               | Note                                                                                                                 |
|--------------------------|-------------------------------|------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| `id`                     | `VARCHAR(36)`                 | `NOT NULL PRIMARY KEY`             | UUID identifier of the attachment, generated when the record is created.                                             |
| `document_id`            | `VARCHAR(36)`                 | `NOT NULL`                         | UUID identifier of the document.                                                                                     |
| `user_id`                | `VARCHAR(255)`                | `NOT NULL`                         | User identifier, owner of the attachment.                                                                            |
| `external_id`            | `VARCHAR(255)`                |                                    | Optional external identifier of the stored attachment (e.g. ID in an external database).                             |
| `attachment_type`        | `VARCHAR(32)`                 | `NOT NULL`                         | Attachment type, one of: `text`, `image_base64`, `binary_base64`.                                                    |
| `attachment_data`        | `TEXT`                        | `NOT NULL`                         | Data of the attachment, encrypted in case encryption is enabled.                                                     |
| `encryption_mode`        | `VARCHAR(255)`                | `DEFAULT 'NO_ENCRYPTION' NOT NULL` | Encryption of attachment data: `NO_ENCRYPTION` means plaintext, `AES_HMAC` for AES encryption with HMAC-based index. |
| `timestamp_created`      | `TIMESTAMP WITHOUT TIME ZONE` | `DEFAULT NOW()`                    | Timestamp of creation of the attachment.                                                                             |
| `timestamp_last_updated` | `TIMESTAMP WITHOUT TIME ZONE` |                                    | Optional timestamp of last update of the attachment.                                                                 |

<!-- end -->

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