# sample

Spring Boot 3.x (3.5) / Java 17+ layered monolith (JPA, Liquibase). Each exercise below lives in
the same service and was delivered in its own pull request.

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

## Q3: Authentication and roles

| Method | Path | Notes |
|---|---|---|
| POST | `/api/v1/auth/register` | 201, creates a USER; 409 if the email is taken |
| POST | `/api/v1/auth/login` | bearer token valid for 15 minutes |
| GET | `/api/v1/users/me` | any logged-in user |
| GET | `/api/v1/users` | ADMIN only (403 for USER, 401 without a token) |

| Environment variable | Purpose | Default |
|---|---|---|
| `JWT_SECRET` | HS256 signing key, at least 32 bytes (startup fails if shorter) | random per start, so tokens die on restart |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | create an ADMIN account on startup | none |

```bash
curl -X POST localhost:8080/api/v1/auth/register -H 'Content-Type: application/json' \
  -d '{"email":"me@example.com","password":"<at least 8 chars>"}'
curl -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"me@example.com","password":"<password>"}'
curl localhost:8080/api/v1/users/me -H 'Authorization: Bearer <token>'
```

Stateless HS256 JWTs (Spring Security OAuth2 resource server, no sessions), role in a `roles`
claim, BCrypt password hashes. 401 and 403 return the standard JSON error shape. No secret is
in the source: the signing key and admin credentials come only from the environment. Only the
user endpoints require a token; the other exercises' endpoints stay public so each can be tried on
its own, and every new endpoint is authenticated by default.

## Q4: Product catalog

| Method | Path | Notes |
|---|---|---|
| GET | `/api/v1/products` | filters `category`, `minPriceCents`, `maxPriceCents`, `inStock`, `name`; `page`, `size` (max 100), `sort=field,dir` |
| GET | `/api/v1/products/{id}` | cached |
| POST / PUT / DELETE | `/api/v1/products[/{id}]` | changes evict the cache |

```bash
curl 'localhost:8080/api/v1/products?category=Books&inStock=true&minPriceCents=1000&name=deluxe&sort=priceCents,desc&size=20'
curl localhost:8080/api/v1/products/<id>
```

100 products are seeded on startup. Filters are JPA Specifications, so any combination works in
one request; the page response carries `totalElements` and `totalPages`.

**Fast lookups without stale data.** `GET /products/{id}` is `@Cacheable` (Caffeine); update and
delete use `@CacheEvict`. The cache manager is transaction-aware, so evictions run after commit and
a concurrent reader can't re-cache the old row mid-transaction. A 10-minute TTL is a safety net.
**How we know:** `ProductCacheTest` spies on the repository and asserts that three lookups cause
one `findById`, and that reads after update and delete are fresh. At runtime, `/actuator/caches`
lists the cache.

## Q5: Order service

| Method | Path | Notes |
|---|---|---|
| POST | `/api/v1/orders` | header `Idempotency-Key`; 201 new, 200 replay, 409 insufficient stock |
| GET | `/api/v1/orders/{id}` | |
| POST | `/api/v1/orders/{id}/cancel` | returns the stock; 409 if already cancelled |

```bash
curl -X POST localhost:8080/api/v1/orders -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: 4f6c1f9e-order-1' \
  -d '{"items":[{"productId":"<id>","quantity":2}]}'
curl -X POST localhost:8080/api/v1/orders/<orderId>/cancel
```

**No overselling.** Each item is reserved with
`UPDATE product SET stock = stock - :q WHERE id = :id AND stock >= :q`. The row lock serialises
concurrent buyers, and the condition makes a reservation that would go negative affect zero rows.
Any zero-row result throws `InsufficientStockException` (409), which rolls back the whole
transaction, including earlier items and the order row: all-or-nothing. Items are merged per product
and processed in id order, so two multi-item orders can't deadlock. Placing and cancelling evict
the affected products from the Q4 cache. `OrderConcurrencyTest` fires 50 simultaneous orders at
stock 10: exactly 10 succeed and stock ends at 0.

**Retries.** The client sends an `Idempotency-Key` (e.g. a UUID per logical order) and reuses it on
retry. A key seen before returns the original order with 200 and no side effects. Two concurrent
requests with the same key race on a unique index: the loser's transaction rolls back (including
its stock reservation) and it returns the winner's order. A test fires 10 concurrent retries and
expects one order and one stock deduction. Reusing a key with a different payload returns the
original order rather than an error.

**Cancel.** The `PLACED → CANCELLED` transition is a conditional update, so a double cancel can't
return stock twice.
