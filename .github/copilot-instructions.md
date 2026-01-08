<!-- Copilot instructions for AI coding agents working on FinEd Mentor -->
# FinEd Mentor — Copilot instructions

Purpose: Quickly orient an AI coding agent so it can be productive editing, debugging, and extending this repository.

- **Big picture**: This is a single‑repo full‑stack app with a Spring Boot backend (`backend/`) and an Angular frontend (`frontend/`). Data is stored in MongoDB. AI/chat features use Google GenAI via Spring AI and a small third‑party `Tavily` client.

- **Service boundaries**:
  - Backend: `backend/` — Spring Boot application (main class `backend/src/main/java/com/fined/mentor/MentorApplication.java`). REST API root is `/api`.
  - Frontend: `frontend/` — Angular SPA (source: `frontend/src/`). Environment API URL: `frontend/src/environments/environment.ts`.
  - Infra: `docker-compose.yml` for local compose, `infrastructure/` for k8s manifests.

- **Key integration points & env vars**:
  - MongoDB: `MONGO_URI` (used in `backend/src/main/resources/application.properties`).
  - Google GenAI: `GOOGLE_GENAI_API_KEY` — Spring AI starter configured in `pom.xml` and `application.properties`.
  - Tavily API: `TAVILY_API_KEY` — client under `backend/src/main/java/com/fined/mentor/tavily`.
  - JWT: `JWT_SECRET` (see `application.properties` and `auth` package).
  - Email: `EMAIL_USERNAME` / `EMAIL_PASSWORD` for SMTP (Gmail settings exist in properties).

- **How to run locally (developer flow)**
  - Backend (Windows PowerShell):
    - With wrapper: `cd backend; .\mvnw.cmd spring-boot:run` (uses `application.properties` and env vars).
    - Or build jar and run: `cd backend; .\mvnw.cmd -DskipTests package; java -jar target/mentor-0.0.1-SNAPSHOT.jar`.
  - Frontend:
    - `cd frontend; npm install` (or `npm ci`), then `npm start` (runs `ng serve`).
  - Full stack via compose: `docker-compose up --build` at repo root (reads env vars for backend).

- **Build & test commands**
  - Backend build: `cd backend; .\mvnw.cmd spring-boot:run` or `.\mvnw.cmd package`; tests: `.\mvnw.cmd test`.
  - Frontend build: `cd frontend; npm run build` (production). Tests: `npm test`.

- **Project-specific conventions & patterns**
  - Java package root: `com.fined.mentor`. Modules are organized by feature: `auth`, `chat`, `quiz`, `core`.
  - API responses often use an `ApiResponse<T>` wrapper (see `backend/src/main/java/com/fined/mentor/core/dto/ApiResponse.java`). Follow that pattern for new controllers.
  - Persistence: Spring Data MongoDB repositories in `*repository` packages; entities live in `*entity` packages.
  - Exceptions: use feature-specific exception classes (e.g., `chat.exception`, `quiz.exception`) and global handlers in `core.exception`.
  - DTOs: separate `dto` packages (request/response shapes). Keep controllers thin — business logic lives in `service` implementations.
  - Frontend: token is stored in `localStorage` keys `jwtToken` and `currentUser` (see `AuthService`). Requests attach `Authorization: Bearer <token>` via `AuthInterceptor`.

- **Files worth reading for orientation** (shortlist)
  - `backend/src/main/java/com/fined/mentor/MentorApplication.java` — app entry.
  - `backend/pom.xml` and `backend/HELP.md` — build and dependency notes (Spring-AI, MongoDB drivers).
  - `backend/src/main/resources/application.properties` — runtime config and required env vars.
  - `backend/src/main/java/com/fined/mentor/auth` — authentication, JWT and mail flow.
  - `backend/src/main/java/com/fined/mentor/chat` and `quiz` — how AI chat and quiz generation are structured.
  - `frontend/src/environments/environment.ts` — API base URL.
  - `frontend/src/app/core/services` and `frontend/src/app/core/interceptors` — HTTP and auth patterns.
  - `docker-compose.yml`, `backend/Dockerfile`, `frontend/Dockerfile` — containerization and local compose flow.

- **Debugging tips**
  - Backend logs are set to DEBUG for key packages in `application.properties` (`com.fined.mentor`, `org.springframework.security`). Use these to trace auth and request flow.
  - If AI responses behave unexpectedly, check Spring AI model config in `application.properties` (`spring.ai.google.genai.chat.options.*`) and validate `GOOGLE_GENAI_API_KEY` scope and quotas.

- **When editing code, follow these practical rules for changes**
  - Preserve the `ApiResponse` wrapper unless a new endpoint intentionally returns raw data.
  - Add new DB fields as explicit fields in entity classes and ensure repository queries cover them.
  - For feature branches that touch auth or token handling, run a local end-to-end via `docker-compose up` to validate cross-service integration.

If anything here is unclear or you want more detail (e.g., sequence diagrams, common test patterns, or examples to implement a new Quiz/Chat endpoint), tell me which part to expand.
