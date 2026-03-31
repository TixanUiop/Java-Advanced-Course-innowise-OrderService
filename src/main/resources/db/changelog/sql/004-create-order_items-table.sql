-- changeset evgeny:004

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    item_id BIGINT NOT NULL REFERENCES items(id),
    quantity BIGINT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
