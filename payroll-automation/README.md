# Paylocity Payroll Automation Agent

## 1. Executive Summary

This project implements a robust **Payroll Automation Agent** targeting the Paylocity Web Services API. Designed for high-reliability enterprise environments, the agent automates the critical path of payroll processing:

1.  **Secure Authentication**: Handles OAuth 2.0 Client Credentials flow with automatic token caching and preemptive renewal.
2.  **Identity Resolution**: Synchronizes employee data from Paylocity to a local operational database, mapping mutable names/emails to immutable Paylocity Employee IDs.
3.  **Code Mapping**: Dynamically resolves company-specific earning codes (e.g., "Regular Pay" -> "E001") using fuzzy matching logic.
4.  **Batch Submission**: Constructs and submits compliant JSON payroll batches to the Pay Entry API.
5.  **Asynchronous Tracking**: Polls for batch status and provides real-time feedback on processing outcomes.

The system is built as a **Spring Boot** application, leveraging `RestClient` for efficient HTTP communication and `Spring Data JPA` for persistence.

---

## 2. Prerequisites

To run this application, you need:

-   **Java 17** or higher (tested with Java 21).
-   **Maven** (wrapper included).
-   **Paylocity API Credentials**:
    -   `Client ID`
    -   `Client Secret`
    -   `Company ID`
-   **Network Access**: The server must be able to reach `https://apisandbox.paylocity.com` (Sandbox) or `https://api.paylocity.com` (Production).

---

## 3. Configuration

The application is configured via `application.properties` (or environment variables).

### 3.1. Key Properties

Located in `src/main/resources/application.properties`:

```properties
# Paylocity Credentials
paylocity.clientId=YOUR_CLIENT_ID
paylocity.clientSecret=YOUR_CLIENT_SECRET
paylocity.companyId=YOUR_COMPANY_ID
paylocity.environment=sandbox  # Options: sandbox, production

# Database Configuration (H2 In-Memory by default)
spring.datasource.url=jdbc:h2:mem:payrolldb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

### 3.2. Environment Variables (Recommended for Production)

Do not commit secrets to source control. Use environment variables:

-   `PAYLOCITY_CLIENTID`
-   `PAYLOCITY_CLIENTSECRET`
-   `PAYLOCITY_COMPANYID`

---

## 4. Architecture

### 4.1. Core Components

1.  **PaylocityAuthService**:
    -   Manages the OAuth 2.0 lifecycle.
    -   Uses a `ReentrantLock` to ensure thread-safe token refreshing.
    -   Refreshes tokens 5 minutes before expiration to prevent race conditions.

2.  **PaylocityClient**:
    -   A typed HTTP client wrapping Spring's `RestClient`.
    -   Handles API pagination (e.g., fetching 10,000 employees in chunks).
    -   Standardizes error handling and JSON deserialization.

3.  **PayrollService**:
    -   **Orchestrator**: Coordinates data flow between the API and the Database.
    -   **Logic**:
        -   Resolves "John Doe" to `EmployeeID: 12345`.
        -   Maps "Overtime" to `EarningCode: E002`.
        -   Groups multiple earning lines (Regular, Bonus) into a single Employee Record.

4.  **Persistence Layer**:
    -   `EmployeeRepository`: Stores the local identity map.
    -   `PayrollBatchRepository`: Tracks the status of submitted batches (`Pending` -> `Completed`/`Failed`).

### 4.2. Data Flow

1.  **Sync**: `POST /api/payroll/sync-employees` -> Fetches all employees from Paylocity -> Updates local DB.
2.  **Submit**: `POST /api/payroll/submit` -> Accepts raw payroll data -> Resolves IDs/Codes -> Submits to Paylocity -> Returns `TrackingID`.
3.  **Poll**: `GET /api/payroll/batch/{trackingId}` -> Checks Paylocity status -> Updates DB -> Returns current status.

---

## 5. Usage Guide (API Reference)

### 5.1. Synchronize Employees

**Endpoint:** `POST /api/payroll/sync-employees`

Triggers a full synchronization of the employee directory. Must be run before submitting payroll to ensure the local map is up-to-date.

**Response:** `200 OK` "Employee synchronization started/completed successfully."

### 5.2. Submit Payroll Batch

**Endpoint:** `POST /api/payroll/submit`

Submits a new payroll batch. The system will automatically map names to IDs and earning types to codes.

**Request Body:**

```json
{
  "payPeriodBegin": "2023-10-15",
  "payPeriodEnd": "2023-10-28",
  "checkDate": "2023-11-03",
  "entries": [
    {
      "employeeName": "John Doe",
      "earningType": "Regular",
      "hours": 40.0
    },
    {
      "employeeName": "John Doe",
      "earningType": "Overtime",
      "hours": 5.0
    },
    {
      "employeeName": "Jane Smith",
      "earningType": "Regular",
      "hours": 40.0
    }
  ]
}
```

**Response:** `202 Accepted`

```json
{
  "id": 1,
  "trackingId": "98a76s-87as6d-87as6d",
  "status": "Pending",
  "submitDate": "2023-10-30T10:00:00"
}
```

### 5.3. Check Batch Status

**Endpoint:** `GET /api/payroll/batch/{trackingId}`

Polls the Paylocity API for the current status of the batch.

**Response:** `200 OK`

```json
{
  "id": 1,
  "trackingId": "98a76s-87as6d-87as6d",
  "status": "Completed",
  "errors": null
}
```

---

## 6. Development & Testing

### 6.1. Running Locally

```bash
# Compile and Run
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`.

### 6.2. Integration Testing (WireMock)

The project includes a comprehensive integration test suite that **mocks the Paylocity API**. This allows you to verify the entire payroll flow (Sync -> Submit -> Status) without needing valid credentials or hitting the actual Sandbox environment.

**Run Tests:**

```bash
./mvnw test
```

**Test Class:** `src/test/java/com/czen/payroll_automation/PayrollIntegrationTest.java`
-   Starts a WireMock server on port 8089.
-   Stubs the Auth (Token), Employee, Code, and Pay Entry endpoints.
-   Verifies that the `PayrollService` correctly handles the full lifecycle.

---

## 7. Troubleshooting

### Common Issues

1.  **401 Unauthorized**:
    -   Check `paylocity.clientId` and `paylocity.clientSecret`.
    -   Ensure the `paylocity.companyId` matches the credentials.
    -   Verify the IP address is allowlisted in Paylocity (if applicable).

2.  **400 Bad Request (Invalid Earning Code)**:
    -   The system logs a warning if it cannot resolve a code: `Earning code not found for type: X`.
    -   Ensure the `earningType` in your JSON matches a description in Paylocity (fuzzy match).
    -   Run `syncEmployees` to ensure the local cache is fresh.

3.  **Employee Not Found**:
    -   Ensure the employee is "Active" (Status 'A') in Paylocity.
    -   Verify the name spelling matches exactly what is in Paylocity (or use Email for better reliability).

### Logs

Application logs are written to `payroll-automation.log` (if configured) and the console.
-   **DEBUG**: Shows detailed API request/response metadata.
-   **INFO**: Shows high-level flow (Batch Submitted, Sync Completed).
-   **ERROR**: Shows stack traces for API failures or parsing errors.
