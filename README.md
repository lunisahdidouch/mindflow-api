# MindFlow Backend

Spring Boot backend for MindFlow. It handles authentication, persistence, AI orchestration, and retrieval-augmented tutor chat.

## Responsibilities

- JWT-based authentication and user isolation.
- Study set persistence in PostgreSQL.
- Flyway migrations including `pgvector` setup.
- xAI Grok integration for:
  - document processing
  - practice tests
  - learning plans
  - answer grading
  - tutor chat
- Gemini embeddings integration for semantic retrieval over document chunks.

## Stack

- Spring Boot `3.5.7`
- Spring Web
- Spring Security
- Spring Data JPA
- Flyway
- PostgreSQL
- Hibernate JSON + vector support

## Configuration

The backend reads configuration from [src/main/resources/application.yml](/Users/Lunis/Documents/Mindflow-rebuild/mindflow-api/src/main/resources/application.yml).

Sensitive values are injected from environment variables:

- `XAI_API_KEY`
- `XAI_MODEL`
- `GEMINI_API_KEY`
- `JWT_SECRET`

Example values are documented in [mindflow-api/.env.example](/Users/Lunis/Documents/Mindflow-rebuild/mindflow-api/.env.example).

Important: Spring Boot does not load `.env` automatically. Set these variables in PowerShell, your IDE, or your OS environment variables.

PowerShell example:

```powershell
$env:XAI_API_KEY="your-xai-key"
$env:XAI_MODEL="grok-4-1-fast-reasoning"
$env:GEMINI_API_KEY="your-gemini-key"
$env:JWT_SECRET="change-this-secret-change-this-secret-change-this-secret"
./mvnw spring-boot:run
```

## Local Database

Expected database:

- URL: `jdbc:postgresql://localhost:5432/mindflow`
- Username: `postgres`
- Password: `password`

Start it from the repo root:

```powershell
docker compose up -d
```

## Running

```powershell
./mvnw spring-boot:run
```

Run tests:

```powershell
./mvnw test
```

## Package Overview

- `ai/`: AI request orchestration, chunking, sanitization, Grok client, Gemini embeddings.
- `common/`: shared exception handling.
- `config/`: typed properties and app configuration.
- `security/`: JWT filter and Spring Security configuration.
- `study/`: entities, repositories, DTOs, and read services.
- `user/`: auth flow, user entity, repository, and service.

## Core Files

- App entry: [src/main/java/lunis/work/mindflow/MindflowApplication.java](/Users/Lunis/Documents/Mindflow-rebuild/mindflow-api/src/main/java/lunis/work/mindflow/MindflowApplication.java)
- Security: [src/main/java/lunis/work/mindflow/security/SecurityConfig.java](/Users/Lunis/Documents/Mindflow-rebuild/mindflow-api/src/main/java/lunis/work/mindflow/security/SecurityConfig.java)
- AI controller: [src/main/java/lunis/work/mindflow/ai/AiController.java](/Users/Lunis/Documents/Mindflow-rebuild/mindflow-api/src/main/java/lunis/work/mindflow/ai/AiController.java)
- Study set controller: [src/main/java/lunis/work/mindflow/study/StudySetController.java](/Users/Lunis/Documents/Mindflow-rebuild/mindflow-api/src/main/java/lunis/work/mindflow/study/StudySetController.java)
- Migration: [src/main/resources/db/migration/V1__init_mindflow_schema.sql](/Users/Lunis/Documents/Mindflow-rebuild/mindflow-api/src/main/resources/db/migration/V1__init_mindflow_schema.sql)

## API Overview

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`

Response:

```json
{
  "token": "jwt-token",
  "user": {
    "id": 1,
    "email": "student@example.com"
  }
}
```

### Study Sets

- `GET /api/study-sets`
- `GET /api/study-sets/{id}`
- `GET /api/study-sets/{id}/flashcards`
- `GET /api/study-sets/{id}/quiz`
- `GET /api/study-sets/{id}/practice-tests/latest`
- `GET /api/study-sets/{id}/learning-plans/latest`
- `GET /api/study-sets/{id}/chat-messages`

### AI

- `POST /api/ai/process-document`
- `POST /api/ai/practice-test`
- `POST /api/ai/learning-plan`
- `POST /api/ai/grade-question`
- `POST /api/ai/chat`

## RAG Notes

- Documents are chunked with overlap before embedding.
- Embeddings are created with Gemini and normalized before storage.
- Tutor chat embeds the user message, retrieves the nearest chunks, injects them into the system prompt, and then calls Grok.

## Important Notes

- All data access is scoped to the authenticated user ID.
- `process-document` expects extracted text, not raw files. The current frontend sends extracted text from PDF, TXT, and Markdown sources.
- Running `./mvnw test` will connect to your local database and apply Flyway migrations if needed.
