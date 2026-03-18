-- changeset evgeny:001

CREATE TABLE items(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    price NUMERIC(9, 2) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);