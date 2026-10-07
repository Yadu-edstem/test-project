# sample

Spring Boot 3.x (3.5) / Java 17+ layered monolith (JPA, Liquibase), following the
`edstem-tech/lambdabooks-service` conventions. Each exercise below lives in the same service and
was delivered in its own pull request.

## Run

Requires Java 17 or newer (the build enforces it).

```bash
./mvnw spring-boot:run                       # H2 in PostgreSQL mode, http://localhost:8080
./mvnw spotless:apply test                   # gate: format + all tests
SPRING_PROFILES_ACTIVE=postgres DB_URL=jdbc:postgresql://localhost:5432/sample \
  DB_USERNAME=... DB_PASSWORD=... ./mvnw spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health
- H2 console: http://localhost:8080/h2-console (`jdbc:h2:mem:sample`, user `sa`)

## Response shape

Every endpoint (except redirects) returns `ApiResponse<T>`. Every error uses the same shape with
the right HTTP status: validation, malformed bodies, bad query parameters or sort fields and
missing headers become 400 with per-field `details`; framework rejections keep their own status
(404, 405, 415, ...); unexpected failures return a generic 500 without internals.

```json
{ "success": true,  "data": { }, "error": null }
{ "success": false, "data": null,
  "error": { "code": "VALIDATION_ERROR", "message": "Validation failed",
             "details": { "title": ["Title is required"] } } }
```

## Q1: Task manager

| Method | Path | Notes |
|---|---|---|
| POST | `/api/v1/tasks` | 201; title required (max 100), status defaults to `TODO`, due date not in the past |
| GET | `/api/v1/tasks?status=IN_PROGRESS&page=0&size=20` | paged, optional status filter |
| GET / PUT / DELETE | `/api/v1/tasks/{id}` | 200 / 200 / 204; 404 when unknown |

```bash
curl -X POST localhost:8080/api/v1/tasks -H 'Content-Type: application/json' \
  -d '{"title":"Ship it","status":"IN_PROGRESS","dueDate":"2026-12-01"}'
curl 'localhost:8080/api/v1/tasks?status=IN_PROGRESS'
```

Unknown enum values (`"status":"LATER"`) and bad query values are reported against the field,
like any other validation error.
