-- changeset evgeny:19
UPDATE items SET deleted = false WHERE deleted IS NULL;
ALTER TABLE items ALTER COLUMN deleted SET NOT NULL;