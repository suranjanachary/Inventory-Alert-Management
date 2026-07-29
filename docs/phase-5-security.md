# Phase 5 — JWT Authentication & Authorization

Stateless JWT with Spring Security 6, BCrypt passwords, and role-based HTTP authorization.

## Access matrix

| Capability | ADMIN | MANAGER | VIEWER |
|------------|-------|---------|--------|
| Read products / history / alerts | yes | yes | yes |
| Create / update products | yes | yes | no |
| Soft-delete products | yes | no | no |
| Purchase / sell | yes | yes | no |
| Resolve alerts | yes | yes | no |
| Everything else | yes | no | no |

Public: `POST /api/v1/auth/login`, `POST /api/v1/auth/register`, Swagger, Actuator health.

## Major classes

| Class | Why it exists | How it works | Alternatives / trade-offs |
|-------|---------------|--------------|---------------------------|
| `SecurityConfig` | Single place for filter chain + RBAC | Stateless session, CSRF off, JWT filter before username/password filter, `hasRole` matchers | Method security (`@PreAuthorize`) — more granular but scattered |
| `JwtService` | Create/parse HS256 tokens | Claims: sub=email, userId, role, iat, exp | Asymmetric RSA — better key distribution; OAuth2 Resource Server — more moving parts |
| `JwtAuthenticationFilter` | Attach principal per request | Bearer extract → validate → `SecurityContext` | Spring OAuth2 JWT decoder filter |
| `JwtAuthenticationEntryPoint` / `RestAccessDeniedHandler` | JSON 401/403 | Write `ErrorResponse` | Default HTML/www-authenticate |
| `CustomUserDetails` (+Service) | Bridge DB users to Security | Email username, `ROLE_*` authorities | In-memory users (demo only) |
| `AuthenticationService` | Login/register use-cases | `AuthenticationManager` + BCrypt encode on register | Keycloak / Auth0 external IdP |
| `DevAdminInitializer` | Local ADMIN bootstrap | Seeds `admin@inventory.local` on `dev` if missing | Flyway seed SQL with precomputed hash |

## Dev admin

- Email: `admin@inventory.local`
- Password: `AdminPass123!` (dev profile only; change/remove for real deploys)

Public register allows **MANAGER** or **VIEWER** only (not ADMIN).

## JWT config

```yaml
app.jwt.secret: Base64 256-bit+ key (override with JWT_SECRET)
app.jwt.expiration-ms: 3600000
```

## Swagger

Authorize → Bearer `<accessToken>` from login.

## Self-review (Senior Security)

| Area | Finding |
|------|---------|
| Secret in yaml | Demo default is committed — **must** override via env in any shared environment |
| No refresh tokens | Access token lifetime is the revoke window; add refresh + rotation for production UX |
| No token denylist | Logout cannot invalidate JWTs early without denylist/version claim |
| Public register | Role self-selection (MANAGER) is generous; production often invites-only or VIEWER-only |
| JWT stores role | Role changes need re-login; alternatively load authorities only from DB on each request (we load UserDetails from DB after parsing email — **role in token is informational; authorities come from DB**) |
| Filter DB hit | Every authenticated request loads user by email — correct for enabled/role freshness; cache if hot |
| Timing on login | Generic "Invalid email or password" — good |
| CSRF | Disabled — correct for Bearer APIs; do not add cookie session without CSRF |
| HTTPS | Assumed at reverse proxy in production |
| Actuator | Only health public; `info` requires auth (and ADMIN catch-all) |

Note: After JWT parse we reload `UserDetails` from DB, so **disabled users and role changes apply on the next request**, even if the token still embeds an old role claim. The role claim is still useful for debugging/auditing.

## Interview questions

1. Explain the Spring Security authentication flow for a JWT request.
2. Why JWT instead of session authentication?
3. How does `SecurityFilterChain` work?
4. Why `OncePerRequestFilter`?
5. What happens when a request reaches the application (filter order)?
6. How is `SecurityContext` populated?
7. Difference between authentication and authorization?
8. Why BCrypt?
9. What should / should not be stored in a JWT?
10. How would you implement refresh tokens?
11. How would you invalidate JWTs before expiration?
12. What are the security risks of JWT?
13. How would you secure this application in production?
14. `hasRole` vs `hasAuthority`?
15. Why disable CSRF for stateless APIs?
