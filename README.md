# Pivot Banking System - Credit Microservice (`pivot-ms-credit`)

## Overview
Reactive microservice (`:8083`) responsible for managing **Active Bank Products** (**Personal Loans**, **Business Loans**, **Personal Credit Cards**, and **Business Credit Cards**). It enforces credit ownership rules per customer type, processes credit consumptions (`CONSUMPTION`) and debt payments (`PAYMENT`), manages **`OVERDUE` debt lifecycle and automatic recovery to `ACTIVE` upon full settlement** (Phase III), serves active credit card and overdue-debt checks for `ms-account`, and protects sensitive contract/card numbers with **AES-256-GCM encryption** and **SHA-256 blind indexing**.

## Architecture, Design Patterns & Quality Gates
- **Architectural Style:** Hexagonal Architecture (Ports and Adapters) separating `domain`, `application`, and `infrastructure` layers.
- **Reactive Stack:** Java 17 + Spring Boot `4.1.2` + Spring Cloud `2025.1.4` + Spring WebFlux (Netty) + **RxJava 3 (`3.1.12`)** (`Single`, `Maybe`, `Flowable`, `Completable`) + `reactor-adapter` + Reactive Spring Data MongoDB + Spring Boot Actuator + Netflix Eureka Client + **Resilience4j**.
- **Strong Consistency Design Decision (No Redis Cache):** Intentionally omits Redis caching for credit balances and debt statuses so `OVERDUE` checks, credit limits, and debt payments always read strongly consistent data directly from MongoDB.
- **SOLID & Design Patterns (Zero God Classes):**
  - **Single Responsibility Principle (SRP):** Pure reactive orchestrators in `application/usecases/` (`CreateCreditUseCaseImpl`, `RetrieveCreditUseCaseImpl`, `FindCreditsByIdUseCaseImpl`, `UpdateCreditUseCaseImpl`, `CreditBalanceUseCaseImpl`, `DeleteCreditUseCaseImpl`).
  - **Domain Enricher / Factory Pattern (`CreditDomainInitializer`):** Encapsulates contract/card number generation, SHA-256 hashing, default interest rate resolution, and initial loan/card balances.
  - **Strategy Pattern for Validation & Operations (OCP):**
    - Validation: `PersonalCreditStrategy` (single active `PERSONAL_LOAN` per person, blocks new credit when `OVERDUE` debt exists) and `BusinessCreditStrategy` (multiple business loans and corporate cards, blocks new credit when `OVERDUE` debt exists).
    - Operations (Phase III `OVERDUE` lifecycle): `ConsumptionOperationStrategy` rejects new charges on `OVERDUE` or non-`ACTIVE` products; `PaymentOperationStrategy` accepts payments on both `ACTIVE` and `OVERDUE` credits (including third-party payments from `ms-transaction`) and restores `OVERDUE` credit cards/loans cleanly once debt is settled.
  - **Rich Enum Pattern (`CreditType`):** Encapsulates number prefixes (`LN-`, `4557-8800-`) and product classification (`isLoan()`, `isCreditCard()`, `isBusinessProduct()`).
  - **Circuit Breaker Pattern (`Resilience4j`):** Protects outbound calls to `ms-customer` in `CustomerWebClientAdapter` with a **2-second timeout**.
- **Code Quality & Testing:**
  - **Google Java Style (`maven-checkstyle-plugin`):** Enforced during Maven `validate` phase.
  - **Unit Testing (`JUnit 5` + `Mockito` + `RxJava 3 TestObserver` + `StepVerifier`):** AAA pattern and FIRST principles achieving **~97.6% instruction coverage** verified by **JaCoCo (`0.8.15`)** (minimum gate: 90%).

## Security & Secrets (`HashiCorp Vault`)
- **Vault Path:** `secret/pivot-bank`
- **Required Secret Key:** `aes-secret-key` (AES-256-GCM encryption at rest and SHA-256 blind indexing).
- **Mask-on-Read Presentation:** Full `creditNumber` is returned **only once** upon creation (`POST /credits`). All subsequent reads mask the number (`************1234`) via `DataMaskingUtil`.
- **Gateway RBAC:** All routes require a valid Bearer JWT via `ms-gateway`; `DELETE` requires `ROLE_ADMIN`.

## API Endpoints (`/api/v1/credits`)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/credits` | Grant a loan or issue a credit card (blocks customers with existing `OVERDUE` debt; returns unmasked number once) |
| `GET` | `/api/v1/credits` | Retrieve all credit products (masked numbers) |
| `GET` | `/api/v1/credits/{id}` | Retrieve credit product by ID |
| `GET` | `/api/v1/credits/number/{creditNumber}` | Retrieve credit product by number via SHA-256 blind index |
| `GET` | `/api/v1/credits/customer/{customerId}` | Retrieve all credit products belonging to a customer (used by `ms-account` for `VIP`/`PYME` and `OVERDUE` checks) |
| `PUT` | `/api/v1/credits/{id}` | Update credit limit, interest rate, or status (including `OVERDUE` status transitions) |
| `GET` | `/api/v1/credits/{id}/balance` | Consult available credit limit and current debt (`usedAmount`) |
| `PATCH` | `/api/v1/credits/{id}/balance` | Process a `PAYMENT` (allowed on `ACTIVE` and `OVERDUE`) or `CONSUMPTION` (`ACTIVE` credit cards only) |
| `DELETE` | `/api/v1/credits/{id}` | Soft delete (`status = CLOSED`) only when `usedAmount == 0.00` |

## Inter-Service Communication & Observability
- **`pivot-ms-config` (`:8888`):** Fetches externalized rules (`CreditRulesProperties`).
- **`pivot-ms-eureka` (`:8761`):** Registers service instance as `MS-CREDIT`.
- **`pivot-ms-customer` (`:8081`):** Consumed via `WebClient` + `Resilience4j` (2s timeout) to verify customer eligibility.
- **Consumed by:** `pivot-ms-account` (`:8082`) for `VIP`/`PYME` active credit card and `OVERDUE` debt checks, and `pivot-ms-transaction` (`:8084`) for credit payments, third-party credit payments, and credit card consumptions.
- **Observability:** Exposes `/actuator/health/liveness` & `/actuator/health/readiness` and ships structured logs via **Promtail -> Loki -> Grafana (`:3000`)**.

## Database (Database per Service Pattern)
- **Engine:** MongoDB 7.0 (Reactive Spring Data MongoDB using derived query methods, zero `@Query`, configured via `spring.mongodb.uri`).
- **Database Name:** `db_pivot_credit`
- **Collection:** `credits`

## How to Run (Local, Docker & Kubernetes)
1. Ensure MongoDB (`:27017`), Vault (`:8200`), `ms-config` (`:8888`), `ms-eureka` (`:8761`), and `ms-customer` (`:8081`) are running.
2. Run unit tests, Checkstyle validation, and verify the 90% JaCoCo coverage gate:
   ```powershell
   mvn clean verify
   ```
3. Start the microservice locally (port `8083`):
   ```powershell
   mvn spring-boot:run
   ```
4. Build and run as a Docker container (or deploy via `ENVIROMENT/k8s/05-microservices.yaml` in namespace `pivot-banking`):
   ```powershell
   docker build -t pivot/ms-credit:1.0.0 .
   docker run -d -p 8083:8083 --name pivot-ms-credit pivot/ms-credit:1.0.0
   ```
