# Phase 2 — Database Design

Persistence layer only. No services, controllers, or security logic.

## ER diagram (ASCII)

```
┌──────────────────────────┐
│          users           │
├──────────────────────────┤
│ PK id                    │
│    full_name             │
│ UK email                 │
│    password (hashed)     │
│    role (ADMIN|MANAGER|  │
│          VIEWER)         │
│    enabled               │
│    created_at            │
│    updated_at            │
└──────────────────────────┘
         (auth only; no FK to inventory yet)


┌──────────────────────────┐         ┌────────────────────────────────┐
│         products         │         │    inventory_transactions      │
├──────────────────────────┤         ├────────────────────────────────┤
│ PK id                    │◄────────┤ PK id                          │
│ UK sku                   │  1    * │ FK product_id                  │
│    name                  │         │    transaction_type            │
│    description (NULL)    │         │      (PURCHASE|SALE)           │
│    price                 │         │    quantity                    │
│    available_quantity    │         │    quantity_before             │
│    minimum_quantity      │         │    quantity_after              │
│    active                │         │    remarks (NULL)              │
│    created_at            │         │    created_at                  │
│    updated_at            │         └────────────────────────────────┘
│    version (optimistic)  │
└────────────┬─────────────┘
             │ 1
             │
             │ *
┌────────────┴─────────────┐
│     inventory_alerts     │
├──────────────────────────┤
│ PK id                    │
│ FK product_id            │
│    alert_type (LOW_STOCK)│
│    status                │
│      (PENDING|SENT|      │
│       RESOLVED)          │
│    message               │
│    created_at            │
│    resolved_at (NULL)    │
└──────────────────────────┘
```

## Design decisions

### Normalization

- **3NF:** Product holds current stock; `inventory_transactions` is an immutable ledger of changes (quantity before/after). Alerts are separate event rows, not flags on `products`, so multiple historical alerts can exist per product.
- **Users** are independent of inventory (auth/authorization). No premature `created_by` FKs until audit-of-who lands in a later phase.
- Avoided embedding stock history or alert state as JSON columns — relational querying for scheduler and reports needs first-class tables.

### Foreign keys

- `inventory_transactions.product_id` → `products.id` and `inventory_alerts.product_id` → `products.id`
- **`ON DELETE RESTRICT`:** deleting a product with history/alerts must be an explicit business decision (soft-deactivate via `active=false` instead).
- **`ON UPDATE CASCADE`:** keeps referential integrity if surrogate keys ever change (rare with IDENTITY, harmless).

### Indexes

| Index | Why |
|-------|-----|
| `uk_users_email` | Login + uniqueness |
| `uk_products_sku` | Business key lookup |
| `idx_products_active` | List active catalog |
| `idx_products_name` | Name search (demo-scale; consider FULLTEXT later) |
| `idx_inv_txn_product_created` | Ledger page by product + time |
| `idx_inv_alerts_status` | Scheduler: pending/sent scans |
| `idx_inv_alerts_product_status` | “Open alert for this SKU?” checks |

### Unique constraints

- Email and SKU only at DB level.
- **No unique (product_id, PENDING)** yet — duplicate open alerts should be prevented in the service/scheduler with a documented rule; a partial unique index is not portable on MySQL without a generated column workaround.

### Nullable columns

- `products.description` — optional merchandising text
- `inventory_transactions.remarks` — optional note
- `inventory_alerts.resolved_at` — null until `RESOLVED`

### Naming

- Tables/columns: `snake_case`
- Java: `camelCase` mapped with `@Column(name = "...")`
- Plural table names for collections of rows

### Optimistic locking

- **`products.version` only** — stock mutations are the concurrency hotspot (purchase/sale/alert resolution updating quantity).
- Transactions and alerts are insert-mostly; no `@Version` (avoids noisy conflicts on append-only rows).

### Audit fields

- `users` / `products`: `created_at` + `updated_at` via Spring Data JPA auditing
- `inventory_transactions`: `created_at` only (immutable ledger)
- `inventory_alerts`: `created_at` + domain `resolved_at` (not `updated_at` — status transitions are few and `resolved_at` is the meaningful timestamp)

### Enum storage

- **`VARCHAR` + CHECK** in SQL; `@Enumerated(EnumType.STRING)` in JPA
- Stable across renames/order changes (ordinal would break); readable in ops tools

### Scalability notes

- Ledger grows fastest — partition by `created_at` or archive cold rows later if needed
- Alert table stays small if PENDING is resolved promptly; status index keeps scheduler cheap
- Name search index is B-tree (prefix); switch to FULLTEXT or OpenSearch if catalog is large
- Single MySQL instance is fine for portfolio; read replicas / CQRS out of scope

### Relationship style (JPA)

- **Unidirectional `@ManyToOne(fetch = LAZY)`** from transaction/alert → product
- No `OneToMany` on `Product` — prevents accidental EAGER bags and cascade surprises
- **No `CascadeType.ALL`** — persist children explicitly in services

### Password exposure

- Stored only on `User` entity; excluded from `toString`
- **Never** present on `UserResponse` (or any response DTO)
