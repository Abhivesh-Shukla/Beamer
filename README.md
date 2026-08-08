# Food Delivery Order Processing Platform

A distributed, event-driven microservices platform built for high-throughput food delivery order management, spatial driver assignment, and real-time order tracking.

---

## 🏛️ Architectural Principles & Constraints

- **Storage**: MySQL 8 strictly across all services (`order_db`, `restaurant_db`, `delivery_db`, `assignment_db`, `ops_db`).
- **Spatial Engine**: MySQL 8 native spatial `POINT SRID 4326` with `SPATIAL INDEX` for driver geo-location matching (No Redis / Redis GEO).
- **Messaging**: Apache Kafka in KRaft mode (No ZooKeeper).
- **Observability**: Custom Node.js/Express Ops Dashboard polling service metrics & rendering order funnels (No Prometheus / Grafana).
- **Isolation**: Strict database-per-service isolation.
- **Logging**: Structured JSON logging with order ID & trace correlation.

---

## 📁 Repository Structure

```text
food-delivery-platform/
├── services/
│   ├── order-service/         # Java 21 / Spring Boot (order lifecycle & REST composition)
│   ├── restaurant-service/    # Java 21 / Spring Boot (restaurant menu & prep time validation)
│   ├── delivery-service/      # Java 21 / Spring Boot (delivery tracking & status transitions)
│   ├── eta-service/           # Python 3.12+ / FastAPI (delivery time estimation engine)
│   ├── assignment-service/    # Python 3.12+ / FastAPI (MySQL spatial nearest-driver matching)
│   ├── notification-service/  # Java/Python (event notification processor)
│   └── ops-dashboard/         # Node.js / Express / Chart.js (custom metrics dashboard)
├── infra/
│   ├── docker-compose.yml     # Core infrastructure definitions (MySQL, Kafka KRaft)
│   ├── mysql/init/            # Database initialization SQL scripts
│   └── logging/               # Logback JSON & Python logging configurations
├── docs/
│   ├── roadmap.md             # Complete Phase 0–13 development roadmap
│   ├── architecture.md        # Technical architecture and event streams
│   ├── decisions.md           # Architecture Decision Records (ADRs 001–005)
│   ├── kafka-contracts/       # Event JSON schemas (order-created, driver-assigned, etc.)
│   └── api/                   # OpenAPI specs per service
├── tests/
│   └── e2e/                   # End-to-end multi-service test suites
├── scripts/                   # Deployment, seeding, and chaos testing scripts
└── .github/workflows/         # CI/CD workflows for PR testing and image release
```

---

## 🚀 Services Matrix

| Service | Stack | Port | Database | Primary Responsibility |
| :--- | :--- | :--- | :--- | :--- |
| **Order Service** | Java 21 / Spring Boot | `8081` | `order_db` | Order creation, state management, API composition |
| **Restaurant Service** | Java 21 / Spring Boot | `8082` | `restaurant_db` | Restaurant & menu management, order acceptance |
| **Delivery Service** | Java 21 / Spring Boot | `8083` | `delivery_db` | Delivery state tracking (`ASSIGNED` → `DELIVERED`) |
| **ETA Service** | Python / FastAPI | `8000` | *None* | Delivery ETA prediction engine |
| **Assignment Service** | Python / FastAPI | `8001` | `assignment_db` | Driver location updates & MySQL spatial search |
| **Notification Service**| Java / Python | `8084` | *None* | Consumer for order notifications |
| **Ops Dashboard** | Node / Express | `3000` | `ops_db` | Metric snapshot poller & monitoring UI |

---

## 📚 Documentation Links

- 📖 [Full Roadmap](docs/roadmap.md)
- 🏗️ [Architecture Specification](docs/architecture.md)
- 📝 [Architecture Decision Records (ADRs)](docs/decisions.md)

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
