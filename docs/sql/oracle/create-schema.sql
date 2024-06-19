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
