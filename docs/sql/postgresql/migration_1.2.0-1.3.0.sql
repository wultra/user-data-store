-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::1::Roman Strobl
CREATE TABLE uds_document (id VARCHAR(36) NOT NULL, user_id VARCHAR(255) NOT NULL, document_type VARCHAR(32) NOT NULL, data_type VARCHAR(32) NOT NULL, document_data_id VARCHAR(255), external_id VARCHAR(255), document_data TEXT NOT NULL, attributes TEXT NOT NULL, encryption_mode VARCHAR(255) DEFAULT 'NO_ENCRYPTION' NOT NULL, timestamp_created TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(), timestamp_last_updated TIMESTAMP WITHOUT TIME ZONE, CONSTRAINT uds_document_pkey PRIMARY KEY (id));

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::2::Roman Strobl
CREATE TABLE uds_document_history (id VARCHAR(36) NOT NULL, document_id VARCHAR(36) NOT NULL, user_id VARCHAR(255) NOT NULL, document_type VARCHAR(32) NOT NULL, data_type VARCHAR(32) NOT NULL, document_data_id VARCHAR(255), external_id VARCHAR(255), document_data TEXT NOT NULL, attributes TEXT NOT NULL, encryption_mode VARCHAR(255) DEFAULT 'NO_ENCRYPTION' NOT NULL, timestamp_created TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(), CONSTRAINT uds_document_history_pkey PRIMARY KEY (id));

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::3::Roman Strobl
CREATE TABLE uds_photo (id VARCHAR(36) NOT NULL, document_id VARCHAR(36) NOT NULL, external_id VARCHAR(255), photo_type VARCHAR(32) NOT NULL, photo_data TEXT NOT NULL, encryption_mode VARCHAR(255) DEFAULT 'NO_ENCRYPTION' NOT NULL, timestamp_created TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(), timestamp_last_updated TIMESTAMP WITHOUT TIME ZONE, CONSTRAINT uds_photo_pkey PRIMARY KEY (id));

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::4::Roman Strobl
ALTER TABLE uds_photo ADD CONSTRAINT fk_uds_photo_document_id FOREIGN KEY (document_id) REFERENCES uds_document (id);

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::5::Roman Strobl
CREATE TABLE uds_attachment (id VARCHAR(36) NOT NULL, document_id VARCHAR(36) NOT NULL, external_id VARCHAR(255), attachment_type VARCHAR(32) NOT NULL, attachment_data TEXT NOT NULL, encryption_mode VARCHAR(255) DEFAULT 'NO_ENCRYPTION' NOT NULL, timestamp_created TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(), timestamp_last_updated TIMESTAMP WITHOUT TIME ZONE, CONSTRAINT uds_attachment_pkey PRIMARY KEY (id));

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::6::Roman Strobl
ALTER TABLE uds_attachment ADD CONSTRAINT fk_uds_attachment_document_id FOREIGN KEY (document_id) REFERENCES uds_document (id);

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240614-migrate-data-1.3.0.xml::1::Roman Strobl
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240614-migrate-data-1.3.0.xml::2::Roman Strobl
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

