CREATE TABLE uds_user_claims
(
    user_id                VARCHAR(255) NOT NULL,
    claims                 TEXT         NOT NULL,
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
