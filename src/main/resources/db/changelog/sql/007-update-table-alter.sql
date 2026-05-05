-- changeset evgeny:15
ALTER TABLE order_items
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;