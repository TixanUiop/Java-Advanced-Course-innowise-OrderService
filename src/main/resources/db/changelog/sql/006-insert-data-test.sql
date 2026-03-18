-- changeset evgeny:11
INSERT INTO items (name, price, created_at, updated_at) VALUES
('Laptop', 1200.00, NOW(), NOW()),
('Smartphone', 800.00, NOW(), NOW()),
('Tablet', 450.00, NOW(), NOW()),
('Headphones', 150.00, NOW(), NOW()),
('Keyboard', 70.00, NOW(), NOW()),
('Mouse', 50.00, NOW(), NOW()),
('Monitor', 300.00, NOW(), NOW()),
('Printer', 200.00, NOW(), NOW()),
('Webcam', 90.00, NOW(), NOW()),
('External HDD', 130.00, NOW(), NOW());

-- changeset evgeny:12
INSERT INTO orders (user_id, status, total_price, deleted, created_at, updated_at) VALUES
(1, 'Awaiting', 1250.00, FALSE, NOW(), NOW()),
(2, 'Accepted', 800.00, FALSE, NOW(), NOW()),
(3, 'Collect', 450.00, FALSE, NOW(), NOW()),
(4, 'Sent', 220.00, FALSE, NOW(), NOW()),
(5, 'Ready to receive', 70.00, FALSE, NOW(), NOW()),
(6, 'Awaiting', 500.00, FALSE, NOW(), NOW()),
(7, 'Accepted', 300.00, FALSE, NOW(), NOW()),
(8, 'Collect', 200.00, FALSE, NOW(), NOW()),
(9, 'Sent', 90.00, FALSE, NOW(), NOW()),
(10, 'Ready to receive', 180.00, FALSE, NOW(), NOW());

-- changeset evgeny:13
INSERT INTO order_items (order_id, item_id, quantity, created_at, updated_at) VALUES
(1, 1, 1, NOW(), NOW()),
(1, 4, 1, NOW(), NOW()),
(2, 2, 1, NOW(), NOW()),
(3, 3, 1, NOW(), NOW()),
(4, 5, 1, NOW(), NOW()),
(4, 6, 1, NOW(), NOW()),
(5, 5, 1, NOW(), NOW()),
(6, 7, 1, NOW(), NOW()),
(7, 7, 1, NOW(), NOW()),
(8, 8, 1, NOW(), NOW()),
(9, 9, 1, NOW(), NOW()),
(10, 10, 1, NOW(), NOW());