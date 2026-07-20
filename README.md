# Recommendation Engine

Modern, full-stack movie recommendation platform built with Spring Boot (Java) and a Python agent. Users register, choose favorite genres, rate movies, and receive curated recommendations powered by TMDB data and Azure OpenAI.

## Overview
- Spring Boot web app with Thymeleaf UI and Spring Security
- MongoDB for users and cached recommendations
- TMDB integration for movie metadata and trending lists
- Python agent for hybrid recommendation strategy (collaborative + content-based + trending) orchestrated by an LLM (Azure OpenAI)
- Async scheduling for periodic batch recomputation

## Features
- User auth (username or email) and profile
- Onboarding questionnaire to select favorite genres
- Initial rating form and ongoing rating updates
- Search and detailed movie view
- Personalized Home with three recommendation sections
- Manual and scheduled triggers for the Python agent

## Architecture
- `backend-java`: Spring Boot application
  - MVC controllers for pages and JSON endpoints
  - Services for TMDB, users, and agent orchestration
  - Mongo repositories for `users` and `recommended_cache`
- `python_agent`: Data mining and LLM curation
  - Pulls user profiles and ratings from MongoDB
  - Calls TMDB for details and related titles
  - Uses Azure OpenAI to select and rank items into sections
  - Saves back to MongoDB for the Java app to render

## Prerequisites
- Java 17
- Python 3.10+ with `pip`
- MongoDB instance
- TMDB API key
- Azure OpenAI credentials (endpoint, API key, deployment name)

## Configuration

### Spring Boot env (`.env` at repo root)
Copy `.env.example` to `.env` and set values:

```env
SPRING_APPLICATION_NAME=recommendatio-engine
MONGODB_URI=mongodb+srv://<user>:<pass>@<cluster>/<db>?retryWrites=true&w=majority
TMDB_API_KEY=<tmdb_api_key>
TMDB_API_BASEURL=https://api.themoviedb.org/3
APP_PYTHON_COMMAND=python3    # or python on Windows
APP_PYTHON_SCRIPTHPATH=./python_agent/batch_processor.py
```

Notes:
- `APP_PYTHON_SCRIPTHPATH` is relative to the Java process working directory.
- Ensure the MongoDB recommendation collection name matches Java: set Python `RECOMMENDATION_COLLECTION` to `recommended_cache`.

### Python agent env (`backend-java/python_agent/.env`)
Copy `backend-java/python_agent/.env.example` to `.env` and set values:

```env
AZURE_OPENAI_API_KEY=<azure_api_key>
AZURE_OPENAI_API_VERSION=2025-01-01-preview
AZURE_OPENAI_ENDPOINT=https://<your-resource>.openai.azure.com
TMDB_API_KEY=<tmdb_api_key>
MONGO_URI=mongodb+srv://<user>:<pass>@<cluster>/<db>?retryWrites=true&w=majority
DB_NAME=<db_name>
COLLECTION_NAME_USERS=users
RECOMMENDATION_COLLECTION=recommended_cache
```

## Run Locally

### With Maven Wrapper
```bash
cd backend-java
./mvnw spring-boot:run        # macOS/Linux
# or
mvnw.cmd spring-boot:run       # Windows
```
App listens on `http://localhost:8080`.

### VS Code
- Uses `.vscode/launch.json`, which loads env from `./.env`
- Start: Run and Debug → Spring Boot-RecommendatioEngineApplication

### Docker
Build and run the Spring + Python container (Java 17 + Python 3):
```bash
docker build -t recommendation-engine backend-java
docker run --rm -p 8080:8080 \
  --env-file ./.env \
  -e AZURE_OPENAI_API_KEY=<key> \
  -e AZURE_OPENAI_API_VERSION=2025-01-01-preview \
  -e AZURE_OPENAI_ENDPOINT=https://<resource>.openai.azure.com \
  -e TMDB_API_KEY=<tmdb_key> \
  -e MONGO_URI=<mongo_uri> \
  -e DB_NAME=<db_name> \
  -e RECOMMENDATION_COLLECTION=recommended_cache \
  recommendation-engine
```

## Key Endpoints
- Pages: `GET /login`, `GET/POST /register`, `GET /home`, `GET /profile`, `GET/POST /search`, `GET /movie/{title}`
- Ratings: `POST /api/rate`, `POST /rate-movie`, `POST /search/rate-movie`
- Onboarding: `GET/POST /questionnaire`, `GET/POST /rating-form`, `POST /rating-form/save-initial-ratings`, `GET /rating-form/api/search`
- Agent trigger: `POST /api/trigger-demo-agent` with `userId`

## Scheduling
- Biweekly Sundays 1:00 AM (`@Scheduled(cron = "0 0 1 * * SUN")`), runs only on even weeks.
- Adjust or disable in `RecommendationScheduler.java` if not desired.

## Data Model
- `users` collection: username, email, bcrypt password, `favoriteGenres`, `movieRatings`
- `recommended_cache` collection: sections with curated movies and reasons

## Project Structure
```
recommendatio engine/
├─ backend-java/
│  ├─ src/main/java/com/project/recommendation_engine/
│  │  ├─ config/        # Security, async
│  │  ├─ controller/    # MVC + JSON endpoints
│  │  ├─ model/         # Mongo documents
│  │  ├─ repository/    # Spring Data Mongo
│  │  ├─ scheduler/     # Batch agent trigger
│  │  └─ service/       # TMDB, users, agent orchestration
│  ├─ src/main/resources/  # templates, static, application.properties
│  ├─ python_agent/        # batch_processor.py, data_tools.py, llm_recommender.py
│  ├─ Dockerfile
│  └─ pom.xml
└─ .env.example
```

## Development
- Tests: `cd backend-java && ./mvnw test`
- Sensitive config is excluded by `.gitignore` (`.env` files). Do not commit secrets.

## Troubleshooting
- Python not found: set `APP_PYTHON_COMMAND` to `python` on Windows or `python3` on macOS/Linux.
- Script path issues: verify `APP_PYTHON_SCRIPTHPATH=./python_agent/batch_processor.py` from the Java working dir.
- No recommendations: ensure `RECOMMENDATION_COLLECTION` is `recommended_cache`, Mongo credentials are valid, and Azure OpenAI envs are set.

## License
Proprietary or internal use; add a license if neede
