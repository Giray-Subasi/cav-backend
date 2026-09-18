
# C.A.V eSIM Management System

C.A.V is a full-stack eSIM profile management application built with Spring Boot, React, PostgreSQL, JWT authentication, and Docker.

The project demonstrates backend development, frontend integration, database management, automated testing, containerization, and CI/CD.

**Project purpose:** A locally runnable portfolio and internship demonstration project. Public internet deployment is not currently required.

## Features

- User registration and login
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
- Reusable script for creating fictional demo profiles

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

- Register and log in
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

Newly registered accounts receive the `USER` role by default. Public registration does not grant administrator access.

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
|       |-- compose.deploy.yaml
|       `-- scripts/
|           `-- seed-demo-profiles.ps1
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

Open http://localhost:5173/login in your browser.

On a fresh database, select **Create an account** to register a user, then sign in. Newly registered accounts have the `USER` role. An existing administrator account is required for administrator-only operations.

## Preparing Fictional Demo Profiles

The backend repository includes a reusable PowerShell script:

```text
scripts/seed-demo-profiles.ps1
```

The script prepares up to **14 fictional eSIM profiles** through the application's existing REST API. It uses the supported operators `TURKCELL`, `VODAFONE`, and `TURK_TELEKOM`.

### What the script does

- Connects to the **development backend at `http://localhost:8080`**.
- Prompts for an existing development `ADMIN` account's username and password.
- Checks each example profile by its ICCID before attempting to create it.
- Creates a profile only when that ICCID does not already exist.
- Skips existing profiles, including profiles whose other fields differ from the example data.
- Leaves existing profile details and lifecycle states unchanged.
- Creates new profiles in the `CREATED` state.

The script does **not** delete profiles or reset the database. It does not store the administrator password in the script.

**Important:** The script targets the standard development environment associated with `localhost:5173`. It does not populate the separate published-image demo environment at `localhost:5174`.

### Run the script on Windows

Start the development environment first:

```cmd
docker compose up -d
docker compose ps
```

From the inner backend directory, run:

```cmd
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\seed-demo-profiles.ps1"
```

When prompted, enter the username and password of an existing **development ADMIN account**. Do not use the credentials of an account that exists only in the separate `5174` environment.

The `-ExecutionPolicy Bypass` option applies to the PowerShell process started by this command; it does not permanently change the system's execution policy. Use it only after reviewing and trusting the local script.

### Re-running the script

The script can be run again without recreating profiles that already exist.

For example, a successful repeat run with all 14 example ICCIDs present produces:

```text
Finished. Created: 0 | Skipped: 14
```

An existing ICCID with different EID or operator data is also skipped rather than overwritten.

The development database may contain other test profiles, so the **total number of profiles in the dashboard is not necessarily 14** after running the script.

This initial version creates missing profiles only. It does not automatically distribute them across lifecycle states.

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

### Locally verified workflows

- User registration and login
- USER and ADMIN interfaces
- Profile creation and lifecycle transitions
- Profile filtering, sorting, and pagination
- Read-only profile details for USER accounts
- Backend rejection of an unauthorized USER lifecycle request with HTTP `403`
- Persistence of demo profiles after restarting the local Docker environment
- Repeat execution of the demo profile script without duplicate creation

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
- Public registration creates `USER` accounts only.
- The backend's allowed CORS origins can be configured through `CORS_ALLOWED_ORIGINS`.
- Demo profiles use fictional data rather than real subscriber identifiers.

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
- Repeatable creation of missing fictional demo profiles

Current focus: reproducible local setup, documentation, and internship demonstration.