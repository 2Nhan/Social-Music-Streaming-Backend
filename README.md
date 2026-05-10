# Micsu Music Streaming API

[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8+-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-Auth_State-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![Cloudflare R2](https://img.shields.io/badge/Cloudflare_R2-Storage-F38020?logo=cloudflare&logoColor=white)](https://www.cloudflare.com/developer-platform/r2/)

Micsu is a backend REST API for a music streaming and social interaction platform built with Spring Boot. The project focuses on audio upload, processing, and HLS streaming pipelines, while supporting social features such as user follows, likes, comments, reposts, playlists, and listening history.

## Overview

The system is designed using a layered monolith architecture with clear separation of concerns across controller, service, repository, entity, DTO, and security layers. Primary data is stored in MySQL, authentication state is managed via JWT combined with Redis, and media files are stored on Cloudflare R2 through AWS SDK v2.

## Key Features

### Authentication and Security

- User registration, login, token refresh, and logout using access token + refresh token mechanism.
- JWT validation via security filters; revoked access tokens are blocked through Redis blacklist.
- Role-based access control (RBAC) at the security layer.

### Music and Streaming

- Audio file upload with metadata persistence for playback.
- HLS-based media processing for optimized streaming.
- Audio files and HLS segments stored on Cloudflare R2.

### Social Features

- Like, comment, and repost songs.
- Follow and unfollow users.
- Listening history tracking.

### Content Organization

- Personal playlist management.
- User profile management.
- Song search and genre browsing.
- View count tracking.

## Technology Stack

- Java 21
- Spring Boot 3.4.3
- Spring Security, OAuth2 Resource Server, Nimbus JOSE + JWT
- Spring Data JPA, MySQL 8
- Spring Data Redis
- Cloudflare R2 via AWS SDK v2
- FFmpeg for HLS processing
- Lombok, MapStruct, Apache Commons
- Springdoc OpenAPI
- Micrometer Prometheus, Spring Boot Actuator
- Maven

## Project Architecture

```text
src/main/java/com/tunhan/micsu/
├── configuration/   # Application configuration
├── controller/      # REST API endpoints
├── dto/             # Request/response models
├── entity/          # Domain entities
├── exception/       # Error handling
├── repository/      # Data access layer
├── security/        # JWT, filters, authentication
├── service/         # Business logic
└── utils/           # Media, file, and token utilities
```

Main system workflows:

1. User uploads audio file.
2. Application processes media and generates HLS content.
3. Segments and playlists are stored on Cloudflare R2.
4. API returns data for client playback and metadata display.

## System Requirements

- JDK 21
- Maven 3.9+ or Maven Wrapper
- MySQL 8+
- Redis
- FFmpeg if running local media processing
- Cloudflare R2 credentials for production environment

## Environment Configuration

The application reads environment variables from `.env` file or system environment. Key variables include:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `R2_ACCOUNT_ID`
- `R2_ACCESS_KEY`
- `R2_SECRET_KEY`
- `R2_BUCKET`
- `JWT_SECRET`
- `JWT_ACCESS_EXPIRATION`
- `JWT_REFRESH_EXPIRATION`

Application runs at:

- Base URL: `http://localhost:8081/micsu`
- OpenAPI spec: `http://localhost:8081/micsu/docs/openapi.yml`
- Swagger UI: `http://localhost:8081/micsu/swagger-ui/index.html`
- Actuator health: `http://localhost:8081/micsu/actuator/health`
- Prometheus metrics: `http://localhost:8081/micsu/actuator/prometheus`

## Running Locally

### 1. Prepare `.env`

Create `.env` file in the project root and configure necessary environment variables.

### 2. Start MySQL and Redis

You can run via Docker Compose:

```bash
docker compose up -d mysql redis
```

Or start the full stack if you've configured all images and reverse proxy:

```bash
docker compose up -d
```

### 3. Run Spring Boot Application

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Running with Docker

The `docker-compose.yml` file declares the main services:

- `micsu-app`
- `mysql`
- `redis`
- `nginx`

If you want to use pre-built images, ensure the `.env` file is correct and run:

```bash
docker compose up -d
```

## API Documentation

OpenAPI documentation is served directly from the application and mounted into Spring Boot's static directory. You can open Swagger UI to explore endpoints organized by groups: auth, songs, playlists, likes, comments, follows, reposts, search, history, genres, and users.

## Redis Notes

Redis in this project is used only for authentication/security state, not as a general cache for the entire application.

- Refresh tokens are stored with key `refresh:<token> -> userId` with TTL.
- Revoked access tokens are blacklisted as `blacklist:<jti> -> 1` with TTL.
- Security filter checks the blacklist before allowing JWT to proceed through authentication flow.

## Resource Structure

```text
docs/openapi/      # OpenAPI spec source
nginx/             # Reverse proxy and SSL configuration
monitoring/        # Prometheus and monitoring setup
src/main/resources # Application config and static assets
```

## Contributing

When extending the project, maintain the current layered architecture organization. Also update the OpenAPI documentation and README when adding new endpoints, changing environment variables, or adding infrastructure services.
