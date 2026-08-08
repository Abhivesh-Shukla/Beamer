-- Assignment Service Spatial Database Initialization
USE assignment_db;

CREATE TABLE IF NOT EXISTS driver_locations (
    driver_id VARCHAR(36) PRIMARY KEY,
    location POINT SRID 4326 NOT NULL,
    status VARCHAR(20) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    SPATIAL INDEX idx_driver_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample driver location seed (Chennai location)
INSERT INTO driver_locations (driver_id, location, status, updated_at)
VALUES (
    'd1111111-1111-1111-1111-111111111111',
    ST_SRID(POINT(80.2800, 13.0900), 4326),
    'AVAILABLE',
    NOW(6)
) ON DUPLICATE KEY UPDATE status = 'AVAILABLE', updated_at = NOW(6);

-- Verification query testing ST_Distance_Sphere against reference point (80.2707, 13.0827)
SELECT 
    driver_id,
    status,
    ST_Distance_Sphere(
        location,
        ST_SRID(POINT(80.2707, 13.0827), 4326)
    ) AS distance_m
FROM driver_locations
WHERE status = 'AVAILABLE';
