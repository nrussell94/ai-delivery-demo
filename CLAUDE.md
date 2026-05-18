# CLAUDE.md — ai-delivery-demo

Full-stack bootstrap for rapid feature delivery. Read this before touching any code.

## Architecture

```
contracts/openapi.yaml          ← single source of truth for all API shapes
backend/                        ← Spring Boot 3.3.5 / Java 21 / Maven
  src/main/java/com/example/demo/
    api/                        ← generated (do not edit by hand)
    controller/                 ← implements generated *Api interfaces
    config/                     ← WebConfig (CORS), RestTemplateConfig, GlobalExceptionHandler
  src/main/resources/
    application.yml
frontend/                       ← React 18 + TypeScript + MUI + Vite
  src/
    api.ts                      ← all fetch calls live here
    App.tsx                     ← root component
e2e/                            ← Playwright smoke tests
docker-compose.yml              ← SQLite volume + backend + frontend
```

## Contract-first workflow — the critical pattern

**Every new API endpoint starts in `contracts/openapi.yaml`.** Never add a route directly to Java first.

1. Edit `contracts/openapi.yaml` — add path, request/response schemas.
2. Regenerate backend interfaces:
   ```powershell
   cd backend
   mvn generate-sources
   ```
   This writes generated interfaces into `backend/target/generated-sources/openapi/` and makes them available under the `com.example.demo.api` package.
3. Create (or update) a `@RestController` in `controller/` that `implements` the generated `*Api` interface.
4. Add the corresponding fetch function in `frontend/src/api.ts`.
5. Build the UI in `frontend/src/`.

**Never edit files inside `backend/target/` or the generated `com.example.demo.api` package** — they are overwritten on every `mvn generate-sources`.

## Commands

### Local dev (everything)
```powershell
docker compose up --build
```
- Frontend: http://localhost:5173
- Backend:  http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Backend only
```powershell
cd backend
mvn spring-boot:run          # uses SQLite at ./data/demo.db — no separate service required
mvn generate-sources         # regenerate API interfaces from openapi.yaml
mvn test                     # unit tests only (Surefire)
mvn verify                   # unit + integration tests (Failsafe); ITs run in-process against in-memory SQLite
```

### Frontend only
```powershell
cd frontend
npm install
npm run dev                  # Vite dev server on :5173, proxies /api → :8080
npm test                     # Vitest unit tests (jsdom)
```

### E2E tests
```powershell
cd e2e
npm install
npm test                     # Playwright; expects full stack running on :5173
```

## Key conventions

- **API package**: `com.example.demo.api` — generated. `com.example.demo.controller` — hand-written.
- **CORS**: backend allows `GET`, `POST`, `OPTIONS` from `http://localhost:5173` on `/api/**`. Add new methods in `WebConfig` if needed.
- **Frontend env**: `VITE_API_BASE_URL` controls the API base (empty string in dev — Vite proxy handles it; set to `http://localhost:8080` in Docker).
- **Tests skip Docker in Maven build**: the `backend/Dockerfile` runs `mvn package -DskipTests`. Run `mvn verify` locally when you need backend integration test coverage.
- **Backend integration tests**: name files `*IT.java` (Failsafe convention). Tests use `@TestPropertySource(properties = "spring.datasource.url=jdbc:sqlite::memory:")`. No docker container is needed — SQLite runs in-process. Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)` + `@AutoConfigureMockMvc`. Do **not** use `@Testcontainers` / `@Container` — docker-java is broken on this machine's Docker Desktop named-pipe relay.
- **OpenAPI generator config** (in `pom.xml`): `interfaceOnly=true`, `useTags=true` — controllers implement tag-named interfaces (e.g., tag `health` → `HealthApi`).
- **JPA entities (Feature 2 onwards)**: when `@Entity` classes land, use Lombok `@Getter`, `@Setter`, `@NoArgsConstructor` on entities. Don't reach for `@Data` — entities should be equal by `@Id`, not all-fields. Generated API DTOs in `com.example.demo.api.model.*` keep their generator-emitted getters/setters; don't lombokify them — they're overwritten on every `mvn generate-sources`.
- **PR screenshots are dropped pre-merge.** Phase 9 of `/deliver` commits UI screenshots to `screenshots/<branch>/` on the PR branch so reviewers see visual evidence inline in the PR description. Before merging, run `git rm -rf screenshots/<branch>/ && git commit -m "Pre-merge: drop PR screenshots"`. Closed-without-merge PRs need no cleanup — branch deletion takes the screenshots with it.
- **GitHub-API markdown needs absolute image URLs.** GitHub renders relative image paths in committed markdown (READMEs, files browsed at HEAD) but **not** in anything posted through the API: PR descriptions (`gh pr create --body-file`), PR comments (`gh pr comment`), formal PR reviews (`gh pr review`), and issue comments all treat `![alt](screenshots/...png)` as a broken link. Rewrite image refs to absolute `https://raw.githubusercontent.com/<owner>/<repo>/<branch>/screenshots/<branch>/<slug>.png` URLs before posting. This applies to Phase 10 of `/deliver`, `/pr-agent`, and `/review-branch`'s opt-in GitHub publishing. The screenshots still live on the branch (so the pre-merge cleanup convention above still applies); only the URL form in the posted markdown changes. *Link-style* refs (`[text](path)`) auto-resolve in PR descriptions but not reliably in comments — prefer absolute `https://github.com/<owner>/<repo>/blob/<branch>/<path>` URLs for cross-comment links too.
- **SQLite is single-writer.** Concurrent writes from multiple JVM threads serialise via SQLite's file lock. Acceptable for a demo; not acceptable for production-scale write load.

## Known environmental quirks

- **Failsafe must be explicitly bound** (audit round 3, 2026-05-17). Spring Boot starter parent provides `pluginManagement` for `maven-failsafe-plugin` but does NOT bind goals to `integration-test`/`verify`. Without an explicit `<plugin><artifactId>maven-failsafe-plugin</artifactId>…<executions>` block in `<build><plugins>`, `*IT.java` files compile silently and never execute under `mvn verify` — `BUILD SUCCESS` is reported with zero IT coverage. Always verify Failsafe output (`failsafe:3.x.x:integration-test` line) appears in the build log when running IT.
- **Bash tool CWD drifts on Windows** (audit round 4, 2026-05-17). Successive Bash tool calls do not reliably honour `cd`-then-command chains across invocations — the working directory can snap back to wherever the first call ran. Use absolute paths (`mvn -f C:/git/ai-delivery-demo/backend/pom.xml verify`) or chain commands inside a single Bash call (`cd frontend && npm test`) rather than relying on persistent state between calls.
- **`/deliver` Phase 0 step 6 false-positive after Mongo→SQLite swap** (2026-05-18). The skill's Phase 0 step 6 greps for `exec-maven-plugin`. After this delivery the plugin is removed (it only existed for the mongo:7 IT container). Future `/deliver` runs will halt at Phase 0 with "wire the plugins" until the global skill is patched. Workaround: manually confirm Failsafe is bound (`<executions>` block in `pom.xml`) and ITs run in-process (in-memory SQLite, no container).

## Adding a new feature — checklist

- [ ] Add path + schemas to `contracts/openapi.yaml`
- [ ] `cd backend && mvn generate-sources`
- [ ] Create `controller/FooController.java` implementing the generated `FooApi`
- [ ] Add a `@Entity` model in a new `model/` package if persistence is needed (Feature 2+)
- [ ] Add fetch function(s) to `frontend/src/api.ts`
- [ ] Build React component(s); wire into `App.tsx` or new routes
- [ ] Add Vitest unit test for new component
- [ ] Add or extend Playwright smoke test in `e2e/tests/`

## Tech versions (pinned)

| Layer | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.5 |
| OpenAPI Generator | 7.8.0 |
| springdoc | 2.6.0 |
| SQLite (sqlite-jdbc) | 3.46.1.3 |
| Hibernate community dialect (org.hibernate.orm:hibernate-community-dialects) | 6.5.3.Final |
| Node / frontend | see `frontend/package.json` |
| Playwright | see `e2e/package.json` |

## Historical note (pre-2026-05-18 SQLite swap)

Prior to 2026-05-18, persistence was MongoDB 7 via `spring-boot-starter-data-mongodb`.

- **`@Document` Lombok convention (obsolete):** Persistence classes used `@Getter`, `@Setter`, `@NoArgsConstructor`. No `@Data` on entities to avoid PII leakage in `@ToString`. Not applicable post-swap.
- **`@EnableMongoAuditing` defeats autoconfigure-exclude:** The `mongoAuditingHandler` bean depended on `mongoMappingContext` regardless of `spring.autoconfigure.exclude`. This meant `@WebMvcTest` was required for tests not needing persistence. Not applicable post-swap.
- **Use `Instant`, not `OffsetDateTime`, for `@Document` fields:** Spring Data MongoDB's `Jsr310Converters` covered `Instant`/`LocalDateTime`/`ZonedDateTime` but NOT `OffsetDateTime`. Persisting `OffsetDateTime` failed with `CodecConfigurationException` at runtime (surfaced by ITs). Not applicable post-swap.
- **`exec-maven-plugin` for IT containers:** `mvn verify` previously started/stopped a `mongo:7` container on `:27018`. Removed 2026-05-18.

## Persistence migration log

**2026-05-18 — MongoDB → SQLite swap**

- `spring-boot-starter-data-mongodb` removed from `backend/pom.xml`.
- `MongoConfig.java` (`@EnableMongoAuditing`) deleted.
- `exec-maven-plugin` block removed from `pom.xml` (was mongo:7 IT container lifecycle).
- `spring-boot-starter-data-jpa` + `sqlite-jdbc:3.46.1.3` + `org.hibernate.orm:hibernate-community-dialects:6.5.3.Final` added.
- `application.yml`: `spring.data.mongodb.uri` removed; `spring.datasource.url`, `spring.jpa.database-platform`, `spring.jpa.hibernate.ddl-auto` added.
- `docker-compose.yml`: `mongo` service and `mongo-data` volume removed; `sqlite-data` named volume mounted at `/data` on `backend` added.
- ITs no longer need a Docker container. `@TestPropertySource(properties = "spring.datasource.url=jdbc:sqlite::memory:")` is sufficient.
- `spring.jpa.hibernate.ddl-auto=update` chosen over `validate` because `validate` fails at startup with zero JPA entities (as of this delivery, no `@Entity` classes exist yet — Feature 2 will add them).
- `@Pattern` on OpenAPI-generated query params (`lat`/`lon`) survived code generation with the `spring` generator at OpenAPI Generator 7.8.0. The regex `^-?\d{1,3}(\.\d{1,6})?$` in `contracts/openapi.yaml` emitted `@Pattern(regexp = "...")` on both parameters in the generated `WeatherApi` interface. No manual fallback needed.
- **Hibernate community dialects groupId correction (2026-05-18):** The correct Maven groupId for the community SQLite dialect is `org.hibernate.orm:hibernate-community-dialects`, NOT `org.hibernate.community:hibernate-community-dialects`. The `org.hibernate.community` group does not exist on Maven Central. Any plan or decision entry using `org.hibernate.community` must be read as `org.hibernate.orm`.
- **`@Primary` `@TestConfiguration` requires `spring.main.allow-bean-definition-overriding=true` (2026-05-18):** Spring Boot 2.6+ disables bean definition overriding by default. A `@TestConfiguration` providing a `@Primary` bean with the same name as a production bean (`weatherClock`) will throw `BeanDefinitionOverrideException` at context startup unless `spring.main.allow-bean-definition-overriding=true` is set in `@TestPropertySource`. This is test-scoped only — no production impact.
- **`/deliver` Phase 0 step 6 false-positive (mandatory lesson, 2026-05-18):** The skill's Phase 0 step 6 greps for `exec-maven-plugin`. After this delivery the plugin is gone (it only existed for the mongo:7 IT container). Future `/deliver` runs on this repo will halt at Phase 0 with "wire the plugins" until the global skill is patched. Workaround: manually confirm Failsafe is bound (`<executions>` block in `pom.xml`) and ITs run in-process (in-memory SQLite, no container needed).
