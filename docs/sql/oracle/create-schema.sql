-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: ./docs/db/changelog/changesets/user-data-store/db.changelog-module.xml
-- Ran at: 6/19/24, 11:37 AM
-- Against: null@offline:oracle
-- Liquibase version: 4.25.0
-- *********************************************************************

-- Changeset user-data-store/0.1.x/20230220-initial-schema.xml::1::Lubos Racansky
-- Create a new table uds_user_claims
CREATE TABLE uds_user_claims (user_id VARCHAR2(255) NOT NULL, claims CLOB NOT NULL, encryption_mode VARCHAR2(255) DEFAULT 'NO_ENCRYPTION' NOT NULL, timestamp_created TIMESTAMP DEFAULT sysdate, timestamp_last_updated TIMESTAMP, CONSTRAINT PK_UDS_USER_CLAIMS PRIMARY KEY (user_id));

-- Changeset user-data-store/0.1.x/20230220-initial-schema.xml::2::Lubos Racansky
-- Create a new table uds_users for spring security
CREATE TABLE uds_users (username VARCHAR2(50) NOT NULL, "password" VARCHAR2(500) NOT NULL, enabled BOOLEAN NOT NULL, CONSTRAINT PK_UDS_USERS PRIMARY KEY (username));

-- Changeset user-data-store/0.1.x/20230220-initial-schema.xml::3::Lubos Racansky
-- Create a new table uds_authorities for spring security
CREATE TABLE uds_authorities (username VARCHAR2(50) NOT NULL, authority VARCHAR2(50) NOT NULL, CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES uds_users(username));

-- Changeset user-data-store/0.1.x/20230220-initial-schema.xml::4::Lubos Racansky
-- Create a new unique index on uds_authorities(username, authority)
CREATE UNIQUE INDEX ix_auth_username ON uds_authorities(username, authority);

-- Changeset user-data-store/0.1.x/20230224-audit.xml::1::Lubos Racansky
-- Create a new table audit_log
CREATE TABLE audit_log (audit_log_id VARCHAR2(36) NOT NULL, application_name VARCHAR2(256) NOT NULL, audit_level VARCHAR2(32) NOT NULL, audit_type VARCHAR2(256) NOT NULL, timestamp_created TIMESTAMP DEFAULT sysdate, message CLOB NOT NULL, exception_message CLOB, stack_trace CLOB, param CLOB, calling_class VARCHAR2(256) NOT NULL, thread_name VARCHAR2(256) NOT NULL, version VARCHAR2(256), build_time TIMESTAMP, CONSTRAINT PK_AUDIT_LOG PRIMARY KEY (audit_log_id));

-- Changeset user-data-store/0.1.x/20230322-audit-param.xml::1::Zdenek Cerny
-- Create a new table audit_param
CREATE TABLE audit_param (audit_log_id VARCHAR2(36) NOT NULL, param_key VARCHAR2(256), param_value VARCHAR2(4000), timestamp_created TIMESTAMP DEFAULT sysdate, CONSTRAINT PK_AUDIT_PARAM PRIMARY KEY (audit_log_id));

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::1::Lubos Racansky
-- Create a new index on audit_log(timestamp_created)
CREATE INDEX audit_log_timestamp ON audit_log(timestamp_created);

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::2::Lubos Racansky
-- Create a new index on audit_log(application_name)
CREATE INDEX audit_log_application ON audit_log(application_name);

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::3::Lubos Racansky
-- Create a new index on audit_log(audit_level)
CREATE INDEX audit_log_level ON audit_log(audit_level);

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::4::Lubos Racansky
-- Create a new index on audit_log(audit_type)
CREATE INDEX audit_log_type ON audit_log(audit_type);

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::5::Lubos Racansky
-- Create a new index on audit_param(audit_log_id)
CREATE INDEX audit_param_log ON audit_param(audit_log_id);

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::6::Lubos Racansky
-- Create a new index on audit_param(timestamp_created)
CREATE INDEX audit_param_timestamp ON audit_param(timestamp_created);

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::7::Lubos Racansky
-- Create a new index on audit_log(param_key)
CREATE INDEX audit_param_key ON audit_param(param_key);

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::8::Lubos Racansky
-- Create a new index on audit_log(param_value)
CREATE INDEX audit_param_value ON audit_param(param_value);

-- Changeset user-data-store/0.1.x/20231003-audit-type-nullable.xml::1::Lubos Racansky
-- Drop not null constraint for audit_log.audit_type
ALTER TABLE audit_log MODIFY audit_type NULL;

-- Changeset user-data-store/1.0.x/20231003-add-tag-1.0.0::1::Lubos Racansky
-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::1::Roman Strobl
CREATE TABLE uds_document (id VARCHAR2(36) NOT NULL, user_id VARCHAR2(255) NOT NULL, document_type VARCHAR2(32) NOT NULL, data_type VARCHAR2(32) NOT NULL, document_data_id VARCHAR2(255), external_id VARCHAR2(255), document_data CLOB NOT NULL, attributes CLOB NOT NULL, encryption_mode VARCHAR2(255) DEFAULT 'NO_ENCRYPTION' NOT NULL, timestamp_created TIMESTAMP DEFAULT sysdate, timestamp_last_updated TIMESTAMP, CONSTRAINT PK_UDS_DOCUMENT PRIMARY KEY (id));

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::2::Roman Strobl
CREATE TABLE uds_document_history (id VARCHAR2(36) NOT NULL, document_id VARCHAR2(36) NOT NULL, user_id VARCHAR2(255) NOT NULL, document_type VARCHAR2(32) NOT NULL, data_type VARCHAR2(32) NOT NULL, document_data_id VARCHAR2(255), external_id VARCHAR2(255), document_data CLOB NOT NULL, attributes CLOB NOT NULL, encryption_mode VARCHAR2(255) DEFAULT 'NO_ENCRYPTION' NOT NULL, timestamp_created TIMESTAMP DEFAULT sysdate, CONSTRAINT PK_UDS_DOCUMENT_HISTORY PRIMARY KEY (id));

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::3::Roman Strobl
CREATE TABLE uds_photo (id VARCHAR2(36) NOT NULL, user_id VARCHAR2(255) NOT NULL, document_id VARCHAR2(255) NOT NULL, external_id VARCHAR2(255), photo_type VARCHAR2(32) NOT NULL, photo_data CLOB NOT NULL, encryption_mode VARCHAR2(255) DEFAULT 'NO_ENCRYPTION' NOT NULL, timestamp_created TIMESTAMP DEFAULT sysdate, timestamp_last_updated TIMESTAMP, CONSTRAINT PK_UDS_PHOTO PRIMARY KEY (id));

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::4::Roman Strobl
ALTER TABLE uds_photo ADD CONSTRAINT fk_uds_photo_document_id FOREIGN KEY (document_id) REFERENCES uds_document (id);

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::5::Roman Strobl
CREATE TABLE uds_attachment (id VARCHAR2(36) NOT NULL, user_id VARCHAR2(255) NOT NULL, document_id VARCHAR2(255) NOT NULL, external_id VARCHAR2(255), attachment_type VARCHAR2(32) NOT NULL, attachment_data CLOB NOT NULL, encryption_mode VARCHAR2(255) DEFAULT 'NO_ENCRYPTION' NOT NULL, timestamp_created TIMESTAMP DEFAULT sysdate, timestamp_last_updated TIMESTAMP, CONSTRAINT PK_UDS_ATTACHMENT PRIMARY KEY (id));

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::6::Roman Strobl
ALTER TABLE uds_attachment ADD CONSTRAINT fk_uds_attachment_document_id FOREIGN KEY (document_id) REFERENCES uds_document (id);

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240614-migrate-data-1.3.0.xml::3::Roman Strobl
CREATE OR REPLACE FUNCTION uuid_generate_v4
                RETURN RAW IS
                v_uuid RAW(16);

BEGIN
                SELECT SYS_GUID() INTO v_uuid FROM DUAL;
                RETURN v_uuid;
            END;

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240614-migrate-data-1.3.0.xml::4::Roman Strobl
INSERT INTO uds_document (
                id,
                user_id,
                document_type,
                data_type,
                document_data_id,
                external_id,
                document_data,
                attributes,
                encryption_mode,
                timestamp_created,
                timestamp_last_updated
            )
            SELECT
                uuid_generate_v4(),
                user_id,
                'profile',
                'claims',
                NULL,
                NULL,
                claims,
                '{}',
                encryption_mode,
                timestamp_created,
                timestamp_last_updated
            FROM
                uds_user_claims;

