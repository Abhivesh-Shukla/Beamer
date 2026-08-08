# Architecture Decision Records (ADRs)

This document tracks the foundational architectural decisions made for the Food Delivery Order Processing Platform.

---

## ADR-001: MySQL 8 as Standard Relational Database everywhere

### Status
Accepted

### Context
Distributed microservice platforms often introduce multiple database engines (e.g., PostgreSQL for relational data, MongoDB for documents). This increases operational complexity, licensing/maintenance overhead, and infrastructure requirements.

### Decision
We will standardise strictly on **MySQL 8** for all relational persistence across all microservices (`order_db`, `restaurant_db`, `delivery_db`, `assignment_db`, `ops_db`). We will not introduce PostgreSQL or other database engines.

### Consequences
- **Positive**: Simplified container orchestration, unified backup/restore strategy, consistent Flyway/SQLAlchemy schema migration patterns.
- **Negative**: Must rely on MySQL 8 specific features (e.g., Spatial functions) rather than engine-specific alternatives.

---

## ADR-002: MySQL 8 Spatial Engine instead of Redis GEO for Driver Locations

### Status
Accepted

### Context
Spatial proximity queries (finding nearest available delivery drivers to a restaurant/customer coordinate) are typically implemented using Redis GEO or PostgreSQL PostGIS. However, adding Redis adds another memory caching store to manage and synchronize.

### Decision
We will use **MySQL 8 native Spatial data types** (`POINT SRID 4326`) with `SPATIAL INDEX` and `ST_Distance_Sphere` functions within `assignment_db` to store driver locations and perform nearest-driver searches. Redis will not be used.

### Consequences
- **Positive**: Eliminates Redis infrastructure dependency; location state persists reliably across container restarts without separate cache warming routines.
- **Negative**: MySQL spatial nearest-neighbor search performance under high concurrent location write loads must be carefully indexed and load-tested (benchmark target in Phase 13).

---

## ADR-003: Kafka KRaft Mode instead of ZooKeeper

### Status
Accepted

### Context
Apache Kafka historically required an external Apache ZooKeeper cluster for metadata management, topic configuration, and controller election, adding overhead and failure modes.

### Decision
We will deploy **Apache Kafka in KRaft (Kafka Raft Metadata) mode**, running a self-managed quorum without ZooKeeper.

### Consequences
- **Positive**: Simplified Docker Compose topology, reduced resource consumption, faster broker startup and controller election.
- **Negative**: Requires modern Kafka image configuration without ZooKeeper parameters.

---

## ADR-004: Custom Ops Dashboard instead of Prometheus and Grafana

### Status
Accepted

### Context
Standard observability stacks deploy Prometheus for scraping metrics and Grafana for rendering visual dashboards. For this platform, deploying heavyweight monitoring agents adds unnecessary infrastructure friction.

### Decision
We will build a **custom Ops Dashboard service** using Node.js, Express, MySQL (`ops_db`), and Chart.js. Java services will expose Spring Actuator endpoints (`/actuator/health`, `/actuator/metrics`), while Python services will expose custom endpoints (`/health`, `/metrics-lite`). The dashboard will poll these endpoints every 10 seconds, record snapshot metrics, and display service health and order funnels.

### Consequences
- **Positive**: Full control over metric schema, zero external monitoring agents required, lightweight web interface tailored to the domain funnel.
- **Negative**: Metric aggregation and retention logic must be explicitly managed within the custom service.

---

## ADR-005: Strict Database-per-Service Pattern

### Status
Accepted

### Context
In distributed microservices, sharing databases between services creates tight coupling, prevents independent schema evolution, and causes subtle transactional side-effects.

### Decision
Each microservice owns its dedicated database schema (`order_db`, `restaurant_db`, `delivery_db`, `assignment_db`, `ops_db`). No service is allowed to read from or write to another service's database directly.

### Consequences
- **Positive**: Complete loose coupling, clear data ownership, ability to scale and migrate service databases independently.
- **Negative**: Cross-domain queries require asynchronous event propagation via Kafka or synchronous API composition (e.g., `GET /orders/{id}/full-status`).
