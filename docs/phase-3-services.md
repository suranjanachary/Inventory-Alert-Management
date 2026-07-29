# Phase 3 — Service layer

Business logic only. No controllers, JWT, or scheduler.

## Components

| Piece | Location |
|-------|----------|
| Interfaces | `service/ProductService`, `InventoryService`, `InventoryAlertService` |
| Implementations | `service/impl/*ServiceImpl` |
| MapStruct mappers | `mapper/*` (entities never leave the service layer) |
| Request validation | `service/support/RequestValidator` (Bean Validation without controllers) |
| Exceptions | `exception/*` |

## Transaction boundaries

| Method | Annotation | Why |
|--------|------------|-----|
| `ProductServiceImpl.create/update/softDelete` | `@Transactional` | Multi-step write (check + mutate + save) must be atomic |
| `ProductServiceImpl` reads | `readOnly=true` | Flush mode / hint for Hibernate; no dirty writes |
| `InventoryServiceImpl.purchaseStock` | `@Transactional` | Stock update + ledger insert succeed or roll back together |
| `InventoryServiceImpl.sellStock` | `@Transactional` | Stock + ledger + optional alert creation are one unit of work |
| `InventoryAlertServiceImpl.ensurePendingLowStockAlert` | `@Transactional` (REQUIRED) | Joins the sell transaction — alert cannot commit if sale rolls back |
| `resolveAlert` | `@Transactional` | Status + `resolvedAt` update |
| History / pending / by-product | `readOnly=true` | Query-only |

Read methods are never marked with a writable `@Transactional` without `readOnly=true`.

## Exception catalog

| Exception | Why it exists |
|-----------|----------------|
| `ProductNotFoundException` | Map missing id/SKU to 404-style API errors later |
| `DuplicateSkuException` | Clear conflict before/around unique index violations |
| `InsufficientStockException` | Domain rule: never sell below zero |
| `InactiveProductException` | Sales blocked on soft-deleted/inactive catalog rows |
| `DuplicateAlertException` | Documents “one PENDING LOW_STOCK per product” for explicit create paths |
| `AlertNotFoundException` / `AlertAlreadyResolvedException` | Resolve semantics |
| `InvalidRequestException` | DTO Bean Validation failures without `@Valid` controllers yet |

## Repository call notes

- `existsBySku` / `existsBySkuAndIdNot` — uniqueness without loading the row
- `existsById` — cheap 404 guard before empty history/alert pages
- `existsByProductIdAndAlertTypeAndStatus` — duplicate pending alert check
- `getReferenceById` — set alert FK without SELECT of full product
- `@EntityGraph(product)` on alert/txn page queries — avoid N+1 when mapping SKU

## Design decisions

1. **Interfaces + impl** — ISP/DIP; controllers (Phase 8) depend on abstractions.
2. **Constructor injection** (`@RequiredArgsConstructor`) — mandatory dependencies, testable.
3. **DTOs only across service API** — MapStruct at the boundary.
4. **Soft delete** — `active=false`; preserves FK history (`ON DELETE RESTRICT`).
5. **Purchase allowed on inactive** — restock before reactivation; **sale requires active**.
6. **Inventory → AlertService** for low-stock — alert rules stay in one place (SRP).
7. **Optimistic locking** — `Product.version` serializes concurrent stock writers; loser gets `OptimisticLockException`.

## Self code review (senior)

### SOLID
- **Good:** service interfaces, alert creation delegated, mappers isolated.
- **Watch:** `InventoryAlertService.ensurePendingLowStockAlert` is a bit of an “internal API” on a public interface — acceptable for Spring proxies; could move to a package-private collaborator later.

### Transactions
- Sell path is correctly one TX including alert.
- `ensurePendingLowStockAlert` also checks `existsById` even when called from sell (extra round-trip) — minor; could add an overload taking a managed `Product` in a package-private component to avoid it without leaking entities on the public API.

### Concurrency / races
- Concurrent sells on the same product: `@Version` causes one commit to win; the other fails with optimistic lock — **clients must retry**. No automatic retry template yet.
- Concurrent `ensurePendingLowStockAlert` without product version involvement (e.g. future scheduler + sale): possible duplicate PENDING alerts — mitigate later with a unique generated-column index or `SELECT … FOR UPDATE` on product.
- SKU create race: `existsBySku` then insert — rare duplicate caught by DB unique constraint; map `DataIntegrityViolationException` in Phase 8 advice.

### Performance
- Entity graphs on history/alerts: good.
- Product update still loads full entity (necessary for `@Version`).
- Search `LIKE %name%` remains non-index-friendly at scale.

### Readability
- Helpers (`assertActive`, `saveLedger`, `applyMutableFields`) keep public methods short.
- Logging avoids secrets; includes business identifiers only.

### Possible improvements before controllers
1. Retry template / translate `OptimisticLockException` to a domain conflict exception.
2. Map DB unique violations to `DuplicateSkuException`.
3. Partial unique index for pending alerts.
4. Unit tests with `@DataJpaTest` / Mockito for stock math and alert dedupe.
5. Do not allow direct `availableQuantity` edits via `ProductUpdateRequest` if all stock must go through InventoryService (stricter ledger integrity).

**Stop here — await review before Controllers.**
