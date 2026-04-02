-- changeset evgeny:14
ALTER TABLE order_items
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;