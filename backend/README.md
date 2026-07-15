# Sistema de Gestion Escolar - Backend

API REST en Spring Boot (Java 21) para administrar Estudiantes, Profesores y Cursos. Esta es la guía operativa: cómo instalar, correr, probar, y qué endpoints expone. Para entender **qué** hace el sistema y **por qué** está diseñado así (arquitectura, modelo de datos, reglas de negocio), ver [`../SDD-Backend.md`](../SDD-Backend.md).

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

Todos los tests actuales son unitarios/de capa web: los servicios se prueban con Mockito puro (mocks de los repositorios) y los controllers con `@WebMvcTest` + `@MockitoBean`. **No requieren Docker ni una base de datos corriendo.**

Para ver el reporte de cobertura (JaCoCo):

```bash
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## Endpoints principales

| Recurso | Base path |
|---|---|
| Estudiantes | `/api/v1/estudiantes` |
| Profesores | `/api/v1/profesores` |
| Cursos | `/api/v1/cursos` |
| Inscripcion | `POST/DELETE /api/v1/cursos/{cursoId}/estudiantes/{estudianteId}` |
| Auditoria (solo lectura) | `GET /api/v1/auditoria` y `GET /api/v1/auditoria?entidad=CURSO&entidadId=3` |

> **Soft delete:** los `DELETE` de Profesor, Curso y Estudiante no borran la fila, solo marcan `eliminado_en` (columna nullable; `NULL` = activo). Un recurso eliminado lógicamente responde 404 en cualquier lectura posterior, como si no existiera — la restricción se aplica automáticamente vía `@SQLRestriction` en cada entidad (`domain/Profesor.java`, `domain/Curso.java`, `domain/Estudiante.java`). El email queda libre para reutilizarse gracias a un índice único parcial (`WHERE eliminado_en IS NULL`) en vez de un `UNIQUE` de tabla completa.
>
> **Auditoría:** cada creación/actualización/eliminación de las 3 entidades (y cada inscripción/retiro de un curso) queda registrada en la tabla `auditoria`, con un snapshot en JSON del estado del recurso. Consultable de solo lectura en `GET /api/v1/auditoria` (todo el historial) o `GET /api/v1/auditoria?entidad=CURSO&entidadId=3` (historial de un recurso puntual). Ver `service/impl/AuditoriaServiceImpl.java`.

Para el diseño del sistema (qué hace, arquitectura, modelo de datos, reglas de negocio), ver [`../SDD-Backend.md`](../SDD-Backend.md).
