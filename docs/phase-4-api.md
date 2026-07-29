# Phase 4 — REST API layer

Controllers are thin: validate → delegate → map HTTP status. Business rules stay in services.

## Endpoints

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| POST | `/api/v1/products` | 201 | `Location` header |
| GET | `/api/v1/products` | 200 | `?active=true`, page/sort |
| GET | `/api/v1/products/search?name=` | 200 | |
| GET | `/api/v1/products/{id}` | 200/404 | |
| GET | `/api/v1/products/sku/{sku}` | 200/404 | |
| PUT | `/api/v1/products/{id}` | 200 | |
| DELETE | `/api/v1/products/{id}` | 204 | Soft delete |
| POST | `/api/v1/inventory/purchase` | 201 | |
| POST | `/api/v1/inventory/sale` | 201 | 422 insufficient/inactive |
| GET | `/api/v1/inventory/history/{productId}` | 200 | |
| GET | `/api/v1/alerts` | 200 | |
| GET | `/api/v1/alerts/pending` | 200 | |
| PUT | `/api/v1/alerts/{id}/resolve` | 200 | 409 if already resolved |
| GET | `/api/v1/alerts/product/{productId}` | 200 | |
| POST | `/api/v1/auth/login` | 501 | Placeholder |
| POST | `/api/v1/auth/register` | 501 | Placeholder |

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Service touch (minimal)

`InventoryAlertService.getAlerts(Pageable)` + `findAllWithProduct` were added so `GET /api/v1/alerts` has a service-backed page without putting repository calls in the controller.

## Exception → HTTP

| Exception | Status |
|-----------|--------|
| `ProductNotFoundException` / `AlertNotFoundException` | 404 |
| `DuplicateSkuException` / `DuplicateAlertException` / `AlertAlreadyResolvedException` / optimistic lock | 409 |
| `InsufficientStockException` / `InactiveProductException` | 422 |
| Validation / malformed JSON / `InvalidRequestException` | 400 |
| Unhandled | 500 (no stack in body) |

`ErrorResponse`: timestamp, status, error, message, path, errorCode, fieldErrors.

## Senior review

**REST naming** — Resources are nouns; `purchase`/`sale`/`resolve` are intentional RPC-style sub-resources for domain commands (acceptable when a pure noun would be awkward). Alternative: `POST /inventory/transactions` with `type` in body.

**Status codes** — Soft delete uses 204; creates use 201; domain conflicts use 409/422 appropriately.

**Validation** — `@Valid` on bodies; `@Validated` + `@NotBlank` on search query param.

**Maintainability** — Controllers stay under ~service delegation; advice centralizes errors.

**Swagger** — Tags, operations, error schemas, bearer scheme reserved for Phase 9.

### Suggested improvements

1. Envelope for pages (`data` + `meta`) if clients dislike Spring `Page` JSON shape.
2. `ApiResponse` wrapper vs raw DTO — trade consistency vs verbosity.
3. Request logging filter with correlation id (Madeira/MDC) instead of per-method INFO.
4. ETag / `If-Match` using `Product.version` for explicit concurrency control.
5. Secure auth endpoints and remove `permitAll` in Phase 9.

## Interview angles

- Why `ResponseEntity`? Full control of status, headers (`Location`), and body without coupling success shape to errors.
- Why `@RestControllerAdvice`? Cross-cutting translation of domain exceptions → stable JSON; controllers stay clean.
- Why DTOs? Hide persistence, versioning surface, prevent lazy-load leaks, shape API independently of tables.
- Why validate at controller? Fail fast on contract violations before service/DB work; services still re-validate for non-HTTP entry points.
- Why no business logic in controllers? SRP, testability, reuse from scheduler/messaging later.
- OpenAPI advantages? Contract-first discovery, client generation, living docs aligned with code.
- API versioning? URI (`/api/v1`) chosen here; alternatives: header (`Accept-Version`) or media type — URI is simplest for demos and gateways.
