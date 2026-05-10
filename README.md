# Full-Stack Bootstrap Repo

Small full-stack repo bootstrapped for rapid feature delivery.

## Stack

- Spring Boot OpenAPI backend with MongoDB persistence
- React + MUI frontend
- Playwright end-to-end tests
- Docker Compose for local MongoDB, backend, and frontend

## Run locally

```powershell
docker compose up --build
```

Open the frontend at `http://localhost:5173`.

## Test locally

Backend:

```powershell
cd backend
mvn test
```

The backend Docker image packages with tests skipped because the MongoDB Testcontainers tests need Docker access from the Maven process.

Frontend:

```powershell
cd frontend
npm install
npm test
```

E2E:

```powershell
cd e2e
npm install
npm test
```
