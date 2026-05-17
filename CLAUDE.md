# CLAUDE.md — ai-delivery-demo

Full-stack bootstrap for rapid feature delivery. Read this before touching any code.

## Architecture

```
contracts/openapi.yaml          ← single source of truth for all API shapes
backend/                        ← Spring Boot 3.3.5 / Java 21 / Maven
  src/main/java/com/example/demo/
    api/                        ← generated (do not edit by hand)
    controller/                 ← implements generated *Api interfaces
    config/                     ← MongoConfig, WebConfig (CORS)
  src/main/resources/
    application.yml
frontend/                       ← React 18 + TypeScript + MUI + Vite
  src/
    api.ts                      ← all fetch calls live here
    App.tsx                     ← root component
e2e/                            ← Playwright smoke tests
docker-compose.yml              ← MongoDB 7 + backend + frontend
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
mvn spring-boot:run          # needs MongoDB running separately
mvn generate-sources         # regenerate API interfaces from openapi.yaml
mvn test                     # unit tests only (Surefire)
mvn verify                   # unit + integration tests (Failsafe); pom starts/stops mongo:7 on :27018 via docker CLI
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
- **MongoDB auditing** is enabled (`@EnableMongoAuditing`). Use `@CreatedDate` / `@LastModifiedDate` on documents.
- **CORS**: backend allows `GET`, `POST`, `OPTIONS` from `http://localhost:5173` on `/api/**`. Add new methods in `WebConfig` if needed.
- **Frontend env**: `VITE_API_BASE_URL` controls the API base (empty string in dev — Vite proxy handles it; set to `http://localhost:8080` in Docker).
- **Tests skip Docker in Maven build**: the `backend/Dockerfile` runs `mvn package -DskipTests`. Run `mvn verify` locally when you need backend integration test coverage.
- **Backend integration tests**: name files `*IT.java` (Failsafe convention). `exec-maven-plugin` in `pom.xml` runs `docker run mongo:7 -p 27018:27017` in `pre-integration-test` and `docker rm -f` in `post-integration-test`. Tests use `@TestPropertySource(properties = "spring.data.mongodb.uri=mongodb://localhost:27018/test")`. Do **not** use `@Testcontainers` / `@Container` — docker-java is broken on this machine's Docker Desktop named-pipe relay; shelling out to the docker CLI sidesteps it.
- **OpenAPI generator config** (in `pom.xml`): `interfaceOnly=true`, `useTags=true` — controllers implement tag-named interfaces (e.g., tag `health` → `HealthApi`).
- **PR screenshots are dropped pre-merge.** Phase 9 of `/deliver` commits UI screenshots to `screenshots/<branch>/` on the PR branch so reviewers see visual evidence inline in the PR description. Before merging, run `git rm -rf screenshots/<branch>/ && git commit -m "Pre-merge: drop PR screenshots"`. Closed-without-merge PRs need no cleanup — branch deletion takes the screenshots with it.
- **PR descriptions need absolute image URLs.** GitHub renders relative image paths in committed markdown (READMEs, files at HEAD) but **not** in PR descriptions — there, `![alt](screenshots/...png)` becomes a broken link. Phase 10 of `/deliver` must rewrite image refs to absolute `https://raw.githubusercontent.com/<owner>/<repo>/<branch>/screenshots/<branch>/<slug>.png` URLs before `gh pr create`. The screenshots still live on the branch (so the pre-merge cleanup convention above still applies); only the URL form changes.

## Known environmental quirks

- **Failsafe must be explicitly bound** (audit round 3, 2026-05-17). Spring Boot starter parent provides `pluginManagement` for `maven-failsafe-plugin` but does NOT bind goals to `integration-test`/`verify`. Without an explicit `<plugin><artifactId>maven-failsafe-plugin</artifactId>…<executions>` block in `<build><plugins>`, `*IT.java` files compile silently and never execute under `mvn verify` — `BUILD SUCCESS` is reported with zero IT coverage. Always verify Failsafe output (`failsafe:3.x.x:integration-test` line) appears in the build log when running IT.
- **`@EnableMongoAuditing` defeats autoconfigure-exclude** for tests that try to avoid MongoDB. The `mongoAuditingHandler` bean depends on `mongoMappingContext`, which is created by `@EnableMongoAuditing` on `DemoApplication` regardless of `spring.autoconfigure.exclude=…MongoDataAutoConfiguration`. For tests that don't need persistence (e.g., the health endpoint), use `@WebMvcTest(SomeController.class)` rather than `@SpringBootTest(MOCK)` — it slices the context and avoids the auditing handler entirely.
- **Bash tool CWD drifts on Windows** (audit round 4, 2026-05-17). Successive Bash tool calls do not reliably honour `cd`-then-command chains across invocations — the working directory can snap back to wherever the first call ran. Use absolute paths (`mvn -f C:/git/ai-delivery-demo/backend/pom.xml verify`) or chain commands inside a single Bash call (`cd frontend && npm test`) rather than relying on persistent state between calls.

## Adding a new feature — checklist

- [ ] Add path + schemas to `contracts/openapi.yaml`
- [ ] `cd backend && mvn generate-sources`
- [ ] Create `controller/Foo Controller.java` implementing the generated `FooApi`
- [ ] Add a `@Document` model in a new `model/` package if persistence is needed
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
| Testcontainers | 1.20.2 |
| MongoDB image | 7 |
| Node / frontend | see `frontend/package.json` |
| Playwright | see `e2e/package.json` |
