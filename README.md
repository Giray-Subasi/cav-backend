# C.A.V eSIM Management System

C.A.V is a full-stack eSIM profile management application built with Spring Boot, React, PostgreSQL, JWT authentication, and Docker.

The system provides role-based access for users and administrators and supports the complete lifecycle of an eSIM profile.

## Features

- JWT-based authentication
- Role-based authorization (`USER` / `ADMIN`)
- eSIM profile creation and management
- Profile lifecycle management
  - CREATED
  - DOWNLOADING
  - DOWNLOADED
  - ENABLED
  - FAILED
- Profile filtering
- Sorting
- Pagination
- Profile detail view
- Profile update and deletion
- Centralized API error handling
- Database migrations with Flyway
- Swagger / OpenAPI documentation
- Dockerized frontend, backend, and PostgreSQL

## Technology Stack

### Backend

- Java 21
- Spring Boot 4
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- JWT
- Maven
- JUnit
- Mockito

### Frontend

- React
- Vite
- React Router
- JavaScript
- CSS
- Nginx

### Infrastructure

- Docker
- Docker Compose

## Architecture

```text
React Frontend
      |
      | HTTP / JWT
      v
Spring Boot REST API
      |
      | JPA / Hibernate
      v
PostgreSQL
```

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

## Running the Project

The frontend and backend repositories should be located next to each other:

```text
Desktop/
├── cav-backend/
│   └── cav-backend/
└── cav-frontend/
```

Create a `.env` file in the backend project based on `.env.example`.

Example:

```env
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password
JWT_SECRET=your_base64_jwt_secret
SPRING_PROFILES_ACTIVE=dev
```

Do not commit the real `.env` file.

Start the complete application with:

```bash
docker compose up -d --build
```

The services will be available at:

```text
Frontend:   http://localhost:5173
Backend:    http://localhost:8080
PostgreSQL: localhost:5433
```

## API Documentation

When the `dev` profile is active, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger is disabled in the production profile.

## Testing

Run the backend test suite with:

```bash
mvnw.cmd clean test
```

The project contains unit, controller, repository, security, and integration tests.

## Repositories

Backend:

```text
https://github.com/Giray-Subasi/cav-backend
```

Frontend:

```text
https://github.com/Giray-Subasi/cav-frontend
```

## Security

Sensitive configuration such as database passwords and JWT secrets is provided through environment variables and is not committed to Git.

Authentication is stateless and uses JWT bearer tokens.

## Project Status

The main application functionality is complete.

Current focus:

- Documentation
- CI/CD
- Deployment
- Final UI and project polish