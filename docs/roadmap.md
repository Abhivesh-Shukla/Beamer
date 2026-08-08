# Food Delivery Order Processing Platform — Full Antigravity Build Roadmap

## 0. Architecture

```text
                           CLIENT
                             │
                             │ REST
                             ▼
                    ┌──────────────────┐
                    │  ORDER SERVICE   │
                    │ Java / Spring    │
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
       Java/Spring     Python/FastAPI     Java/Python
       restaurant_db   assignment_db
              │              │
              │              │ REST
              │              ▼
              │         ETA SERVICE
              │         Python/FastAPI
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

# Phase 0 — Repository & Project Setup

## Goal

Create the monorepo and documentation before implementing business logic.

## Repository

```text
food-delivery-platform/
│
├── services/
│   ├── order-service/
│   ├── restaurant-service/
│   ├── delivery-service/
│   ├── eta-service/
│   ├── assignment-service/
│   ├── notification-service/
│   └── ops-dashboard/
│
├── infra/
│   ├── docker-compose.yml
│   ├── mysql/
│   │   └── init/
│   └── logging/
│       ├── logback-json.xml
│       └── python-logging.json
│
├── docs/
│   ├── roadmap.md
│   ├── architecture.md
│   ├── decisions.md
│   ├── kafka-contracts/
│   └── api/
│
├── tests/
│   └── e2e/
│
├── scripts/
│
├── .github/
│   └── workflows/
│
├── .gitignore
├── README.md
└── LICENSE
```

## Initial documentation

Create:

```text
docs/roadmap.md
docs/architecture.md
docs/decisions.md
```

`roadmap.md` contains this complete Phase 0–13 plan.

`architecture.md` contains:

* services
* databases
* Kafka topics
* REST communication
* event flow
* deployment architecture

`decisions.md` contains ADR-style decisions.

First decisions:

```text
ADR-001: MySQL 8 instead of PostgreSQL
ADR-002: MySQL spatial instead of Redis GEO
ADR-003: Kafka KRaft instead of ZooKeeper
ADR-004: Custom Ops Dashboard instead of Prometheus/Grafana
ADR-005: Database-per-service
```

## Antigravity prompt

```text
Antigravity, inspect the current repository before making changes.

Initialize the project as a monorepo for a food-delivery distributed
systems project.

Create:

services/
  order-service/
  restaurant-service/
  delivery-service/
  eta-service/
  assignment-service/
  notification-service/
  ops-dashboard/

infra/
  mysql/init/
  logging/

docs/
  roadmap.md
  architecture.md
  decisions.md
  kafka-contracts/
  api/

tests/e2e/
scripts/
.github/workflows/

Create a root README.md and appropriate .gitignore.

Architecture constraints:

- MySQL 8 everywhere
- Kafka KRaft
- MySQL spatial POINT for driver locations
- no Redis
- no PostgreSQL
- no Prometheus
- no Grafana
- database-per-service
- structured JSON logging
- custom Ops Dashboard

Do not implement business logic.

Do not add technologies that aren't required.

After creating the structure, verify that the repository layout is correct.
```

## Checkpoint

You should have a clean monorepo and documentation.

---

# Phase 1 — Contracts Before Code

This phase defines the communication contracts.

## Kafka topics

```text
order-created
order-accepted
driver-assigned
order-delivered
driver-assignment-dlq
```

Potential future topic:

```text
driver-location-updated
```

---

## Event 1 — `order-created`

```json
{
  "eventId": "uuid",
  "eventType": "order-created",
  "orderId": "uuid",
  "customerId": "uuid",
  "restaurantId": "uuid",
  "items": [],
  "deliveryAddress": {
    "line1": "123 Main Street",
    "city": "Chennai",
    "postalCode": "600001"
  },
  "lat": 13.0827,
  "lng": 80.2707,
  "timestamp": "2026-08-08T18:00:00Z"
}
```

---

## Event 2 — `order-accepted`

```json
{
  "eventId": "uuid",
  "eventType": "order-accepted",
  "orderId": "uuid",
  "restaurantId": "uuid",
  "prepTimeMinutes": 20,
  "timestamp": "2026-08-08T18:02:00Z"
}
```

---

## Event 3 — `driver-assigned`

```json
{
  "eventId": "uuid",
  "eventType": "driver-assigned",
  "orderId": "uuid",
  "driverId": "uuid",
  "distanceKm": 2.4,
  "etaMinutes": 28,
  "timestamp": "2026-08-08T18:04:00Z"
}
```

---

## Event 4 — `order-delivered`

```json
{
  "eventId": "uuid",
  "eventType": "order-delivered",
  "orderId": "uuid",
  "driverId": "uuid",
  "deliveredAt": "2026-08-08T18:32:00Z"
}
```

## Database ownership

```text
Order Service       → order_db
Restaurant Service  → restaurant_db
Delivery Service    → delivery_db
Assignment Service  → assignment_db
Ops Dashboard       → ops_db
```

No service directly accesses another service's database.

## OpenAPI

Create:

```text
docs/api/
├── order-service.yaml
├── restaurant-service.yaml
├── delivery-service.yaml
├── eta-service.yaml
├── assignment-service.yaml
└── ops-dashboard.yaml
```

## Antigravity prompt

```text
Antigravity, inspect docs/architecture.md and the current repository.

Implement Phase 1 only: communication contracts.

Create JSON Schema files:

docs/kafka-contracts/order-created.json
docs/kafka-contracts/order-accepted.json
docs/kafka-contracts/driver-assigned.json
docs/kafka-contracts/order-delivered.json

Use these exact event fields:

order-created:
eventId, eventType, orderId, customerId, restaurantId,
items, deliveryAddress, lat, lng, timestamp

order-accepted:
eventId, eventType, orderId, restaurantId,
prepTimeMinutes, timestamp

driver-assigned:
eventId, eventType, orderId, driverId,
distanceKm, etaMinutes, timestamp

order-delivered:
eventId, eventType, orderId, driverId, deliveredAt

Add required fields, types, formats and descriptions.

Also create initial OpenAPI specifications for:
- Order Service
- Restaurant Service
- Delivery Service
- ETA Service
- Assignment Service
- Ops Dashboard

Do not implement services yet.

Do not modify the event contract after creating it unless explicitly requested.
```

## Checkpoint

Contracts are stable.

---

# Phase 2 — Infrastructure

## Components

```text
MySQL 8
Kafka KRaft
Ops Dashboard placeholder
```

No:

```text
Redis
PostgreSQL
ZooKeeper
Prometheus
Grafana
```

## Docker network

```text
delivery-net
```

## Databases

```text
order_db
restaurant_db
delivery_db
assignment_db
ops_db
```

## Spatial database

Create:

```sql
CREATE TABLE driver_locations (
    driver_id VARCHAR(36) PRIMARY KEY,
    location POINT SRID 4326 NOT NULL,
    status VARCHAR(20) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    SPATIAL INDEX idx_driver_location (location)
);
```

Test:

```sql
SELECT ST_Distance_Sphere(
    ST_SRID(POINT(80.2707, 13.0827), 4326),
    ST_SRID(POINT(80.2800, 13.0900), 4326)
);
```

## Antigravity prompt

```text
Antigravity, implement Phase 2 infrastructure.

Create docker-compose.yml with:

- MySQL 8
- Kafka in KRaft mode
- placeholder ops-dashboard

Use Docker network:
delivery-net

Create databases:
order_db
restaurant_db
delivery_db
assignment_db
ops_db

Requirements:
- MySQL 8
- Kafka KRaft
- no ZooKeeper
- no Redis
- no PostgreSQL
- no Prometheus
- no Grafana

Add:
- persistent volumes
- healthchecks
- environment variables
- reasonable resource configuration
- Kafka topic initialization where appropriate

Create MySQL initialization scripts.

Create a scratch spatial SQL script that:
1. creates a POINT SRID 4326 column
2. creates a SPATIAL INDEX
3. inserts test coordinates
4. executes ST_Distance_Sphere

Add commented placeholders for the application containers.

Run docker compose config and validate the configuration.
```

## Checkpoint

```bash
docker compose up
```

All infrastructure should start successfully.

---

# Phase 3 — Order Service

## Technology

```text
Java 21
Spring Boot
Spring Web
Spring Data JPA
MySQL 8
Kafka
Flyway
Validation
Actuator
Testcontainers
```

## Tables

```text
orders
order_items
```

## Order states

```text
CREATED
ACCEPTED
ASSIGNED
PICKED_UP
IN_TRANSIT
DELIVERED
```

Optional:

```text
CANCELLED
```

## REST

```http
POST /orders
GET /orders/{id}
GET /orders/{id}/full-status
```

Initially implement only the first two.

## Flow

```text
POST /orders
      ↓
validate
      ↓
save MySQL
      ↓
publish order-created
```

## Kafka consumers

Later consume:

```text
order-accepted
driver-assigned
order-delivered
```

## Logging

```json
{
  "timestamp": "...",
  "level": "INFO",
  "service": "order-service",
  "orderId": "...",
  "message": "Order created"
}
```

## Antigravity implementation sequence

Don't ask Antigravity to implement the entire service in one shot.

### 3A — Project

```text
Antigravity, implement Phase 3A.

Inspect the repository first.

Create the Order Service Spring Boot project using:
- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- MySQL 8
- Flyway
- Validation
- Kafka
- Actuator
- Testcontainers

Implement only:
- project configuration
- Order entity
- OrderItem entity
- repositories
- DTOs
- validation
- Flyway migration

Use MySQL 8 syntax only.

Do not implement REST endpoints or Kafka yet.

Run compilation and tests.
```

### 3B — REST

```text
Antigravity, continue Phase 3.

Implement:

POST /orders
GET /orders/{id}

POST /orders must:
1. validate input
2. create Order
3. create OrderItems
4. persist using a transaction
5. return the created order

Add controller/service tests.

Do not change Kafka contracts.

Run all tests.
```

### 3C — Kafka

```text
Antigravity, continue Phase 3.

Implement the order-created Kafka producer.

After an order is successfully persisted:
- construct the event using docs/kafka-contracts/order-created.json
- publish to order-created
- include lat/lng
- include orderId
- include eventId
- include timestamp

Handle Kafka failures safely.

Add unit tests and Kafka integration tests.

Do not modify the event schema.
```

### 3D — Observability

```text
Antigravity, continue Phase 3.

Add:
- Actuator health
- Actuator metrics
- structured JSON logging
- orderId correlation
- Dockerfile
- MySQL Testcontainers tests

Do not add Prometheus/Grafana.

Run the complete Order Service test suite.
```

## Checkpoint

You can create an order and observe:

```text
REST
 ↓
MySQL
 ↓
Kafka
 ↓
JSON logs
```

---

# Phase 4 — Restaurant Service

## Tables

```text
restaurants
menu_items
```

## REST

```http
POST /restaurants
GET /restaurants/{id}

POST /restaurants/{id}/menu
GET /restaurants/{id}/menu

PUT /menu-items/{id}
DELETE /menu-items/{id}
```

## Kafka

Consume:

```text
order-created
```

Publish:

```text
order-accepted
```

## Flow

```text
order-created
      ↓
restaurant lookup
      ↓
prep time
      ↓
order-accepted
```

## Antigravity prompt

```text
Antigravity, implement Phase 4: Restaurant Service.

Inspect:
- docs/architecture.md
- docs/decisions.md
- docs/kafka-contracts/order-created.json
- docs/kafka-contracts/order-accepted.json

Technology:
- Java 21
- Spring Boot
- JPA
- MySQL 8
- Kafka
- Flyway
- Validation
- Actuator
- Testcontainers

Implement:
restaurants
menu_items

REST:
POST /restaurants
GET /restaurants/{id}
POST /restaurants/{id}/menu
GET /restaurants/{id}/menu
PUT /menu-items/{id}
DELETE /menu-items/{id}

Consume order-created.

For each order:
- validate event
- find restaurant
- calculate/select prep time
- publish order-accepted

Add:
- unit tests
- MySQL Testcontainers tests
- Kafka integration tests
- structured JSON logging
- restaurantId/orderId correlation
- health and metrics
- Dockerfile

Use MySQL 8 only.

Do not add Redis, PostgreSQL, Prometheus or Grafana.

Run all tests before finishing.
```

---

# Phase 5 — ETA Service

## Technology

```text
Python 3.12+
FastAPI
Pydantic
pytest
python-json-logger
```

## API

```http
POST /predict-eta
GET /health
GET /metrics-lite
```

## Input

```json
{
  "orderId": "uuid",
  "distanceKm": 2.4,
  "prepTimeMinutes": 20,
  "driverAvailability": 0.8,
  "trafficFactor": 1.2
}
```

## Output

```json
{
  "estimatedDeliveryMinutes": 31
}
```

Start with a deterministic formula.

Later you can replace it with ML.

## Antigravity prompt

```text
Antigravity, implement Phase 5: ETA Service.

Use:
- Python 3.12+
- FastAPI
- Pydantic
- pytest
- python-json-logger

Implement:

POST /predict-eta

Input:
orderId
distanceKm
prepTimeMinutes
driverAvailability
trafficFactor

Output:
estimatedDeliveryMinutes

Use a deterministic weighted formula.

Keep calculation logic isolated behind a clean service interface so a future
ML model can replace it without changing the REST API.

Implement:
GET /health
GET /metrics-lite

metrics-lite must expose:
requestCount
errorCount
avgLatencyMs

Track metrics in memory only.

Add:
- validation
- unit tests
- response contract test
- structured JSON logs
- orderId correlation
- Dockerfile

No Redis.
No PostgreSQL.
No Prometheus.
No Grafana.

Run pytest and verify the container builds.
```

## Checkpoint

```text
POST /predict-eta
        ↓
ETA returned
```

---

# Phase 6 — Assignment Service

This is the most technically interesting service.

## Technology

```text
Python
FastAPI
aiokafka
SQLAlchemy
MySQL 8
pytest
Testcontainers
python-json-logger
```

## Table

```text
driver_locations
```

```text
driver_id
location POINT SRID 4326
status
updated_at
```

## Location endpoint

```http
PATCH /drivers/{id}/location
```

## Spatial query

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

## Assignment

```text
order-created
      ↓
find 5 nearest drivers
      ↓
call ETA service
      ↓
score candidates
      ↓
select driver
      ↓
driver-assigned
```

## Failure

```text
No driver
   ↓
retry
   ↓
retry
   ↓
DLQ
```

## Antigravity prompt

```text
Antigravity, implement Phase 6: Assignment Service.

Inspect the repository and existing contracts first.

Technology:
- Python 3.12+
- FastAPI
- aiokafka
- SQLAlchemy
- MySQL 8
- pytest
- Testcontainers
- python-json-logger

Database:
assignment_db

Create driver_locations:

driver_id VARCHAR(36) PRIMARY KEY
location POINT SRID 4326 NOT NULL
status VARCHAR(...)
updated_at DATETIME(6)

Create a SPATIAL INDEX on location.

Implement:

PATCH /drivers/{id}/location

Request:
{
  "lat": number,
  "lng": number
}

Consume order-created.

For each order:
1. Read delivery latitude/longitude.
2. Find up to 5 nearest AVAILABLE drivers using MySQL 8
   ST_Distance_Sphere.
3. Call:
   http://eta-service:8000/predict-eta
4. Calculate a deterministic candidate score.
5. Select the best driver.
6. Publish driver-assigned using the existing JSON Schema.

Implement retry/backoff when no drivers are available.

After retry exhaustion publish:
driver-assignment-dlq

Add:
- Kafka error handling
- structured JSON logging
- orderId correlation
- /health
- /metrics-lite
- unit tests
- MySQL integration tests
- Kafka integration tests
- Dockerfile

Keep the spatial query isolated in its own repository/service component
so it can be benchmarked independently.

Important:
Do not claim the SPATIAL INDEX automatically makes nearest-distance
ORDER BY fast. Keep the implementation benchmarkable.

Do not introduce Redis, PostgreSQL, Prometheus or Grafana.

Run all tests.
```

## Checkpoint

Seed several drivers.

Then:

```text
order-created
      ↓
spatial search
      ↓
candidate drivers
      ↓
ETA
      ↓
best driver
      ↓
driver-assigned
```

---

# Phase 7 — Delivery Service

## Database

```text
deliveries
```

## States

```text
ASSIGNED
PICKED_UP
IN_TRANSIT
DELIVERED
```

## Kafka

Consume:

```text
driver-assigned
```

Publish:

```text
order-delivered
```

## REST

```http
GET /deliveries/{id}
PATCH /deliveries/{id}/status
```

## Antigravity prompt

```text
Antigravity, implement Phase 7: Delivery Service.

Technology:
- Java 21
- Spring Boot
- JPA
- MySQL 8
- Kafka
- Flyway
- Validation
- Actuator
- Testcontainers

Consume driver-assigned.

Create delivery:
status = ASSIGNED

Implement:
GET /deliveries/{id}
PATCH /deliveries/{id}/status

Valid state transitions:

ASSIGNED -> PICKED_UP
PICKED_UP -> IN_TRANSIT
IN_TRANSIT -> DELIVERED

Reject invalid transitions.

When status becomes DELIVERED:
publish order-delivered using the existing Kafka contract.

Add:
- persistence
- migrations
- unit tests
- integration tests
- Kafka tests
- structured JSON logging
- orderId correlation
- Actuator
- Dockerfile

Use MySQL 8 only.

Do not add Redis, PostgreSQL, Prometheus or Grafana.
```

---

# Phase 8 — Notification Service

Keep this lightweight.

## Consume

```text
order-created
order-delivered
```

## Initially

Just log:

```text
Notification:
Order 123 was created
```

and:

```text
Notification:
Order 123 was delivered
```

No external provider initially.

## Antigravity prompt

```text
Antigravity, implement Phase 8: Notification Service.

Build a minimal Kafka consumer service.

Consume:
- order-created
- order-delivered

For each event:
- validate the event
- create a structured notification log
- include orderId
- include event type
- include timestamp

No real email/SMS provider is required.

Keep this service intentionally small.

Add:
- Kafka consumer
- structured JSON logging
- health endpoint
- unit tests
- Dockerfile

Do not add a database unless required by the existing architecture.

Do not introduce Redis, PostgreSQL, Prometheus or Grafana.
```

---

# Phase 9 — API Composition

## Endpoint

```http
GET /orders/{id}/full-status
```

## Aggregation

Order Service:

```text
local order
     +
Restaurant Service
     +
Delivery Service
     +
ETA if available
```

## Important

Never do:

```text
Order Service → restaurant_db
```

Instead:

```text
Order Service
     ↓ REST
Restaurant Service
```

## Resilience4j

Add:

```text
timeout
circuit breaker
retry
fallback
```

## Antigravity prompt

```text
Antigravity, implement Phase 9.

Add:

GET /orders/{id}/full-status

The endpoint must aggregate information using REST calls to:
- local Order Service data
- Restaurant Service
- Delivery Service
- ETA Service where appropriate

Do not directly access another service's database.

Use WebClient or the appropriate modern Spring HTTP client.

Add Resilience4j:
- timeout
- circuit breaker
- carefully scoped retries
- fallback handling

Create a combined DTO.

Add tests for:
1. all services healthy
2. Restaurant Service unavailable
3. Delivery Service unavailable
4. ETA Service unavailable
5. timeout
6. circuit breaker

Do not change Kafka contracts.
Do not introduce Redis or other infrastructure.
```

---

# Phase 10 — Observability & Ops Dashboard

## Java

```http
/actuator/health
/actuator/metrics
```

## Python

```http
/health
/metrics-lite
```

## Dashboard

Use:

```text
Node.js
Express
MySQL
Chart.js
```

## Poll every

```text
10 seconds
```

## Database

```text
service_metric_snapshots
```

Columns:

```text
id
service_name
status
request_count
error_count
avg_latency_ms
captured_at
```

## Dashboard

Show:

```text
Service status
Request rate
Error rate
Latency
Last successful poll
```

## Order funnel

```text
CREATED
   ↓
ACCEPTED
   ↓
ASSIGNED
   ↓
DELIVERED
```

## Antigravity prompt

```text
Antigravity, implement Phase 10: Ops Dashboard.

Use:
- Node.js
- Express
- MySQL 8
- Chart.js

Database:
ops_db

Create:
service_metric_snapshots

Fields:
id
service_name
status
request_count
error_count
avg_latency_ms
captured_at

Every 10 seconds poll:

order-service
restaurant-service
delivery-service
eta-service
assignment-service

Java:
 /actuator/health
 /actuator/metrics

Python:
 /health
 /metrics-lite

Store snapshots in MySQL.

Create an internal HTML dashboard showing:
- health status per service
- request rate
- error rate
- average latency
- one-hour trend
- last poll time

Add an order funnel:
CREATED
ACCEPTED
ASSIGNED
DELIVERED

Use Chart.js.

No Prometheus.
No Grafana.
No Redis.

Also ensure Java/Python services have structured JSON logs
with orderId correlation when available.

Run tests and verify the dashboard container can communicate
with the application containers over delivery-net.
```

## Checkpoint

Kill one service.

Dashboard should show:

```text
🔴 UNHEALTHY
```

within the polling interval.

---

# Phase 11 — CI/CD

## Pull Request pipeline

```text
format-check
static-analysis
unit-tests
integration-tests
docker-build-check
security-scan
```

## Java

```text
Spotless
Checkstyle
SpotBugs
PMD
Maven test
```

## Python

```text
Black
Ruff
mypy
pytest
pip-audit
```

## Node

```text
ESLint
npm test
npm audit
```

## Security

```text
Trivy
OWASP Dependency Check
pip-audit
npm audit
```

## Release

Trigger:

```text
v1.0.0
v1.1.0
...
```

Build and push images.

## Antigravity prompt

```text
Antigravity, implement Phase 11 CI/CD.

Create:

.github/workflows/ci.yml

Trigger:
pull_request

Jobs:
1. format-check
2. static-analysis
3. unit-tests
4. integration-tests
5. docker-build-check
6. security-scan

Use matrix strategies where practical.

Java:
- Maven
- Spotless
- Checkstyle
- SpotBugs

Python:
- Black
- Ruff
- mypy
- pytest

Node:
- ESLint
- npm test

Integration:
- MySQL
- Kafka
- Testcontainers

Docker:
- build every service image
- do not push

Security:
- Trivy
- OWASP Dependency Check
- pip-audit
- npm audit

Create:

.github/workflows/release.yml

Trigger:
tags matching v*.*.*

Build and push all service images to a configurable registry.

Do not introduce Prometheus, Grafana, Redis or PostgreSQL.
```

---

# Phase 12 — Blue-Green Deployment

For this project, use **Docker Compose blue-green** unless Kubernetes is specifically one of your learning goals.

## Architecture

```text
                  nginx
                    │
            ┌───────┴───────┐
            │               │
          BLUE            GREEN
            │               │
         stack            stack
```

## Deployment

```text
Deploy Green
     ↓
Health checks
     ↓
Green healthy?
  ┌──┴──┐
 YES    NO
  │      │
  ▼      ▼
switch  rollback
traffic
  │
  ▼
stop Blue
```

## Antigravity prompt

```text
Antigravity, implement Phase 12 using Docker Compose blue-green deployment.

Create:
- blue environment
- green environment
- nginx reverse proxy
- deployment script

The deployment script must:

1. Start/deploy green.
2. Wait for startup.
3. Check every service health endpoint.
4. Java services must use /actuator/health.
5. Python services must use /health.
6. Only switch nginx traffic if all required services are healthy.
7. Verify the new environment after cutover.
8. Roll back automatically if verification fails.
9. Stop the old environment only after successful verification.

Make the script safe and idempotent where practical.

Do not introduce Kubernetes.

Do not change application architecture.
```

---

# Phase 13 — E2E, Load & Chaos Testing

This is the final validation phase.

# E2E happy path

```text
POST /orders
       ↓
order-created
       ↓
order-accepted
       ↓
driver-assigned
       ↓
PICKED_UP
       ↓
IN_TRANSIT
       ↓
DELIVERED
       ↓
order-delivered
       ↓
GET /orders/{id}/full-status
```

Expected final:

```json
{
  "status": "DELIVERED"
}
```

## E2E test

Use:

```text
JUnit
Testcontainers
or
Postman/Newman
```

## Antigravity prompt

```text
Antigravity, implement Phase 13 E2E validation.

Create an end-to-end test covering the complete order lifecycle:

1. Create order.
2. Verify order-created.
3. Verify Restaurant Service accepts it.
4. Verify driver assignment.
5. Verify delivery creation.
6. Transition delivery:
   ASSIGNED
   -> PICKED_UP
   -> IN_TRANSIT
   -> DELIVERED
7. Verify order-delivered.
8. Call GET /orders/{id}/full-status.
9. Assert final status is DELIVERED.

Use the real Docker/Testcontainers infrastructure where practical.

Do not mock the Kafka flow for the primary E2E test.

Add cleanup and deterministic test data.
```

---

# Load Testing

Use:

```text
k6
```

Test:

```text
10 drivers
25 drivers
50 drivers
100 drivers
```

Measure:

```text
POST /orders latency
Kafka event processing latency
driver assignment latency
ETA latency
delivery completion latency
full-status latency
```

Most importantly:

```text
driver location writes
        vs
nearest-driver reads
```

Measure:

```text
p50
p95
p99
throughput
error rate
```

## Antigravity prompt

```text
Antigravity, add a k6 load-testing suite for Phase 13.

Test:
- order creation
- order processing
- driver location updates

Create scenarios for:
10 drivers
25 drivers
50 drivers
100 drivers

Measure:
- p50
- p95
- p99
- throughput
- error rate

Specifically capture the performance of the MySQL spatial
nearest-driver query under concurrent location writes.

Do not introduce Redis as part of the test.

Produce a concise performance report template in docs/.
```

---

# Chaos Testing

Test:

### Restaurant failure

```text
kill restaurant-service
```

Verify:

```text
orders aren't silently lost
```

### Assignment failure

```text
kill assignment-service
```

Verify:

```text
Ops Dashboard → unhealthy
Kafka events remain available
service recovers
```

### Delivery failure

```text
kill delivery-service
```

Verify:

```text
order doesn't incorrectly become DELIVERED
```

### ETA failure

```text
kill eta-service
```

Verify:

```text
assignment handles downstream failure
```

## Antigravity prompt

```text
Antigravity, create a chaos-testing checklist and scripts.

Test failure of:
- restaurant-service
- assignment-service
- eta-service
- delivery-service

For each failure:
1. Start normal order processing.
2. Kill the target service at an appropriate point.
3. Verify expected degradation.
4. Restart the service.
5. Verify recovery.
6. Verify no incorrect order state.
7. Verify Kafka events are not silently lost.
8. Verify Ops Dashboard detects the outage.

Document the expected behavior and actual result in docs/chaos-testing.md.
```

---

# Final Repository

At the end:

```text
food-delivery-platform/
│
├── services/
│   ├── order-service/
│   ├── restaurant-service/
│   ├── delivery-service/
│   ├── eta-service/
│   ├── assignment-service/
│   ├── notification-service/
│   └── ops-dashboard/
│
├── infra/
│   ├── docker-compose.yml
│   ├── docker-compose.blue.yml
│   ├── docker-compose.green.yml
│   │
│   ├── mysql/
│   │   └── init/
│   │
│   ├── nginx/
│   │   └── nginx.conf
│   │
│   └── logging/
│       ├── logback-json.xml
│       └── python-logging.json
│
├── docs/
│   ├── roadmap.md
│   ├── architecture.md
│   ├── decisions.md
│   ├── chaos-testing.md
│   ├── performance-report.md
│   │
│   ├── kafka-contracts/
│   │   ├── order-created.json
│   │   ├── order-accepted.json
│   │   ├── driver-assigned.json
│   │   └── order-delivered.json
│   │
│   └── api/
│       ├── order-service.yaml
│       ├── restaurant-service.yaml
│       ├── delivery-service.yaml
│       ├── eta-service.yaml
│       ├── assignment-service.yaml
│       └── ops-dashboard.yaml
│
├── tests/
│   └── e2e/
│
├── scripts/
│   ├── seed-drivers.sh
│   ├── deploy-blue-green.sh
│   └── chaos-test.sh
│
├── .github/
│   └── workflows/
│       ├── ci.yml
│       └── release.yml
│
├── .gitignore
├── README.md
└── LICENSE
```

# Complete Build Order

```text
PHASE 0
Repository + documentation
        ↓
PHASE 1
Kafka + REST contracts
        ↓
PHASE 2
MySQL + Kafka infrastructure
        ↓
PHASE 3
Order Service
        ↓
PHASE 4
Restaurant Service
        ↓
PHASE 5
ETA Service
        ↓
PHASE 6
Assignment Service
        ↓
PHASE 7
Delivery Service
        ↓
PHASE 8
Notification Service
        ↓
PHASE 9
API Composition
        ↓
PHASE 10
Ops Dashboard + logging
        ↓
PHASE 11
CI/CD
        ↓
PHASE 12
Blue-Green Deployment
        ↓
PHASE 13
E2E + Load + Chaos
```

# Suggested Timeline

| Week  | Phases | Main Result                             |
| ----- | ------ | --------------------------------------- |
| **1** | 0–2    | Repo + contracts + infrastructure       |
| **2** | 3–4    | Order + Restaurant                      |
| **3** | 5–6    | ETA + Assignment                        |
| **4** | 7–9    | Delivery + Notification + Composition   |
| **5** | 10–11  | Observability + CI                      |
| **6** | 12–13  | Deployment + testing + portfolio polish |

### Most important rule when using Antigravity

Don't tell it:

> "Build Phase 6."

Instead tell it:

> **"Implement Phase 6A: create the driver location repository and MySQL spatial migration. Do not implement Kafka or assignment logic yet."**

Then verify it.

Then:

> **"Implement Phase 6B: add the location update API."**

Then verify it.

Then:

> **"Implement Phase 6C: add nearest-driver querying."**

Then benchmark/test it.

Then:

> **"Implement Phase 6D: integrate ETA and driver selection."**

This keeps Antigravity from making a huge number of architectural decisions in one pass and makes the project much easier to debug.
