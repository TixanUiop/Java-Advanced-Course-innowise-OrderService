-- changeset evgeny:18
ALTER TABLE items
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false;