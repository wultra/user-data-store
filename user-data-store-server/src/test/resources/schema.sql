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

CREATE TABLE uds_users (username VARCHAR(50) NOT NULL, password VARCHAR(500) NOT NULL, enabled BOOLEAN NOT NULL, CONSTRAINT uds_users_pkey PRIMARY KEY (username));
CREATE TABLE uds_authorities (username VARCHAR(50) NOT NULL, authority VARCHAR(50) NOT NULL, CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES uds_users(username));
INSERT INTO uds_users (username, password, enabled) VALUES ('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', true);
INSERT INTO uds_authorities (username, authority) VALUES ('admin', 'ROLE_WRITE');
INSERT INTO uds_authorities (username, authority) VALUES ('admin', 'ROLE_READ');
