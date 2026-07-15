# Sistema de Gestion Escolar - Backend

API REST en Spring Boot (Java 21) para administrar Estudiantes, Profesores y Cursos. Ver el diseño completo en [`../SDD-Backend.md`](../SDD-Backend.md).

## Stack

Java 21, Spring Boot 3.4.1, Spring Data JPA, PostgreSQL 16 (Docker), Flyway, springdoc-openapi (Swagger), Gradle.

## Requisitos

- JDK 21
- Docker (o Docker Desktop / OrbStack / Colima) para PostgreSQL

> **Si tienes varias versiones de JDK instaladas** y `./gradlew` falla al arrancar el daemon (error críptico tipo solo un número de versión, p. ej. `26.0.1`), crea un `backend/gradle.properties` local (no versionado) con:
> ```properties
> org.gradle.java.home=/ruta/a/tu/jdk-21
> ```
> En macOS puedes obtener esa ruta con `/usr/libexec/java_home -v 21`.

## Levantar en local (app fuera de Docker, DB en Docker)

```bash
docker compose up -d postgres
./gradlew bootRun
```

La app queda en `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui/index.html`. Spec OpenAPI: `http://localhost:8080/v3/api-docs`.

> **Nota de puertos:** el `docker-compose.yml` mapea Postgres al puerto **5433** del host (no 5432), porque en varias máquinas de desarrollo ya hay un PostgreSQL local (Homebrew, etc.) escuchando en 5432, y eso haría que la app se conectara silenciosamente al Postgres equivocado en vez del contenedor. Si tu máquina no tiene ese conflicto, puedes cambiar el mapeo a `5432:5432` y ajustar el puerto en `application.yml`.

## Levantar todo con Docker (app + DB dockerizadas)

```bash
docker compose up -d --build
```

## Variables de entorno

Copia `.env.example` a `.env` y ajusta `DB_PASSWORD` si quieres una contraseña distinta a la de desarrollo por defecto.

## Tests

```bash
./gradlew test
```

Los tests de integración usan Testcontainers (levantan su propio PostgreSQL efímero), así que requieren Docker corriendo.

## Endpoints principales

| Recurso | Base path |
|---|---|
| Estudiantes | `/api/v1/estudiantes` |
| Profesores | `/api/v1/profesores` |
| Cursos | `/api/v1/cursos` |
| Inscripcion | `POST/DELETE /api/v1/cursos/{cursoId}/estudiantes/{estudianteId}` |

Ver el detalle completo de contratos, códigos de error y decisiones de diseño en el [SDD](../SDD-Backend.md).
