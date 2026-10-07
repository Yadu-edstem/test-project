# Changelog

## 0.0.1

- Service scaffold on Spring Boot 3.5 / Java 17+: `BaseEntity`, `ApiResponse`, global error handling, Liquibase.
- Task manager API: CRUD, status filter, field-level validation errors.
- URL shortener: 8-character codes, redirect with atomic visit counting, stats, 404/410 handling.
- Authentication: register/login with BCrypt, stateless 15-minute JWTs, USER/ADMIN roles, JSON 401/403.
- Product catalog: 100 seeded products, combinable filters, paging capped at 100, transaction-aware cached lookups.
