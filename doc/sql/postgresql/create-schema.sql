CREATE TABLE uds_user_claims
(
    user_id                VARCHAR(255) NOT NULL,
    claims                 TEXT         NOT NULL,
    encryption_mode        VARCHAR(255) DEFAULT 'NO_ENCRYPTION' NOT NULL,
    timestamp_created      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    timestamp_last_updated TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uds_user_claims_pkey PRIMARY KEY (user_id)
);

-- Spring Security
CREATE TABLE uds_users
(
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(500) NOT NULL,
    enabled  BOOLEAN NOT NULL
);

create table uds_authorities
(
    username  VARCHAR(50) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES uds_users (username)
);

CREATE UNIQUE INDEX ix_auth_username ON uds_authorities (username, authority);

-- Create audit log table - https://github.com/wultra/lime-java-core#wultra-auditing-library
CREATE TABLE IF NOT EXISTS audit_log
(
    audit_log_id      VARCHAR(36) PRIMARY KEY,
    application_name  VARCHAR(256) NOT NULL,
    audit_level       VARCHAR(32)  NOT NULL,
    audit_type        VARCHAR(256),
    timestamp_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message           TEXT         NOT NULL,
    exception_message TEXT,
    stack_trace       TEXT,
    param             TEXT,
    calling_class     VARCHAR(256) NOT NULL,
    thread_name       VARCHAR(256) NOT NULL,
    version           VARCHAR(256),
    build_time        TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_param
(
    audit_log_id      VARCHAR(36),
    timestamp_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    param_key         VARCHAR(256),
    param_value       VARCHAR(4000)
);
