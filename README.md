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

## Q2: URL shortener

| Method | Path | Notes |
|---|---|---|
| POST | `/api/v1/links` | `{url, expiresAt?}`; 201 new, 200 when the same URL + expiry already exists |
| GET | `/s/{code}` | 302 to the original URL and counts the visit; 404 unknown, 410 expired |
| GET | `/api/v1/links/{code}/stats` | original URL, visit count, created date |

`SHORT_LINK_BASE_URL` sets the host used in returned short URLs (default `http://localhost:8080`).

```bash
curl -X POST localhost:8080/api/v1/links -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/a/very/long/path","expiresAt":"2026-12-31T00:00:00Z"}'
curl -i localhost:8080/s/<code>
curl localhost:8080/api/v1/links/<code>/stats
```

**Shortening the same URL twice.** The same URL with the same expiry returns the existing link
(200 instead of 201). That makes shortening idempotent, so client retries never create extra
codes, and the code space isn't wasted. A different expiry is a different link, because the
lifetimes differ. Trade-off: everyone who shortens the same URL shares one link and one visit
count. If per-user stats mattered, creating a new code every time would be the better choice.

**Codes.** 8 characters from `SecureRandom` over `[A-Za-z0-9]` (62^8 ≈ 2.2·10^14), checked for
collisions, with a unique index as the final guard. URLs must parse as absolute http(s) URIs, so
anything accepted can always be redirected to.

**Accurate visit counts.** Each visit is a single `UPDATE … SET visit_count = visit_count + 1` in
the database, so concurrent visits never overwrite each other (no read-modify-write in Java).
`ShortLinkApiTest.concurrentVisitsAreAllCounted` fires 50 simultaneous visits and expects 50.
