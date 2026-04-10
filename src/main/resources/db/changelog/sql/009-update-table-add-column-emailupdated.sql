-- changeset evgeny:16
UPDATE orders SET user_email = 'unknown@example.com' WHERE user_email IS NULL;
ALTER TABLE orders ALTER COLUMN user_email SET NOT NULL;