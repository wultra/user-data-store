-- Changeset user-data-store/0.1.x/20230220-initial-schema.xml::1::Lubos Racansky
-- Create a new table uds_user_claims
CREATE TABLE uds_user_claims (user_id varchar(255) NOT NULL, claims varchar (max) NOT NULL, encryption_mode varchar(255) CONSTRAINT DF_uds_user_claims_encryption_mode DEFAULT 'NO_ENCRYPTION' NOT NULL, timestamp_created datetime2 CONSTRAINT DF_uds_user_claims_timestamp_created DEFAULT GETDATE(), timestamp_last_updated datetime2, CONSTRAINT PK_UDS_USER_CLAIMS PRIMARY KEY (user_id));
GO

-- Changeset user-data-store/0.1.x/20230220-initial-schema.xml::2::Lubos Racansky
-- Create a new table uds_users for spring security
CREATE TABLE uds_users (username varchar(50) NOT NULL, password varchar(500) NOT NULL, enabled bit NOT NULL, CONSTRAINT PK_UDS_USERS PRIMARY KEY (username));
GO

-- Changeset user-data-store/0.1.x/20230220-initial-schema.xml::3::Lubos Racansky
-- Create a new table uds_authorities for spring security
CREATE TABLE uds_authorities (username varchar(50) NOT NULL, authority varchar(50) NOT NULL, CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES uds_users(username));
GO

-- Changeset user-data-store/0.1.x/20230220-initial-schema.xml::4::Lubos Racansky
-- Create a new unique index on uds_authorities(username, authority)
CREATE UNIQUE NONCLUSTERED INDEX ix_auth_username ON uds_authorities(username, authority);
GO

-- Changeset user-data-store/0.1.x/20230224-audit.xml::1::Lubos Racansky
-- Create a new table audit_log
CREATE TABLE audit_log (audit_log_id varchar(36) NOT NULL, application_name varchar(256) NOT NULL, audit_level varchar(32) NOT NULL, audit_type varchar(256) NOT NULL, timestamp_created datetime2 CONSTRAINT DF_audit_log_timestamp_created DEFAULT GETDATE(), message varchar (max) NOT NULL, exception_message varchar (max), stack_trace varchar (max), param varchar (max), calling_class varchar(256) NOT NULL, thread_name varchar(256) NOT NULL, version varchar(256), build_time datetime2, CONSTRAINT PK_AUDIT_LOG PRIMARY KEY (audit_log_id));
GO

-- Changeset user-data-store/0.1.x/20230322-audit-param.xml::1::Zdenek Cerny
-- Create a new table audit_param
CREATE TABLE audit_param (audit_log_id varchar(36) NOT NULL, param_key varchar(256), param_value varchar(4000), timestamp_created datetime2 CONSTRAINT DF_audit_param_timestamp_created DEFAULT GETDATE(), CONSTRAINT PK_AUDIT_PARAM PRIMARY KEY (audit_log_id));
GO

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::1::Lubos Racansky
-- Create a new index on audit_log(timestamp_created)
CREATE NONCLUSTERED INDEX audit_log_timestamp ON audit_log(timestamp_created);
GO

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::2::Lubos Racansky
-- Create a new index on audit_log(application_name)
CREATE NONCLUSTERED INDEX audit_log_application ON audit_log(application_name);
GO

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::3::Lubos Racansky
-- Create a new index on audit_log(audit_level)
CREATE NONCLUSTERED INDEX audit_log_level ON audit_log(audit_level);
GO

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::4::Lubos Racansky
-- Create a new index on audit_log(audit_type)
CREATE NONCLUSTERED INDEX audit_log_type ON audit_log(audit_type);
GO

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::6::Lubos Racansky
-- Create a new index on audit_param(timestamp_created)
CREATE NONCLUSTERED INDEX audit_param_timestamp ON audit_param(timestamp_created);
GO

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::7::Lubos Racansky
-- Create a new index on audit_log(param_key)
CREATE NONCLUSTERED INDEX audit_param_key ON audit_param(param_key);
GO

-- Changeset user-data-store/0.1.x/20230322-audit-indexes.xml::8::Lubos Racansky
-- Create a new index on audit_log(param_value)
CREATE NONCLUSTERED INDEX audit_param_value ON audit_param(param_value);
GO

-- Changeset user-data-store/0.1.x/20231003-audit-type-nullable.xml::1::Lubos Racansky
-- Drop not null constraint for audit_log.audit_type
ALTER TABLE audit_log ALTER COLUMN audit_type varchar(256) NULL;
GO

-- Changeset user-data-store/1.0.x/20231003-add-tag-1.0.0::1::Lubos Racansky
