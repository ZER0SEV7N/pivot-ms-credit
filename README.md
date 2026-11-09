# Pivot Banking System - Credit Microservice (`pivot-ms-credit`)

## Overview
Reactive microservice responsible for managing **Active Bank Products** (**Personal Loans**, **Business Loans**, **Personal Credit Cards**, and **Business Credit Cards**). It validates customer eligibility reactively against `ms-customer`, enforces product ownership rules, processes credit payments and credit card consumptions, and manages logical lifecycle states (`ACTIVE`, `BLOCKED`, `PAID_OFF`, `CLOSED`).

## Architecture, Design Patterns & Security Highlights
- **Architectural Style:** Hexagonal Architecture (Ports and Adapters) with strict separation of `domain`, `application`, and `infrastructure` layers.
- **Reactive Stack:** Spring Boot 4 + Spring WebFlux (Netty) + **RxJava 3** (`Single`, `Maybe`, `Flowable`, `Completable`).
- **SOLID & Design Patterns (Low Cognitive Complexity):**
  - **Single Responsibility Principle (SRP):** Dedicated Use Case classes (`CreateCreditUseCaseImpl`, `RetrieveCreditUseCaseImpl`, `FindCreditByIdUseCaseImpl`, `UpdateCreditUseCaseImpl`, `CreditBalanceUseCaseImpl`, `DeleteCreditUseCaseImpl`).
  - **Double Strategy Pattern (OCP):**
    1. **Customer Ownership Strategy (`CreditValidationStrategy`):** `PersonalCreditStrategy` (enforces max 1 active personal loan) and `BusinessCreditStrategy` (allows multiple corporate loans).
    2. **Financial Operation Strategy (`CreditOperationStrategy`):** `ConsumptionOperationStrategy` and `PaymentOperationStrategy` to keep cognitive complexity under 5 in SonarQube.
  - **Rich Enum Pattern (`CreditType`):** Encapsulates product prefixes (`LN-`, `4557-8800-`), loan vs. revolving card behavior, and customer eligibility flags.
  - **Immutable DTOs:** Java 17 `record` types across all web and client payloads.
- **Banking-Grade Security:**
  - **AES-256-GCM Encryption at Rest:** Credit and card numbers are encrypted with a 12-byte random IV and 128-bit authentication tag before persisting in MongoDB.
  - **SHA-256 Blind Indexing:** Exact credit/card number lookups (`GET /credits/number/{creditNumber}`) use an irreversible SHA-256 hash (`creditNumberHash`) without exposing plain text or leaking the hash in API responses.
  - **Mask-on-Read Presentation:** Full `creditNumber` is returned **only once** upon product creation (`POST /credits`). All subsequent queries (`GET`, `PUT`, `PATCH`) and console logs mask the number (`************5678`).
  - **HashiCorp Vault Integration:** Cryptographic secret `aes-secret-key` is injected at runtime from Vault (`secret/pivot-bank`).
  - **Soft Delete & State Machine:** `DELETE /credits/{id}` performs a logical closure (`status = CLOSED`) only when `usedAmount == 0.00` (zero outstanding debt). Full payment on a loan automatically transitions its status to `PAID_OFF`.

## API Endpoints (`/api/v1/credits`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/credits` | Grant a new loan or issue a credit card (returns unmasked number once) |
| `GET` | `/api/v1/credits` | Retrieve all credit products (masked numbers) |
| `GET` | `/api/v1/credits/{id}` | Retrieve credit product by ID (masked number) |
| `GET` | `/api/v1/credits/number/{creditNumber}` | Retrieve credit product by number via SHA-256 blind index |
| `GET` | `/api/v1/credits/customer/{customerId}` | Retrieve all credit products belonging to a customer |
| `PUT` | `/api/v1/credits/{id}` | Update credit terms (`interestRate`, `paymentDueDay`, or `BLOCKED`/`ACTIVE` status) |
| `GET` | `/api/v1/credits/{id}/balance` | Consult credit limit, outstanding debt (`usedAmount`), and `availableAmount` |
| `PATCH` | `/api/v1/credits/{id}/balance` | Process a `PAYMENT` or credit card `CONSUMPTION` (consumed by `ms-t1ransaction`) |
| `DELETE` | `/api/v1/credits/{id}` | Soft delete (`status = CLOSED`) if and only if `usedAmount == 0.00` |

## Inter-Service Communication
- **`pivot-ms-config` (`:8888`):** Fetches externalized configuration and default interest rates (`CreditRulesProperties`).
- **HashiCorp Vault (`:8200`):** Retrieves cryptographic keys (`aes-secret-key`).
- **`pivot-ms-customer` (`:8081`):** Consumed via non-blocking `WebClient` to verify customer existence, active status, and type (`PERSONAL` vs. `BUSINESS`).
- **`pivot-ms-transaction` (`:8084`):** Consumes `GET /credits/{id}/balance` and `PATCH /credits/{id}/balance`.

## Database (Database per Service Pattern)
- **Engine:** MongoDB 7 (Reactive Spring Data MongoDB using derived query methods, zero `@Query`).
- **Database Name:** `db_pivot_credit`
- **Collection:** `credits`

## How to Run
1. Ensure MongoDB (`:27017`), HashiCorp Vault (`:8200`), `ms-config` (`:8888`), and `ms-customer` (`:8081`) are running.
2. Create your local `.env` file in the project root (based on `.env.example`) containing your `VAULT_URI` and `VAULT_TOKEN`.
3. Start the microservice:
```powershell
   mvn clean spring-boot:run
```
4. The service will start on port *8083*.
5. API Testing: Import *pivot-ms-credit.postman_collection.json* into Postman to run all test scenarios.

---