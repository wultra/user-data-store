-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::1::Roman Strobl
CREATE TABLE uds_document (id varchar(36) NOT NULL, user_id varchar(255) NOT NULL, document_type varchar(32) NOT NULL, data_type varchar(32) NOT NULL, document_data_id varchar(255), external_id varchar(255), document_data varchar (max) NOT NULL, attributes varchar (max) NOT NULL, encryption_mode varchar(255) CONSTRAINT DF_uds_document_encryption_mode DEFAULT 'NO_ENCRYPTION' NOT NULL, timestamp_created datetime2 CONSTRAINT DF_uds_document_timestamp_created DEFAULT GETDATE(), timestamp_last_updated datetime2, CONSTRAINT PK_UDS_DOCUMENT PRIMARY KEY (id));
GO

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::2::Roman Strobl
CREATE TABLE uds_document_history (id varchar(36) NOT NULL, document_id varchar(36) NOT NULL, user_id varchar(255) NOT NULL, document_type varchar(32) NOT NULL, data_type varchar(32) NOT NULL, document_data_id varchar(255), external_id varchar(255), document_data varchar (max) NOT NULL, attributes varchar (max) NOT NULL, encryption_mode varchar(255) CONSTRAINT DF_uds_document_history_encryption_mode DEFAULT 'NO_ENCRYPTION' NOT NULL, timestamp_created datetime2 CONSTRAINT DF_uds_document_history_timestamp_created DEFAULT GETDATE(), CONSTRAINT PK_UDS_DOCUMENT_HISTORY PRIMARY KEY (id));
GO

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::3::Roman Strobl
CREATE TABLE uds_photo (id varchar(36) NOT NULL, user_id varchar(255) NOT NULL, document_id varchar(36) NOT NULL, external_id varchar(255), photo_type varchar(32) NOT NULL, photo_data varchar (max) NOT NULL, encryption_mode varchar(255) CONSTRAINT DF_uds_photo_encryption_mode DEFAULT 'NO_ENCRYPTION' NOT NULL, timestamp_created datetime2 CONSTRAINT DF_uds_photo_timestamp_created DEFAULT GETDATE(), timestamp_last_updated datetime2, CONSTRAINT PK_UDS_PHOTO PRIMARY KEY (id));
GO

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::4::Roman Strobl
ALTER TABLE uds_photo ADD CONSTRAINT fk_uds_photo_document_id FOREIGN KEY (document_id) REFERENCES uds_document (id);
GO

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::5::Roman Strobl
CREATE TABLE uds_attachment (id varchar(36) NOT NULL, user_id varchar(255) NOT NULL, document_id varchar(36) NOT NULL, external_id varchar(255), attachment_type varchar(32) NOT NULL, attachment_data varchar (max) NOT NULL, encryption_mode varchar(255) CONSTRAINT DF_uds_attachment_encryption_mode DEFAULT 'NO_ENCRYPTION' NOT NULL, timestamp_created datetime2 CONSTRAINT DF_uds_attachment_timestamp_created DEFAULT GETDATE(), timestamp_last_updated datetime2, CONSTRAINT PK_UDS_ATTACHMENT PRIMARY KEY (id));
GO

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240514-refactor-uds-1.3.0.xml::6::Roman Strobl
ALTER TABLE uds_attachment ADD CONSTRAINT fk_uds_attachment_document_id FOREIGN KEY (document_id) REFERENCES uds_document (id);
GO

-- Changeset docs/db/changelog/changesets/user-data-store/1.3.x/20240812-import-result.xml::1::Roman Strobl
CREATE TABLE uds_import_result (id varchar(36) NOT NULL, import_path varchar(255), user_id varchar(255) NOT NULL, document_id varchar(36) NOT NULL, photo_id varchar(36), attachment_id varchar(36), imported bit NOT NULL, error varchar(255), timestamp_created datetime2 CONSTRAINT DF_uds_import_result_timestamp_created DEFAULT GETDATE(), CONSTRAINT PK_UDS_IMPORT_RESULT PRIMARY KEY (id));
GO
