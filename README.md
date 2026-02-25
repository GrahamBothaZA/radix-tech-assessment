# Technical Assessment: Loan Payment System

A Spring Boot REST API for managing loans and processing payments against them. Uses an in-memory H2 database.

## Prerequisites

- Java 21+
- Maven 3.6+

## Running the Application

```bash
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.

## H2 Console

The in-memory database can be inspected via the H2 web console at:

```
http://localhost:8080/h2-console
```

| Field  |Value|
|---|---|
|JDBC URL| `jdbc:h2:mem:loan-payment-db`|
|Username|`admin`|
|Password|`password`|

---

## API Endpoints

### Loans

#### Create a Loan

```
POST /loans
```

|Parameter|Type|Description|
|---|---|---|
|`loanAmount`|Double|Principal amount of the loan|
|`term`|Integer|Repayment term in months|

**Example:**

```bash
curl -X POST "http://localhost:8080/loans?loanAmount=10000&term=12"
```

**Response `201 Created`:**

```json
{
  "loanId": "LOAN_1A2B3C4D",
  "loanAmount": 10000.0,
  "term": 12,
  "status": "ACTIVE",
  "createdDate": "2026-02-25T10:00:00Z"
}
```

**Response `400 Bad request`** — returned when invalid loan data is provided.

---

#### Get a Loan

```
GET /loans
```

| Parameter | Type   | Description                    |
|-----------|--------|--------------------------------|
| `loanId`  | String | Unique identifier of the loan  |

**Example:**

```bash
curl "http://localhost:8080/loans?loanId=LOAN_1A2B3C4D"
```

**Response `200 OK`:**

```json
{
  "loanId": "LOAN_1A2B3C4D",
  "loanAmount": 10000.0,
  "term": 12,
  "status": "ACTIVE",
  "createdDate": "2026-02-25T10:00:00Z"
}
```

**Response `404 Not Found`** — returned when no loan exists with the given ID.

---

### Payments

#### Make a Payment

```
POST /payments
```

|Parameter|Type|Description|
|---|---|---|
|`loanId`|String|Unique identifier of the loan to pay|
|`paymentAmount`|Double|Amount to pay|

The payment must not exceed the outstanding balance (loan amount minus all prior payments).
Payments cannot be made against a loan that has already been fully settled.

**Example:**

```bash
curl -X POST "http://localhost:8080/payments?loanId=LOAN_1A2B3C4D&paymentAmount=2500"
```

**Response `201 Created`:**

```json
{
  "paymentId": "PAYMENT_5E6F7A8B",
  "loan": {
    "loanId": "LOAN_1A2B3C4D",
    "loanAmount": 10000.0,
    "term": 12,
    "status": "ACTIVE",
    "createdDate": "2026-02-25T10:00:00Z"
  },
  "paymentAmount": 2500.0,
  "paymentDate": "2026-02-25T10:05:00Z"
}
```

**Response `404 Not Found`** — returned when no loan exists with the given loanId.

**Response `400 Bad Request`** — returned when the payment amount exceeds the outstanding loan balance or when invalid payment data is provided.

**Response `409 Conflict`** — returned when the loan has already been fully settled.

---

## CURL Example Calls

```bash
# 1. Create a loan for R5000 over 6 months
curl -X POST "http://localhost:8080/loans?loanAmount=5000&term=6"

# 2. Copy the loanId from the response, then make a first payment
curl -X POST "http://localhost:8080/payments?loanId=LOAN_1A2B3C4D&paymentAmount=1000"

# 3. Make a second payment
curl -X POST "http://localhost:8080/payments?loanId=LOAN_1A2B3C4D&paymentAmount=1000"

# 4. Retrieve the loan to check its current state
curl "http://localhost:8080/loans?loanId=LOAN_1A2B3C4D"

# 5. Attempt an overpayment (will return 400)
curl -X POST "http://localhost:8080/payments?loanId=LOAN_1A2B3C4D&paymentAmount=9999"

# 6. Settle the loan with the exact remaining balance
curl -X POST "http://localhost:8080/payments?loanId=LOAN_1A2B3C4D&paymentAmount=3000"

# 7. Attempt a payment on the now-settled loan (will return 409)
curl -X POST "http://localhost:8080/payments?loanId=LOAN_1A2B3C4D&paymentAmount=100"
```