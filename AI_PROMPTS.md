# AI_PROMPTS.md — Cursor workflow for this project

This file records how Cursor was used to build **Inventory Alert Management** in phased increments. Reuse these prompt patterns for similar Spring Boot portfolio systems.

## Principles we followed

1. **One phase per prompt** — architecture → persistence → services → API → security → production → tests → wrap-up.
2. **Do not edit the plan file** when executing assigned todos; mark todos in progress/completed.
3. **Stop for review** between phases when asked (“wait for confirmation”).
4. **Prefer implementing over instructing** — run `mvn compile` / `mvn test`, start Docker when diagnosing failures.
5. **No secrets in commits** — `.env` gitignored; document `.env.example`.

## Prompt sequence (high level)

### Phase 1 — Scaffold
> Implement Phase 1 todos only: create project root, Maven Boot 3.5.16 skeleton, package layout, config stubs with temporary permitAll Security, Docker MySQL, verify compile. Do not edit the plan file.

### Phase 2 — Persistence
> Implement persistence only: entities, repositories, DTOs, Flyway schema, ER diagram docs. No services/controllers.

### Phase 3 — Services
> Implement business layer with interfaces/impls, transactions, custom exceptions, logging. Do not add controllers yet.

### Phase 4 — API
> Controllers + `@RestControllerAdvice` + OpenAPI. Do not modify services unless critically required.

### Security review (read-only)
> Act as Principal Security Architect. Review auth for production: OWASP, JWT, secrets, rate limits. Score /10 and roadmap — do not rewrite everything.

### Phase 5 — JWT
> Implement JWT + RBAC with jjwt, filter, UserDetails, BCrypt, Swagger Authorize. Explain classes and self-review.

### Ops incident
> Paste full stack trace; ask to diagnose. (Here: MySQL down → Flyway cascade looked like security bean failure.)

### Phase 6 — Production readiness
> Scheduler, Flyway split, cache, actuator, logging, profiles, Docker Compose app, correlation IDs, README — without changing business rules unless required.

### Phase 7 & 8 — Tests + wrap-up
> JUnit 5, Mockito, Testcontainers, MockMvc IT; then final review, architecture diagrams, interview prep, and this AI_PROMPTS.md.

## Useful follow-up prompts

```text
Act as a Senior Java Reviewer. Review only the Controller and Exception Handling layer.
Be critical. Do not rewrite everything. Explain what should improve and why.
```

```text
Root-cause this startup failure from the stack trace. Distinguish cascading symptoms from the first real cause.
```

```text
Create branch cursor/<short-description>, commit only related files, do not push.
```

```text
Push branch and open a draft PR against master using gh.
```

## What worked well

- Explicit “do NOT implement X yet” constraints kept phases reviewable.
- Asking for **senior/architect reviews** before coding the next layer improved design quality.
- Separating **security review** from **security implementation** avoided premature complexity.

## Pitfalls to avoid in future Cursor sessions

- Leaving `permitAll` / default JWT secrets without documenting them as temporary.
- Splitting Flyway `V1` after it was applied — requires volume reset (`docker compose down -v`).
- Assuming Testcontainers IT will run without Docker — use `disabledWithoutDocker = true`.
- Gigantic “build the entire app” prompts — prefer phased delivery with compile gates.

## Suggested Cursor rules (optional)

Add a project rule such as:

```text
For this repo: Java 17 + Spring Boot 3.5, package com.inventory.alert,
prefer constructor injection, never log passwords/JWTs, Flyway owns schema,
write tests for new domain rules (Mockito unit + Testcontainers when DB matters).
```
