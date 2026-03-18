-- changeset evgeny:003

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    status order_status default 'Awaiting' not null,
    total_price numeric(9,2) not null,
    deleted BOOLEAN default false not null,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
