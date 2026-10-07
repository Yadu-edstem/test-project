# sample

Spring Boot 3.5 / Java 21 layered monolith (JPA, Liquibase), following the
`edstem-tech/lambdabooks-service` conventions.

## Run

```bash
./mvnw spring-boot:run                       # H2 in PostgreSQL mode, http://localhost:8080
SPRING_PROFILES_ACTIVE=postgres DB_URL=jdbc:postgresql://localhost:5432/sample \
  DB_USERNAME=... DB_PASSWORD=... ./mvnw spring-boot:run
./mvnw spotless:apply test                   # gate
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health
- H2 console: http://localhost:8080/h2-console (`jdbc:h2:mem:sample`, user `sa`)

## Response shape

Every endpoint returns `ApiResponse<T>`:

```json
{ "success": true, "data": { }, "error": null }
{ "success": false, "data": null, "error": { "code": "RESOURCE_NOT_FOUND", "message": "...", "details": null } }
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /actuator/health | Health check |
