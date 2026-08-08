-- Microservice Database Schemas Initialization
CREATE DATABASE IF NOT EXISTS order_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS restaurant_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS delivery_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS assignment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ops_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant permissions to default application user
GRANT ALL PRIVILEGES ON order_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON restaurant_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON delivery_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON assignment_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON ops_db.* TO 'root'@'%';

FLUSH PRIVILEGES;
