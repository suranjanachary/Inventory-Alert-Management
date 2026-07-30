# Phase 8 — Final review

## Staff / mentor review (3–4 YOE bar)

| Area | Score | Notes |
|------|-------|-------|
| Correctness | 8.5 | Clear domain rules; optimistic lock + alert dedupe still best-effort |
| Code quality | 8 | Thin controllers, constructor injection, packages consistent |
| Security | 7 | Good demo JWT/RBAC; prod still needs secrets, rate limits, refresh |
| Testing | 8 | Unit + Testcontainers API IT; expand edge cases over time |
| Operability | 8 | Actuator, profiles, Compose, correlation logs |
| Docs | 8.5 | Phase docs + architecture + interview prep |

**Overall: ~8/10** as a portfolio “production-shaped” Spring Boot service.

## Light refactors applied / recommended

Done in earlier phases (kept stable here):

- Central `ErrorResponse` + advice
- Cache eviction on stock mutations
- Flyway split + admin seed

Optional next refactors (not blocking):

1. Cap `Pageable` max size globally
2. Extract permission constants shared by `SecurityConfig` and docs
3. Replace in-memory cache with Redis when scaling out
4. Add ShedLock for scheduler
5. Map `DataIntegrityViolationException` → `DuplicateSkuException`

## Pre-production checklist

- [ ] No default `JWT_SECRET` in real prod env
- [ ] Swagger disabled in prod
- [ ] Rate limit login/register
- [ ] Centralized logging + alerts on 5xx
- [ ] DB backups + migration runbook
- [ ] Load test sale path under concurrency
- [ ] Dependency CVE scan in CI
- [ ] `mvn test` green in CI with Docker service

## Deliverables in this phase

- Architecture diagrams — [`architecture.md`](architecture.md)
- Interview Q bank — [`interview-prep.md`](interview-prep.md)
- Cursor workflow prompts — [`../AI_PROMPTS.md`](../AI_PROMPTS.md)
- Testing notes — [`phase-7-testing.md`](phase-7-testing.md)
