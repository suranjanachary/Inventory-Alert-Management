# Phase 2 — Persistence annotations & relationships

## Important annotations

| Annotation | Where | Why |
|------------|-------|-----|
| `@Entity` / `@Table` | all entities | Map to snake_case tables with named unique constraints |
| `@Id` + `@GeneratedValue(IDENTITY)` | all PKs | MySQL auto-increment; simple for single-node demo |
| `@Column` | fields | Nullability, length, precision aligned with Flyway |
| `@Enumerated(STRING)` | role, types, status | Stable, readable; matches VARCHAR + CHECK |
| `@ManyToOne(LAZY)` | txn/alert → product | Avoid N+1 / accidental full graph loads; open-in-view is false |
| `@JoinColumn` | FK column | Owns the foreign key; no inverse collection |
| `@Version` | `Product` only | Optimistic locking on stock mutations |
| `@CreatedDate` / `@LastModifiedDate` | audit fields | Filled by `AuditingEntityListener` |
| `@EntityListeners(AuditingEntityListener)` | audited entities | Required for Spring Data auditing |
| `@EnableJpaAuditing` | `JpaConfig` | Activates auditing |
| Jakarta Validation | entities + DTOs | Defense in depth; controllers will trigger DTO validation later |
| Lombok `@NoArgsConstructor(PROTECTED)` | entities | JPA proxy requirement without a public no-arg API |
| `@ToString(exclude = "password")` | `User` | Prevent log leaks |
| Flyway `V1__...sql` | migrations | Schema source of truth; `ddl-auto: validate` |

## Relationships chosen

1. **InventoryTransaction → Product (ManyToOne, unidirectional, LAZY)**  
   Ledger belongs to a product. No `Product.transactions` collection — listing history goes through `InventoryTransactionRepository` with pagination, which is the scalable access path.

2. **InventoryAlert → Product (ManyToOne, unidirectional, LAZY)**  
   Same rationale. Scheduler queries alerts by status, not by navigating from Product.

3. **User has no inventory FKs**  
   Auth principal is orthogonal until “who changed stock” auditing is required.

4. **No CascadeType.ALL**  
   Cascading persist/remove from Product would delete ledger/alert history accidentally. Children are saved explicitly.

5. **No bidirectional mapping**  
   Bidirectional adds inverse-side sync bugs (`mappedBy`, orphanRemoval) without benefit at this stage.

## DTOs vs entities

- Create / Update / Response separated per aggregate.
- **No `InventoryTransactionUpdateRequest`** — ledger rows are immutable; corrections are new compensating transactions (service-layer rule).
- `UserResponse` omits password entirely.
- Response DTOs expose `productId` (+ optional `productSku`) instead of nested entities to keep JSON flat and avoid lazy serialization traps.

## Package structure

```
com.inventory.alert
  config/          JpaConfig (+ existing Security/Cache/OpenAPI)
  entity/          User, Product, InventoryTransaction, InventoryAlert
  enums/           Role, TransactionType, AlertType, AlertStatus
  repository/      Spring Data interfaces
  dto/request/     *CreateRequest, *UpdateRequest
  dto/response/    *Response
```

Schema documentation: [`docs/database-design.md`](../docs/database-design.md)  
Flyway script: [`src/main/resources/db/migration/V1__create_inventory_schema.sql`](../src/main/resources/db/migration/V1__create_inventory_schema.sql)

## Self-review / possible improvements (before Service layer)

1. **Partial unique open alert** — MySQL cannot easily do `UNIQUE (product_id) WHERE status='PENDING'`. Service must use `existsByProductIdAndAlertTypeAndStatus` (+ transaction isolation) or a generated-column unique index in a later migration.
2. **`created_by` / `updated_by`** — not modeled yet; add when JWT security (Phase 9) provides a principal for `AuditorAware`.
3. **Soft delete** — products use `active`; users use `enabled`. No generic `@SQLDelete`. Fine for demo; consider if GDPR erase is needed.
4. **Transaction immutability** — enforce at service with no update repository methods; optionally DB trigger / revoke UPDATE grants in hardened envs.
5. **Money** — `DECIMAL(19,2)` assumes a single currency; add `currency` column if multi-currency appears.
6. **Name search** — B-tree `LIKE %name%` will not scale; FULLTEXT or external search later.
7. **Equals/hashCode** — entities intentionally omit Lombok `@EqualsAndHashCode`; services should compare by id. Document for Set usage.
8. **AlertType extensibility** — only `LOW_STOCK` today; VARCHAR width leaves room for `OVERSTOCK`, etc.
9. **Quantity vs BigDecimal** — integer quantities assume whole units; switch to `DECIMAL` if fractional stock (kg, liters) is required.
10. **MapStruct mappers** — deferred to Phase 6; DTOs exist so the API boundary is already defined.
