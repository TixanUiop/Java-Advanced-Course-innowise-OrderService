-- changeset evgeny:005

CREATE INDEX idx_items_name ON items(name);

-- changeset evgeny:006
CREATE INDEX idx_orders_user_id ON orders(user_id);

-- changeset evgeny:007
CREATE INDEX idx_orders_user_status ON orders(user_id, status);

-- changeset evgeny:008
CREATE INDEX idx_orders_active ON orders(user_id) WHERE deleted = false;

-- changeset evgeny:009
CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- changeset evgeny:010
CREATE INDEX idx_order_items_item_id ON order_items(item_id);