# C.A.V eSIM Management System

C.A.V is a full-stack eSIM profile management application built with Spring Boot, React, PostgreSQL, JWT authentication, and Docker.

The project demonstrates backend development, frontend integration, database management, automated testing, containerization, and CI/CD.

**Project purpose:** A locally runnable portfolio and internship demonstration project. Public internet deployment is not currently required.

## Features

- JWT-based authentication
- Role-based authorization (`USER` / `ADMIN`)
- eSIM profile creation and management
- Profile lifecycle management:
  - CREATED
  - DOWNLOADING
  - DOWNLOADED
  - ENABLED
  - FAILED
- Profile filtering, sorting, and pagination
- Profile detail view
- Profile update and deletion
- Centralized API error handling
- Database migrations with Flyway
- Swagger / OpenAPI documentation in development mode
- Dockerized frontend, backend, and PostgreSQL

## Technology Stack

**Backend:** Java 21, Spring Boot 4, Spring Security, Spring Data JPA, PostgreSQL, Flyway, JWT, Maven, JUnit, Mockito

**Frontend:** React, Vite, React Router, JavaScript, CSS, Nginx

**Infrastructure:** Docker, Docker Compose, GitHub Actions, GitHub Container Registry (GHCR)

## Architecture

```text
Browser
   |
   v
React Frontend / Nginx
   |
   | /api requests
   v
Spring Boot REST API
   |
   | JPA / Hibernate
   v
PostgreSQL
```

Nginx forwards `/api` requests to the backend container. This allows the frontend to use a relative API URL instead of a hard-coded backend hostname.

## User Roles

### USER

Users can:

- Log in
- View profiles
- Filter and sort profiles
- Navigate between pages
- View profile details

### ADMIN

Administrators can additionally:

- Create profiles
- Update profiles
- Delete profiles
- Start profile download
- Complete profile download
- Enable profiles

Newly registered accounts receive the `USER` role by default.

## Running the Project Locally

### 1. Prerequisites

Install Docker Desktop and ensure its Docker Engine is running.

### 2. Folder structure

Clone the frontend and backend repositories so that they are located next to each other:

```text
Desktop/
|-- cav-backend/
|   `-- cav-backend/
|       |-- compose.yaml
|       `-- compose.deploy.yaml
`-- cav-frontend/
```

Run the following commands from the **inner `cav-backend` directory**, where `compose.yaml` is located.

### 3. Environment variables

Create a `.env` file in the backend project using `.env.example` as a template.

Example:

```env
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password
JWT_SECRET=your_base64_jwt_secret
SPRING_PROFILES_ACTIVE=dev
```

Use your own values. Do not commit the `.env` file or share real credentials.

### 4. Create the Docker volume

For the initial setup, create the named PostgreSQL volume:

```bash
docker volume create cav-postgres-data
```

This command is safe to run again if the volume already exists.

### 5. Start the application

```bash
docker compose up -d --build
```

Check container status:

```bash
docker compose ps
```

The application will be available at:

| Service | Address |
|---|---|
| Frontend | http://localhost:5173 |
| Backend | http://localhost:8080 |
| PostgreSQL | localhost:5433 |

Open the frontend in your browser and sign in with an existing account.

**Note:** On a fresh database, you must register an account first. Newly registered accounts have the `USER` role.

## Running the Published Docker Images

The project also includes `compose.deploy.yaml`, which uses Docker images published to GitHub Container Registry instead of building them from source.

From the backend directory:

```bash
docker compose -f compose.deploy.yaml pull
docker compose -f compose.deploy.yaml up -d
docker compose -f compose.deploy.yaml ps
```

Open:

```text
http://localhost:5174
```

This environment:

- Uses the published backend and frontend Docker images
- Runs the backend with the `prod` Spring profile
- Uses a separate PostgreSQL volume
- Does not share user accounts or profiles with the standard local environment

The `prod` profile disables Swagger UI and enables production-specific application settings.

**This is a local deployment demonstration, not a publicly hosted application.**

## API Documentation

With the `dev` profile active, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger is disabled in the `prod` profile.

## Testing

Run the backend test suite on Windows:

```cmd
mvnw.cmd clean test
```

The backend includes unit, controller, repository, security, and integration tests.

The frontend provides linting and a production build:

```bash
npm ci
npm run lint
npm run build
```

Run these frontend commands from the `cav-frontend` directory.

## CI/CD

Both repositories use GitHub Actions.

**Backend pipeline:**

```text
Push to main
    |
    v
Backend CI: Maven tests
    |
    v
Backend Docker Publish
    |
    v
GitHub Container Registry
```

**Frontend pipeline:**

```text
Push to main
    |
    v
Frontend CI: ESLint + Vite build
    |
    v
Frontend Docker Publish
    |
    v
GitHub Container Registry
```

Published images:

```text
ghcr.io/giray-subasi/cav-backend:latest
ghcr.io/giray-subasi/cav-frontend:latest
```

The CI/CD pipelines automate testing, building, and image publishing. They do not deploy the application to a public server.

## Repositories

**Backend:** https://github.com/Giray-Subasi/cav-backend

**Frontend:** https://github.com/Giray-Subasi/cav-frontend

## Security

- Database credentials and JWT secrets are supplied through environment variables.
- Real secrets must not be committed to Git.
- Authentication uses JWT bearer tokens.
- Authorization distinguishes between `USER` and `ADMIN` roles.
- The backend's allowed CORS origins can be configured through `CORS_ALLOWED_ORIGINS`.

## Project Status

The main application functionality is complete.

Verified locally:

- Full-stack application startup with Docker Compose
- Frontend-to-backend communication through Nginx
- User registration and login
- Dashboard and profile retrieval
- Backend test pipeline
- Frontend lint and build pipeline
- Automated Docker image publishing
- Local deployment using published images and the `prod` profile

Current focus: reproducible local setup, documentation, and internship demonstration.