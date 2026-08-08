# Food Delivery Platform — System Architecture

This document describes the high-level system architecture, service boundaries, database design, event streaming model, and operational topology of the Food Delivery Order Processing Platform.

---

## 1. System Overview

```text
                           CLIENT
                             │
                             │ REST
                             ▼
                    ┌──────────────────┐
                    │  ORDER SERVICE   │
                    │ Java 21 / Spring │
                    │    order_db      │
                    └────────┬─────────┘
                             │
                       order-created
                             │
                             ▼
                    ┌──────────────────┐
                    │      KAFKA       │
                    │      KRaft       │
                    └───┬────┬────┬────┘
                        │    │    │
              ┌─────────┘    │    └──────────┐
              ▼              ▼               ▼
       RESTAURANT       ASSIGNMENT       NOTIFICATION
        SERVICE          SERVICE           SERVICE
      Java/Spring      Python/FastAPI     Java/Python
      restaurant_db    assignment_db
              │              │
              │              │ REST
              │              ▼
              │         ETA SERVICE
              │        Python/FastAPI
              │
       order-accepted
              │
              ▼
        ORDER SERVICE


       ASSIGNMENT
           │
           │ driver-assigned
           ▼
    DELIVERY SERVICE
     Java / Spring
      delivery_db
           │
           │ order-delivered
           ▼
      ORDER SERVICE


                 ┌──────────────────────────┐
                 │      OPS DASHBOARD       │
                 │      Node / Express      │
                 │          ops_db          │
                 └────────────┬─────────────┘
                              │
                       polls /health
                       polls /metrics
                              │
             ┌────────────────┼────────────────┐
             ▼                ▼                ▼
          Java services    Python services   ...
```

---

## 2. Microservice Topology & Boundaries

| Service | Technology | Primary Database | Key Responsibilities |
| :--- | :--- | :--- | :--- |
| **Order Service** | Java 21 / Spring Boot | `order_db` (MySQL 8) | Manages order creation, order state machine, REST API composition (`GET /orders/{id}/full-status`). Publishes `order-created`. |
| **Restaurant Service** | Java 21 / Spring Boot | `restaurant_db` (MySQL 8) | Manages restaurant profile & menu. Consumes `order-created`, verifies item availability/prep time, publishes `order-accepted`. |
| **ETA Service** | Python 3.12+ / FastAPI | *None* | Calculates real-time delivery duration predictions via REST API (`POST /predict-eta`). |
| **Assignment Service** | Python 3.12+ / FastAPI | `assignment_db` (MySQL 8) | Stores driver locations using MySQL 8 Spatial `POINT SRID 4326`. Consumes `order-created`, queries 5 nearest drivers, calls ETA Service, publishes `driver-assigned` or `driver-assignment-dlq`. |
| **Delivery Service** | Java 21 / Spring Boot | `delivery_db` (MySQL 8) | Manages delivery state transitions (`ASSIGNED` → `PICKED_UP` → `IN_TRANSIT` → `DELIVERED`). Consumes `driver-assigned`, publishes `order-delivered`. |
| **Notification Service**| Java / Python | *None* | Consumes `order-created` and `order-delivered`, emits correlation-aware structured logs for notifications. |
| **Ops Dashboard** | Node.js / Express / Chart.js | `ops_db` (MySQL 8) | Periodically polls `/health` and `/metrics` / `/metrics-lite` across all services, stores metric snapshots, renders web dashboard and order funnel. |

---

## 3. Database Isolation & Spatial Model

### 3.1 Database-per-Service Principle
Each service strictly owns its MySQL 8 schema. Direct cross-database access is prohibited. Services communicate solely via **Kafka events** or **REST APIs**.

```text
Order Service       → order_db
Restaurant Service  → restaurant_db
Delivery Service    → delivery_db
Assignment Service  → assignment_db
Ops Dashboard       → ops_db
```

### 3.2 MySQL 8 Spatial Location Engine
Driver locations in `assignment_db` use MySQL 8 native spatial data types with WGS 84 coordinate system (`SRID 4326`) and spatial indexing:

```sql
CREATE TABLE driver_locations (
    driver_id VARCHAR(36) PRIMARY KEY,
    location POINT SRID 4326 NOT NULL,
    status VARCHAR(20) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    SPATIAL INDEX idx_driver_location (location)
);
```

Nearest-driver lookup uses `ST_Distance_Sphere`:

```sql
SELECT
    driver_id,
    ST_Distance_Sphere(
        location,
        ST_SRID(POINT(:lng, :lat), 4326)
    ) AS distance_m
FROM driver_locations
WHERE status = 'AVAILABLE'
ORDER BY distance_m ASC
LIMIT 5;
```

---

## 4. Event Streaming & Kafka Topology

The platform uses **Kafka in KRaft mode** (no ZooKeeper).

### 4.1 Topics & Subscriptions

| Topic Name | Producer Service | Consumer Service(s) | Payload Content |
| :--- | :--- | :--- | :--- |
| `order-created` | Order Service | Restaurant Service, Assignment Service, Notification Service | Order ID, customer, restaurant, delivery lat/lng, address |
| `order-accepted` | Restaurant Service | Order Service | Order ID, restaurant ID, estimated prep time |
| `driver-assigned` | Assignment Service | Delivery Service, Order Service | Order ID, driver ID, distance (km), ETA (min) |
| `order-delivered` | Delivery Service | Order Service, Notification Service | Order ID, driver ID, timestamp |
| `driver-assignment-dlq` | Assignment Service | *Monitoring / DLQ Handlers* | Failed assignment payloads after max retries |

---

## 5. Inter-Service Communication & API Composition

### 5.1 Asynchronous Event-Driven Workflow
1. Client submits `POST /orders` to **Order Service**. Order saved in `order_db` (`CREATED`).
2. Order Service emits `order-created` to Kafka.
3. **Restaurant Service** consumes `order-created`, verifies prep time, emits `order-accepted`.
4. **Assignment Service** consumes `order-created`, performs MySQL spatial query for 5 nearest available drivers, calls **ETA Service** via REST (`POST /predict-eta`), selects optimal driver, emits `driver-assigned`.
5. **Delivery Service** consumes `driver-assigned`, creates delivery (`ASSIGNED`). As driver updates status (`PICKED_UP` → `IN_TRANSIT` → `DELIVERED`), Delivery Service emits `order-delivered` when complete.
6. **Order Service** updates local order status upon consuming events.

### 5.2 Synchronous API Composition
`GET /orders/{id}/full-status` on Order Service aggregates data on-the-fly via REST calls to:
- Local `Order Service` state (`orders` / `order_items`)
- `Restaurant Service` (`GET /restaurants/{id}`)
- `Delivery Service` (`GET /deliveries/{id}`)
- `ETA Service` (where applicable)

Resilience is guaranteed using Resilience4j circuit breakers, timeouts, retries, and fallbacks.

---

## 6. Observability & Custom Ops Dashboard

The system avoids third-party metric collectors (Prometheus/Grafana):
- **Structured JSON Logging**: Every service outputs JSON logs with correlation IDs (`orderId`, `traceId`).
- **Health & Metrics Endpoints**:
  - Java services expose `/actuator/health` and `/actuator/metrics`.
  - Python services expose `/health` and `/metrics-lite`.
- **Ops Dashboard Poller**: Node.js worker polls all health/metric endpoints every 10 seconds, persists metric snapshots to `ops_db.service_metric_snapshots`, and renders dynamic Chart.js dashboards.

---

## 7. Operational & Architectural Constraints

1. **Database**: MySQL 8 strictly everywhere. No PostgreSQL.
2. **Spatial Engine**: MySQL Spatial `POINT SRID 4326` with `SPATIAL INDEX`. No Redis / Redis GEO.
3. **Event Broker**: Kafka KRaft mode. No ZooKeeper.
4. **Observability**: Custom Ops Dashboard & structured JSON logging. No Prometheus or Grafana.
5. **Deployment**: Docker Compose blue-green cutover scripts.
