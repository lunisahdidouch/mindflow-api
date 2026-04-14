# MindFlow Backend

Spring Boot backend for MindFlow. It handles authentication, persistence, AI orchestration, and retrieval-augmented tutor chat.

## Responsibilities

- JWT-based authentication and user isolation.
- Study set persistence in PostgreSQL.
- Flyway migrations including `pgvector` setup.
- runtime-selectable LLM integration for:
  - document processing
  - practice tests
  - learning plans
  - answer grading
  - tutor chat
- SambaNova MiniMax-M2.5 as the default configured provider
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

- `MINDFLOW_AI_PROVIDER`
- `SAMBANOVA_API_KEY`
- `SAMBANOVA_BASE_URL`
- `SAMBANOVA_MODEL`
- `XAI_API_KEY`
- `XAI_MODEL`
- `GEMINI_API_KEY`
- `JWT_SECRET`

Example values are documented in [mindflow-api/.env.example](/Users/Lunis/Documents/Mindflow-rebuild/mindflow-api/.env.example).

Important: Spring Boot does not load `.env` automatically. Set these variables in PowerShell, your IDE, or your OS environment variables.

For local development the recommended approach is `src/main/resources/application-local.yml` plus `SPRING_PROFILES_ACTIVE=local`.

Example `application-local.yml` for the default SambaNova setup:

```yml
mindflow:
  ai:
    provider: sambanova

  sambanova:
    base-url: "YOUR_SAMBANOVA_BASE_URL"
    model: "MiniMax-M2.5"
    api-key: "YOUR_SAMBANOVA_API_KEY"

  gemini:
    base-url: https://generativelanguage.googleapis.com
    model: gemini-embedding-001
    api-key: "YOUR_GEMINI_API_KEY"

  jwt:
    secret: "your-32-plus-character-secret-here"
    expiration: PT12H
```

If you want to run with xAI instead:

```yml
mindflow:
  ai:
    provider: xai

  xai:
    base-url: https://api.x.ai
    model: grok-4-1-fast-reasoning
    api-key: "YOUR_XAI_API_KEY"
```

PowerShell example:

```powershell
$env:MINDFLOW_AI_PROVIDER="sambanova"
$env:SAMBANOVA_API_KEY="your-sambanova-key"
$env:SAMBANOVA_BASE_URL="your-sambanova-base-url"
$env:SAMBANOVA_MODEL="MiniMax-M2.5"
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
$env:SPRING_PROFILES_ACTIVE="local"
./mvnw spring-boot:run
```

Run tests:

```powershell
./mvnw test
```

## Package Overview

- `ai/`: AI request orchestration, provider selection, xAI client, SambaNova client, chunking, sanitization, Gemini embeddings.
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
- `DELETE /api/study-sets/{id}`
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
- Tutor chat embeds the user message, retrieves the nearest chunks, injects them into the system prompt, and then calls the configured chat provider.

## Provider Notes

- `mindflow.ai.provider=sambanova` is the default.
- SambaNova uses JSON mode for structured endpoints such as document processing and grading.
- xAI remains available as an alternate provider by switching `mindflow.ai.provider=xai`.
- You still need to provide the real SambaNova base URL from your SambaNova account or dashboard before the app can start with the default provider.
- If the app starts with the `default` Spring profile instead of `local`, your local YAML overrides will not be loaded.

## Important Notes

- All data access is scoped to the authenticated user ID.
- `process-document` expects extracted text, not raw files. The current frontend sends extracted text from PDF, TXT, and Markdown sources.
- Running `./mvnw test` will connect to your local database and apply Flyway migrations if needed.
- Required information you must provide locally for the default setup:
  - SambaNova base URL
  - SambaNova API key
  - Gemini API key
  - a JWT secret with at least 32 characters
