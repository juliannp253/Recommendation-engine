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
SPRING_MONGO_URI=mongodb://localhost:27017/recsysdb
TMDB_API_KEY=<tmdb_api_key>
TMDB_API_BASEURL=https://api.themoviedb.org/3
APP_PYTHON_COMMAND=python3    # or python on Windows
APP_PYTHON_SCRIPT_PATH=./python_agent/batch_processor.py
AZURE_OPENAI_API_KEY=<azure_api_key>
AZURE_OPENAI_API_VERSION=2025-01-01-preview
AZURE_OPENAI_ENDPOINT=https://<your-resource>.openai.azure.com
```

Notes:
- `APP_PYTHON_SCRIPT_PATH` is relative to the Java process working directory.
- Docker Compose loads this file from the repository root. It connects Java and Python to the MongoDB service automatically.
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
  -e SPRING_MONGO_URI=<mongo_uri> \
  -e MONGO_URI=<mongo_uri> \
  -e DB_NAME=<db_name> \
  -e RECOMMENDATION_COLLECTION=recommended_cache \
  recommendation-engine
```

For the full stack, place `.env` at the repository root and run `docker compose up -d --build` from `backend-java`. The application listens on port 8080; MongoDB and Redis are available only to the containers. Check `http://localhost:8080/login` after startup.

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

## CI and EC2 deployment
- CI runs on pushes to `main`, `feature/**`, and `refactor/**`, and on pull requests targeting `main`.
- Protect `main` in GitHub and require the `build-and-test` status check before merging. This setting lives in the repository's branch rules, outside the workflow files.
- Configure repository secrets `EC2_HOST`, `EC2_USER`, and `EC2_SSH_KEY`.
- On EC2, clone the repository at `~/Recommendation-engine`, install Docker with Compose, allow the SSH user to run Docker, and place a populated `.env` at the repository root. Keep this file out of Git.
- Allow inbound TCP port 8080 on the EC2 security group if the login page must be reachable publicly. The deployment checks `http://<EC2_HOST>:8080/login` after the containers start.

## Troubleshooting
- Python not found: set `APP_PYTHON_COMMAND` to `python` on Windows or `python3` on macOS/Linux.
- Script path issues: verify `APP_PYTHON_SCRIPT_PATH=./python_agent/batch_processor.py` from the Java working dir.
- No recommendations: ensure `RECOMMENDATION_COLLECTION` is `recommended_cache`, Mongo credentials are valid, and Azure OpenAI envs are set.

## License
Proprietary or internal use; add a license if neede
