CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    restaurant_id VARCHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL,
    delivery_line1 VARCHAR(255) NOT NULL,
    delivery_city VARCHAR(100) NOT NULL,
    delivery_postal_code VARCHAR(20) NOT NULL,
    lat DOUBLE NOT NULL,
    lng DOUBLE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_orders_customer_id (customer_id),
    INDEX idx_orders_restaurant_id (restaurant_id),
    INDEX idx_orders_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    menu_item_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_items_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
