-- Changeset user-data-store/1.4.x/20250520-shedlock.xml::1::Lubos Racansky
-- Create a new table shedlock
CREATE TABLE shedlock (name varchar(64) NOT NULL, lock_until datetime2 NOT NULL, locked_at datetime2 NOT NULL, locked_by varchar(255) NOT NULL, CONSTRAINT PK_SHEDLOCK PRIMARY KEY (name));
GO
