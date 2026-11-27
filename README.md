# PayFlow – FinTech Payment Processing & Reconciliation

Java 17 / Spring Boot multi-module learning project.

## Services

| Module | Default port | Responsibility |
|---|---:|---|
| payment-service | 8081 | Payment API, idempotency and orchestration |
| account-service | 8082 | Accounts, balance reservation and locking |
| ledger-service | 8083 | Double-entry ledger |
| bank-simulator | 8084 | Simulated external banking partner |
| reconciliation-service | 8085 | Internal-vs-bank reconciliation |

## Prerequisites

- JDK 17
- Maven 3.9+
- Docker Desktop / Docker Engine with Compose

## Start infrastructure

From the repository root:

```bash
docker compose -f infrastructure/docker-compose.yml up -d
```

## Build

```bash
mvn clean package
```

## Run a service

In separate terminals:

```bash
mvn -pl account-service spring-boot:run
mvn -pl ledger-service spring-boot:run
mvn -pl bank-simulator spring-boot:run
mvn -pl reconciliation-service spring-boot:run
mvn -pl payment-service spring-boot:run
```

## UI

Start all five services (including `swagger-gateway`, port 8080), then open **http://localhost:8080/** — a console to create accounts, transfer money, and view balances, payments and ledger entries. Swagger is at http://localhost:8080/swagger-ui.html.

```bash
mvn -pl swagger-gateway spring-boot:run
```

## First API call

Create an account:

```bash
curl -X POST http://localhost:8082/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"accountId":"ACC001","ownerName":"Alice","openingBalance":20000.00,"currency":"INR"}'
```

Create another account:

```bash
curl -X POST http://localhost:8082/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"accountId":"ACC002","ownerName":"Bob","openingBalance":10000.00,"currency":"INR"}'
```

Create a payment:

```bash
curl -X POST http://localhost:8081/api/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: transfer-10001" \
  -d '{"fromAccount":"ACC001","toAccount":"ACC002","amount":5000.00,"currency":"INR"}'
```

## Important implementation notes

This is an educational starter scaffold, not production banking software. It provides service boundaries, persistence, Kafka wiring, Docker infrastructure and starter APIs. The advanced guarantees are intentionally milestones:

1. Add database-enforced idempotency and payload-hash conflict handling.
2. Implement account reservation with pessimistic/optimistic locking.
3. Implement transactional outbox and consumer inbox/deduplication.
4. Implement Saga state machine and compensating actions.
5. Implement immutable double-entry journal posting.
6. Implement bank timeout/unknown-outcome simulation.
7. Implement reconciliation batches and exception resolution.
8. Add Testcontainers, observability, authentication and operational hardening.

Do not use this project to move real money or connect to a real bank.
