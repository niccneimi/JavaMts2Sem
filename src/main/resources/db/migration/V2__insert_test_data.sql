INSERT INTO users (name, email) VALUES
('Иван', 'ivan@mail.ru'),
('Мария', 'maria@mail.ru'),
('Алексей', 'alex@mail.ru');

INSERT INTO orders (order_number, total_amount, status, user_id) VALUES
('ORD-001', 150.50, 'COMPLETED', 1),
('ORD-002', 75.20, 'PROCESSING', 1),
('ORD-003', 210.75, 'COMPLETED', 2),
('ORD-004', 49.99, 'PENDING', 3); 