# Pivot Banking System - Credit Microservice (`pivot-ms-credit`)

## Overview
Reactive microservice responsible for managing **Active Bank Products** (**Personal Loans**, **Business Loans**, **Personal Credit Cards**, and **Business Credit Cards**). It enforces credit ownership rules per customer type, processes credit consumption charges (`CONSUMPTION`) and debt payments (`PAYMENT`), provides active credit card verification for `ms-account` (`VIP`/`PYME` eligibility), and protects sensitive contract/card numbers with **AES-256-GCM encryption** and **SHA-256 blind indexing**.

## Architecture, Design Patterns & Quality Gates
- **Architectural Style:** Hexagonal Architecture (Ports and Adapters) separating `domain`, `application`, and `infrastructure` layers.
- **Reactive Stack:** Java 17 + Spring Boot 4.1.2 + Spring WebFlux (Netty) + **RxJava 3** (`Single`, `Maybe`, `Flowable`, `Completable`) + Netflix Eureka Client + **Resilience4j**.
- **SOLID & Design Patterns (Zero God Classes):**
  - **Single Responsibility Principle (SRP):** Pure reactive orchestrators in `application/usecases/` (`CreateCreditUseCaseImpl`, `RetrieveCreditUseCaseImpl`, `FindCreditsByIdUseCaseImpl`, `UpdateCreditUseCaseImpl`, `CreditBalanceUseCaseImpl`, `DeleteCreditUseCaseImpl`).
  - **Domain Enricher / Factory Pattern (`CreditDomainInitializer`):** Located in `application/support/`, encapsulating contract/card number generation, SHA-256 hashing, default interest rate resolution, and initial loan/card balances.
  - **Strategy Pattern for Validation & Operations (OCP):**
    - Validation: `PersonalCreditStrategy` (allows only one active `PERSONAL_LOAN` per person) and `BusinessCreditStrategy` (allows multiple business loans and corporate cards).
    - Operations: `ConsumptionOperationStrategy` (validates available credit limit on credit cards) and `PaymentOperationStrategy` (reduces debt `usedAmount` and replenishes revolving `availableAmount`).
  - **Rich Enum Pattern (`CreditType`):** Encapsulates number prefixes (`LN-`, `4557-8800-`) and product classification (`isLoan()`, `isCreditCard()`, `isBusinessProduct()`).
  - **Circuit Breaker Pattern (`Resilience4j`):** Protects outbound calls to `ms-customer` in `CustomerWebClientAdapter` with a **2-second timeout**.
- **Code Quality & Testing:**
  - **Google Java Style (`maven-checkstyle-plugin`):** Enforced during Maven `validate` phase.
  - **Unit Testing (`JUnit 5` + `Mockito` + `RxJava 3 TestObserver` + `StepVerifier`):** AAA pattern and FIRST principles achieving **>90% code coverage** in **JaCoCo** and **SonarQube**.

## Security & Secrets (`HashiCorp Vault`)
- **Vault Path:** `secret/pivot-bank`
- **Required Secret Key:** `aes-secret-key` (AES-256-GCM encryption at rest and SHA-256 blind indexing).
- **Mask-on-Read Presentation:** Full `creditNumber` is returned **only once** upon creation (`POST /credits`). All subsequent reads mask the number (`************1234`) via `DataMaskingUtil`.

## API Endpoints (`/api/v1/credits`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/credits` | Grant a loan or issue a credit card (returns unmasked number once) |
| `GET` | `/api/v1/credits` | Retrieve all credit products (masked numbers) |
| `GET` | `/api/v1/credits/{id}` | Retrieve credit product by ID |
| `GET` | `/api/v1/credits/number/{creditNumber}` | Retrieve credit product by number via SHA-256 blind index |
| `GET` | `/api/v1/credits/customer/{customerId}` | Retrieve all credit products belonging to a customer (used by `ms-account` for `VIP`/`PYME` validation) |
| `PUT` | `/api/v1/credits/{id}` | Update credit limit, interest rate, or status |
| `GET` | `/api/v1/credits/{id}/balance` | Consult available credit limit and current debt (`usedAmount`) |
| `PATCH` | `/api/v1/credits/{id}/balance` | Process a `PAYMENT` or `CONSUMPTION` operation |
| `DELETE` | `/api/v1/credits/{id}` | Soft delete (`status = CLOSED`) only when `usedAmount == 0.00` |

## Inter-Service Communication & Observability
- **`pivot-ms-config` (`:8888`):** Fetches externalized rules (`CreditRulesProperties`).
- **`pivot-ms-eureka` (`:8761`):** Registers service instance as `MS-CREDIT`.
- **`pivot-ms-customer` (`:8081`):** Consumed via `WebClient` + `Resilience4j` (2s timeout) to verify customer eligibility.
- **Consumed by:** `pivot-ms-account` (`:8082`) to check active credit cards and `pivot-ms-transaction` (`:8084`) to process payments and credit card consumptions.
- **Observability:** Structured logs with `X-Correlation-Id` exported to **Promtail -> Loki -> Grafana (`:3000`)**.

## Database (Database per Service Pattern)
- **Engine:** MongoDB 7.0 (Reactive Spring Data MongoDB using derived query methods, zero `@Query`).
- **Database Name:** `db_pivot_credit`
- **Collection:** `credits`

## How to Run (Local & Docker)
1. Ensure MongoDB (`:27017`), Vault (`:8200`), `ms-config` (`:8888`), `ms-eureka` (`:8761`), and `ms-customer` (`:8081`) are running.
2. Run unit tests, Checkstyle, and generate the JaCoCo coverage report:
   ```powershell
   mvn clean test checkstyle:check jacoco:report
   ```
3. Start the microservice locally:
   ```powershell
   mvn spring-boot:run
   ```
4. Build and run as an independent Docker container:
   ```powershell
   docker build -t pivot-ms-credit:2.0.0 .
   docker run -d -p 8083:8083 --name pivot-ms-credit pivot-ms-credit:2.0.0
   ```