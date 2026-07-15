# SDD — Sistema de Gestión Escolar (Backend)

**Software Design Document**
Alcance: **solo backend** (API REST). Frontend fuera de alcance de este documento.

| | |
|---|---|
| Autor | jevojob@gmail.com |
| Fecha | 2026-07-14 |
| Versión | 1.0 |
| Stack | Java 21, Spring Boot 3.3.x, PostgreSQL 16 (Docker), springdoc-openapi (Swagger) |

---

## 1. Objetivo

Diseñar la arquitectura backend de un sistema CRUD para gestionar **Estudiantes**, **Profesores** y **Cursos** de un colegio, cumpliendo los 8 criterios de aceptación de la prueba técnica: API REST completa, validaciones con error 400, manejo explícito de eliminación de profesores con cursos asignados, relación bidireccional curso↔estudiante, error 404 para recursos inexistentes, persistencia en PostgreSQL dockerizado, arquitectura en capas con inversión de dependencias y manejo de excepciones centralizado.

## 2. Alcance y no-objetivos

**Incluye:** diseño de entidades, capas (controller/service/repository), DTOs, validación, manejo global de excepciones, contrato OpenAPI/Swagger, docker-compose de PostgreSQL, estrategia de migraciones, estructura de paquetes.

**No incluye:** frontend, autenticación/autorización (no se pide en el enunciado — se documenta como mejora futura), despliegue a la nube, notificaciones.

---

## 3. Stack tecnológico

| Componente | Elección | Motivo |
|---|---|---|
| Lenguaje | Java 21 (LTS) | Pedido por el enunciado; permite records, virtual threads si se necesitan |
| Framework | Spring Boot 3.3.x | Compatible con Java 21, ecosistema maduro |
| Persistencia | Spring Data JPA + Hibernate | Reduce boilerplate de repositorio, integra bien con PostgreSQL |
| Base de datos | PostgreSQL 16 (contenedor Docker) | Obligatorio por enunciado |
| Migraciones | Flyway | Versiona el esquema, evita `ddl-auto: update` en un entregable serio |
| Validación | Jakarta Bean Validation (`spring-boot-starter-validation`) | Validación declarativa de DTOs de entrada |
| Documentación API | springdoc-openapi-starter-webmvc-ui | Genera Swagger UI + spec OpenAPI 3 automáticamente |
| Mapeo DTO↔Entidad | MapStruct | Mapeo compilado, sin reflection, evita mappers manuales repetitivos |
| Reducción boilerplate | Lombok | Getters/setters/constructores en entidades y DTOs |
| Build | Maven | Estándar, buen soporte de plugins Docker |
| Testing | JUnit 5, Mockito, Testcontainers (PostgreSQL) | Unit tests de service con mocks; tests de integración contra Postgres real |

---

## 4. Arquitectura

### 4.1 Arquitectura en 3 capas + inversión de dependencias

```
┌─────────────────────────────────────────────────────────┐
│  Controller (capa de presentación)                      │
│  - Recibe HTTP, valida entrada (DTO + @Valid)            │
│  - Traduce a/desde DTOs                                  │
│  - Depende de interfaces de Service (no de impl)         │
└───────────────────────┬───────────────────────────────────┘
                         │ depende de (interfaz)
┌───────────────────────▼───────────────────────────────────┐
│  Service (capa de negocio)                               │
│  - Interfaz: contrato de negocio (p.ej. EstudianteService)│
│  - Impl: reglas de negocio, orquestación, transacciones   │
│  - Depende de interfaces de Repository (Spring Data JPA)  │
└───────────────────────┬───────────────────────────────────┘
                         │ depende de (interfaz)
┌───────────────────────▼───────────────────────────────────┐
│  Repository (capa de persistencia)                        │
│  - Interfaces Spring Data JPA (ya son abstracciones)       │
│  - Entities JPA (modelo de dominio/persistencia)           │
└─────────────────────────────────────────────────────────┘
```

**Regla de dependencia:** las capas superiores dependen de **abstracciones** (interfaces) de las capas inferiores, nunca de implementaciones concretas. Spring resuelve la implementación en tiempo de ejecución vía **inyección de dependencias por constructor** (constructor injection, campos `private final`, sin `@Autowired` en campos).

Esto es lo que da la **D** de SOLID (DIP): `EstudianteController` depende de `EstudianteService` (interfaz), no de `EstudianteServiceImpl`. Esto permite:
- Sustituir la implementación (p.ej. para tests, usar un mock/fake) sin tocar el controller.
- Desacoplar el "qué" (contrato) del "cómo" (implementación).

### 4.2 Aplicación del resto de SOLID

| Principio | Aplicación concreta |
|---|---|
| **S**RP | Cada clase tiene una responsabilidad: controller = HTTP, service = negocio, repository = persistencia, mapper = conversión DTO↔entidad, exception handler = traducción de errores |
| **O**CP | Las reglas de negocio nuevas se agregan implementando/extendiendo servicios sin modificar controllers; validaciones custom vía `jakarta.validation` sin tocar lógica existente |
| **L**SP | Las implementaciones (`*ServiceImpl`) cumplen íntegramente el contrato de su interfaz; no se lanzan excepciones no declaradas en el contrato ni se debilitan postcondiciones |
| **I**SP | Interfaces de servicio segregadas por entidad (`EstudianteService`, `ProfesorService`, `CursoService`) en vez de una interfaz genérica `CrudService` gigante; cada una expone solo lo que su controller necesita |
| **D**IP | Ver 4.1. Controllers y Services dependen de interfaces, no de clases concretas |

---

## 5. Estructura de carpetas (paquetes)

Paquete base sugerido: `com.colegio.gestion`

```
gestion-escolar-backend/
├── src/
│   ├── main/
│   │   ├── java/com/colegio/gestion/
│   │   │   ├── GestionEscolarApplication.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java          # Bean de configuración Swagger/OpenAPI
│   │   │   │   └── JpaAuditingConfig.java      # (opcional) auditoría created_at/updated_at
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── EstudianteController.java
│   │   │   │   ├── ProfesorController.java
│   │   │   │   └── CursoController.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── EstudianteService.java      # interfaz
│   │   │   │   ├── ProfesorService.java        # interfaz
│   │   │   │   ├── CursoService.java           # interfaz
│   │   │   │   └── impl/
│   │   │   │       ├── EstudianteServiceImpl.java
│   │   │   │       ├── ProfesorServiceImpl.java
│   │   │   │       └── CursoServiceImpl.java
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── EstudianteRepository.java   # extends JpaRepository
│   │   │   │   ├── ProfesorRepository.java
│   │   │   │   └── CursoRepository.java
│   │   │   │
│   │   │   ├── domain/                         # entidades JPA (modelo de persistencia)
│   │   │   │   ├── Estudiante.java
│   │   │   │   ├── Profesor.java
│   │   │   │   └── Curso.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── EstudianteRequest.java
│   │   │   │   │   ├── ProfesorRequest.java
│   │   │   │   │   └── CursoRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── EstudianteResponse.java
│   │   │   │       ├── ProfesorResponse.java
│   │   │   │       ├── CursoResponse.java
│   │   │   │       └── CursoResumenResponse.java  # vista liviana sin lista de estudiantes
│   │   │   │
│   │   │   ├── mapper/
│   │   │   │   ├── EstudianteMapper.java       # interfaz MapStruct
│   │   │   │   ├── ProfesorMapper.java
│   │   │   │   └── CursoMapper.java
│   │   │   │
│   │   │   └── exception/
│   │   │       ├── ResourceNotFoundException.java
│   │   │       ├── DuplicateEmailException.java
│   │   │       ├── ProfesorInvalidoException.java
│   │   │       ├── ProfesorConCursosAsignadosException.java
│   │   │       ├── GlobalExceptionHandler.java # @RestControllerAdvice
│   │   │       ├── ApiError.java               # forma estándar de respuesta de error
│   │   │       └── FieldValidationError.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       └── db/migration/
│   │           ├── V1__create_profesor_table.sql
│   │           ├── V2__create_curso_table.sql
│   │           ├── V3__create_estudiante_table.sql
│   │           └── V4__create_curso_estudiante_table.sql
│   │
│   └── test/
│       └── java/com/colegio/gestion/
│           ├── service/
│           │   ├── EstudianteServiceTest.java   # unit, Mockito
│           │   ├── ProfesorServiceTest.java
│           │   └── CursoServiceTest.java
│           ├── controller/
│           │   ├── EstudianteControllerIT.java  # integración, MockMvc + Testcontainers
│           │   ├── ProfesorControllerIT.java
│           │   └── CursoControllerIT.java
│           └── GestionEscolarApplicationTests.java
│
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

**Notas de diseño de carpetas:**
- `domain/` en vez de `entity/` o `model/`: nombre neutral, evita ambigüedad con los DTOs.
- `service/impl/` separado de las interfaces: dejar las interfaces al mismo nivel que `impl/` hace explícito el contrato que consume `controller/`.
- `dto/request` vs `dto/response` separados: los campos de entrada (con validación) y salida (sin validación, pueden incluir datos derivados) tienen shapes distintos; nunca se expone la entidad JPA directamente en la API.
- `mapper/` como interfaces MapStruct: mantiene `service/impl` limpio de lógica de mapeo repetitiva.

---

## 6. Modelo de datos

### 6.1 Diagrama entidad-relación

```
┌───────────────┐        1        N   ┌───────────────┐
│   Profesor    │───────────────────▶│     Curso      │
├───────────────┤   profesor_id FK    ├───────────────┤
│ id            │   (obligatorio)     │ id            │
│ nombre        │                     │ nombre        │
│ apellido      │                     │ descripcion   │
│ email (UQ)    │                     │ profesor_id FK│
│ especialidad  │                     └───────┬───────┘
└───────────────┘                             │
                                        N      │      N
                                    ┌──────────▼──────────┐
                                    │  curso_estudiante    │  (tabla intermedia)
                                    │  curso_id FK          │
                                    │  estudiante_id FK     │
                                    └──────────┬──────────┘
                                               │
                                        ┌──────▼───────┐
                                        │  Estudiante   │
                                        ├───────────────┤
                                        │ id            │
                                        │ nombre        │
                                        │ apellido      │
                                        │ email (UQ)    │
                                        │ fecha_nac     │
                                        └───────────────┘
```

### 6.2 Entidades JPA (resumen de anotaciones clave)

**Profesor**
```java
@Entity
@Table(name = "profesor")
public class Profesor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String especialidad;

    @OneToMany(mappedBy = "profesor")
    private List<Curso> cursos = new ArrayList<>();
}
```

**Curso**
```java
@Entity
@Table(name = "curso")
public class Curso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesor_id", nullable = false)
    private Profesor profesor;

    @ManyToMany
    @JoinTable(
        name = "curso_estudiante",
        joinColumns = @JoinColumn(name = "curso_id"),
        inverseJoinColumns = @JoinColumn(name = "estudiante_id")
    )
    private Set<Estudiante> estudiantes = new HashSet<>();
}
```

**Estudiante**
```java
@Entity
@Table(name = "estudiante")
public class Estudiante {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @ManyToMany(mappedBy = "estudiantes")
    private Set<Curso> cursos = new HashSet<>();
}
```

**Decisión de diseño — dirección de la relación M:N:** el lado dueño (`@JoinTable`) vive en `Curso`, porque la inscripción se modela como una operación sobre el curso (`POST /cursos/{id}/estudiantes/{estudianteId}`). Para que quede reflejada en ambas direcciones en memoria (criterio 4), el service de inscripción agrega el curso también al `Set<Curso>` del estudiante antes de persistir, o simplemente se relee el estudiante tras el `flush` — se documenta en 7.3.

### 6.3 Script de migración (Flyway, ejemplo V1–V4)

- `V1__create_profesor_table.sql`: tabla `profesor` con `email` `UNIQUE NOT NULL`.
- `V2__create_curso_table.sql`: tabla `curso` con FK `profesor_id REFERENCES profesor(id)`, `NOT NULL`, sin `ON DELETE CASCADE` (para forzar el manejo explícito del criterio 3, ver 7.2).
- `V3__create_estudiante_table.sql`: tabla `estudiante` con `email UNIQUE NOT NULL`.
- `V4__create_curso_estudiante_table.sql`: tabla intermedia con PK compuesta `(curso_id, estudiante_id)` y `ON DELETE CASCADE` en ambas FKs (si se borra un curso o un estudiante, solo desaparece la inscripción, nunca la otra entidad).

**Por qué Flyway y no `ddl-auto: update`:** control de versiones del esquema, reproducibilidad entre entornos, y evita que Hibernate infiera constraints de forma implícita — se declaran explícitamente en SQL.

---

## 7. Diseño de la capa de negocio (Service)

### 7.1 Contratos (interfaces)

```java
public interface CursoService {
    CursoResponse crear(CursoRequest request);
    CursoResponse obtenerPorId(Long id);
    List<CursoResponse> listarTodos();
    CursoResponse actualizar(Long id, CursoRequest request);
    void eliminar(Long id);
    CursoResponse inscribirEstudiante(Long cursoId, Long estudianteId);
    CursoResponse retirarEstudiante(Long cursoId, Long estudianteId);
}
```

Análogamente `EstudianteService` y `ProfesorService` con las 5 operaciones CRUD estándar. `ProfesorService` añade `eliminar(Long id)` con la lógica de 7.2.

### 7.2 Regla de negocio — eliminación de profesor con cursos asignados (criterio 3)

**Decisión de diseño (a documentar explícitamente, según pide el enunciado):** se **impide la eliminación** de un profesor mientras tenga uno o más cursos asignados. No se reasigna automáticamente porque reasignar implícitamente un curso a otro profesor es una decisión pedagógica que no le corresponde tomar al sistema.

Flujo:
1. `DELETE /api/v1/profesores/{id}` → el service verifica `profesorRepository.existsById(id)`; si no existe → `ResourceNotFoundException` (404).
2. Si existe, verifica `cursoRepository.existsByProfesorId(id)`.
   - Si tiene cursos asignados → `ProfesorConCursosAsignadosException` (mapea a **409 Conflict**) con mensaje: *"No se puede eliminar el profesor porque tiene N curso(s) asignado(s). Reasigne los cursos a otro profesor antes de eliminar."*
   - Si no tiene cursos → se elimina.
3. Para reasignar: el cliente usa el endpoint existente `PUT /api/v1/cursos/{id}` para cambiar el `profesorId` de cada curso afectado, y luego reintenta el `DELETE` del profesor. No se agrega un endpoint especial de "reasignación masiva" para mantener el alcance acotado a los 7 días de entrega; se documenta como mejora futura en la sección 12.

### 7.3 Regla de negocio — inscripción estudiante↔curso (criterio 4)

`inscribirEstudiante(cursoId, estudianteId)`:
1. Busca `Curso` por id → 404 si no existe.
2. Busca `Estudiante` por id → 404 si no existe.
3. Si el estudiante ya está inscrito, la operación es **idempotente** (no lanza error, retorna el estado actual) — decisión de diseño para simplificar el cliente.
4. Agrega el estudiante a `curso.getEstudiantes()` y, dado que `Estudiante.cursos` es el lado inverso (`mappedBy`), Hibernate persiste la relación a través de la tabla `curso_estudiante` gestionada por el lado dueño (`Curso`). Como ambos `Set` están mapeados sobre la misma tabla física, tras el `save`/`flush` una relectura de `estudiante.getCursos()` refleja el curso — se garantiza en el mapper de respuesta (`EstudianteMapper`) que siempre lee desde la entidad recién persistida, no desde un DTO cacheado.
5. Retorna `CursoResponse` con la lista actualizada de estudiantes.

### 7.4 Regla de negocio — profesor inválido en curso (criterio 2)

**Importante — esto es una decisión explícita que se aparta de la convención REST "recurso relacionado inexistente → 404":** el enunciado exige **400 Bad Request** cuando no se envía un profesor válido al crear/actualizar un curso. Se implementa así:

1. Validación de forma: `@NotNull` sobre `profesorId` en `CursoRequest` → si viene nulo, falla la validación de Bean Validation automáticamente con 400 (ver sección 8).
2. Validación de existencia: si `profesorId` viene con un valor pero no corresponde a ningún profesor persistido, el service lanza `ProfesorInvalidoException`, mapeada explícitamente a **400** (no 404) en el `GlobalExceptionHandler`, precisamente porque el criterio de aceptación lo exige así. Se documenta esta excepción a la convención estándar en el propio Javadoc de la clase.

---

## 8. Validación de entrada (criterio 5)

Los `*Request` DTOs usan Jakarta Bean Validation:

```java
public record EstudianteRequest(
    @NotBlank(message = "El nombre es obligatorio") String nombre,
    @NotBlank(message = "El apellido es obligatorio") String apellido,
    @NotBlank @Email(message = "El email debe tener un formato válido") String email,
    @NotNull @Past(message = "La fecha de nacimiento debe ser anterior a hoy") LocalDate fechaNacimiento
) {}
```

```java
public record CursoRequest(
    @NotBlank(message = "El nombre del curso es obligatorio") String nombre,
    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres") String descripcion,
    @NotNull(message = "Debe especificar un profesor") Long profesorId
) {}
```

Los `Request` se usan como `record` (Java 21) por inmutabilidad y menos boilerplate; los `*Response` también pueden ser `record` salvo que MapStruct requiera setters (se evalúa en implementación; si es así, se usan clases con Lombok `@Value`).

En el controller: `@Valid @RequestBody EstudianteRequest request` — Spring dispara `MethodArgumentNotValidException` automáticamente si falla la validación, capturada centralizadamente (sección 9).

**Duplicidad de email:** no es un error de formato (pasa `@Email`), es un conflicto de estado con la base de datos. Se valida en el service antes del `save` (`existsByEmail`) y se lanza `DuplicateEmailException` → mapeada a **409 Conflict** (más semánticamente correcto que 400, ya que el payload en sí es válido). Se documenta esta distinción para que quien revise el código entienda por qué no todo error de validación es 400.

---

## 9. Manejo centralizado de excepciones (criterio 8)

Un único `@RestControllerAdvice`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) { ... } // 404

    @ExceptionHandler(ProfesorInvalidoException.class)
    ResponseEntity<ApiError> handleProfesorInvalido(...) { ... } // 400

    @ExceptionHandler(ProfesorConCursosAsignadosException.class)
    ResponseEntity<ApiError> handleProfesorConCursos(...) { ... } // 409

    @ExceptionHandler(DuplicateEmailException.class)
    ResponseEntity<ApiError> handleDuplicateEmail(...) { ... } // 409

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(...) { ... } // 400, incluye lista de FieldValidationError

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(...) { ... } // 500, sin filtrar detalles internos
}
```

Formato único de error (`ApiError`), consistente en toda la API:

```json
{
  "timestamp": "2026-07-14T10:32:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación",
  "path": "/api/v1/cursos",
  "errors": [
    { "field": "profesorId", "message": "Debe especificar un profesor" }
  ]
}
```

Tabla de mapeo excepción → HTTP:

| Excepción | HTTP | Cuándo |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | Campos inválidos en el body (`@Valid`) |
| `ProfesorInvalidoException` | 400 | `profesorId` no corresponde a ningún profesor existente (criterio 2) |
| `ResourceNotFoundException` | 404 | GET/PUT/DELETE por id inexistente (criterio 6) |
| `DuplicateEmailException` | 409 | Email ya registrado en Estudiante o Profesor |
| `ProfesorConCursosAsignadosException` | 409 | DELETE de profesor con cursos activos (criterio 3) |
| `Exception` (catch-all) | 500 | Error no controlado, log interno, mensaje genérico al cliente |

---

## 10. API REST — endpoints

Prefijo común: `/api/v1`

### Estudiante
| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| POST | `/estudiantes` | Crear estudiante | 201 | 400 |
| GET | `/estudiantes` | Listar (paginado opcional `?page&size`) | 200 | – |
| GET | `/estudiantes/{id}` | Obtener por id | 200 | 404 |
| PUT | `/estudiantes/{id}` | Actualizar | 200 | 400, 404 |
| DELETE | `/estudiantes/{id}` | Eliminar (borra sus inscripciones en cascada) | 204 | 404 |

### Profesor
| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| POST | `/profesores` | Crear profesor | 201 | 400 |
| GET | `/profesores` | Listar | 200 | – |
| GET | `/profesores/{id}` | Obtener por id | 200 | 404 |
| PUT | `/profesores/{id}` | Actualizar | 200 | 400, 404 |
| DELETE | `/profesores/{id}` | Eliminar (bloqueado si tiene cursos) | 204 | 404, 409 |

### Curso
| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| POST | `/cursos` | Crear curso (requiere `profesorId` válido) | 201 | 400 |
| GET | `/cursos` | Listar | 200 | – |
| GET | `/cursos/{id}` | Obtener por id (incluye profesor y estudiantes) | 200 | 404 |
| PUT | `/cursos/{id}` | Actualizar (permite reasignar profesor) | 200 | 400, 404 |
| DELETE | `/cursos/{id}` | Eliminar (borra inscripciones en cascada, no afecta estudiantes/profesor) | 204 | 404 |
| POST | `/cursos/{cursoId}/estudiantes/{estudianteId}` | Inscribir estudiante | 200 | 404 |
| DELETE | `/cursos/{cursoId}/estudiantes/{estudianteId}` | Retirar estudiante | 200 | 404 |

Todos los `GET` de listado devuelven DTOs "resumen" (sin cargar colecciones anidadas completas) para evitar el problema N+1 / payloads gigantes; el `GET /{id}` devuelve el detalle completo.

---

## 11. Documentación de API (Swagger / OpenAPI)

Dependencia: `springdoc-openapi-starter-webmvc-ui`.

```java
@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI gestionEscolarOpenAPI() {
        return new OpenAPI().info(new Info()
            .title("Sistema de Gestión Escolar - API")
            .description("API REST para administración de estudiantes, profesores y cursos")
            .version("1.0.0"));
    }
}
```

- UI disponible en `/swagger-ui.html`.
- Spec JSON en `/v3/api-docs`.
- Los DTOs documentan cada campo con `@Schema(description = ..., example = ...)`.
- Cada controller anota sus operaciones con `@Operation` y `@ApiResponse` por código de estado (200/201/204/400/404/409), de modo que el contrato de errores de la sección 9 quede visible en la documentación interactiva.

---

## 12. Configuración y Docker

### 12.1 `docker-compose.yml` (PostgreSQL dockerizado — criterio 7)

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: gestion-escolar-db
    environment:
      POSTGRES_DB: gestion_escolar
      POSTGRES_USER: gestion_user
      POSTGRES_PASSWORD: ${DB_PASSWORD:-changeme}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U gestion_user -d gestion_escolar"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

**Nota:** la contraseña se parametriza vía variable de entorno con default solo para desarrollo local; en un `.env` (no versionado) se sobreescribe. Se agrega `.env.example` al repo.

La app Spring Boot corre localmente (IDE) o, opcionalmente, como segundo servicio en el mismo `docker-compose.yml` (`depends_on: postgres` con `condition: service_healthy`) usando el `Dockerfile` multi-stage (build con `maven:3.9-eclipse-temurin-21` → runtime `eclipse-temurin:21-jre-alpine`). Se documenta como perfil `docker` opcional para no acoplar la entrega a tener la app dockerizada, ya que el enunciado solo exige la base de datos dockerizada.

### 12.2 `application.yml` (perfil por defecto, apunta a Postgres local/Docker)

```yaml
spring:
  application:
    name: gestion-escolar
  datasource:
    url: jdbc:postgresql://localhost:5432/gestion_escolar
    username: gestion_user
    password: ${DB_PASSWORD:changeme}
  jpa:
    hibernate:
      ddl-auto: validate   # el esquema lo gestiona Flyway, no Hibernate
    properties:
      hibernate.format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

`ddl-auto: validate` (no `update`/`create`): fuerza que cualquier discrepancia entre entidades y esquema real sea un error explícito en el arranque, en vez de una migración silenciosa de Hibernate — refuerza que Flyway es la única fuente de verdad del esquema.

---

## 13. Testing (recomendado, complementa el criterio 8)

- **Unit tests de service:** Mockito mockeando los repositorios (interfaces), verificando reglas de negocio de 7.2–7.4 de forma aislada — esto es posible precisamente porque el service depende de la interfaz `Repository`, no de una implementación concreta (DIP).
- **Tests de integración de controller:** `@SpringBootTest` + `MockMvc` + Testcontainers (`postgres:16-alpine`), validando los contratos HTTP (códigos 200/201/204/400/404/409) contra una base de datos real, no en memoria — coherente con el criterio 7 que rechaza H2/in-memory como entrega final.

---

## 14. Mejoras futuras (fuera de alcance de la entrega de 7 días)

- Autenticación/autorización (Spring Security + JWT) para el rol "administrador" mencionado en la historia de usuario.
- Endpoint de reasignación masiva de cursos al eliminar un profesor.
- Paginación y filtros (`?nombre=`, `?especialidad=`) en los listados.
- Auditoría (`created_at`/`updated_at`) vía Spring Data JPA Auditing.
- Rate limiting / caching de listados.

---

## 15. Checklist de trazabilidad a criterios de aceptación

| Criterio | Cubierto en |
|---|---|
| 1. CRUD completo 3 entidades | Sección 10 |
| 2. Profesor inválido → 400 | Sección 7.4 |
| 3. Eliminar profesor con cursos → manejo explícito | Sección 7.2 |
| 4. Inscripción bidireccional | Sección 7.3 |
| 5. Validación de datos → 400 | Sección 8 |
| 6. Recurso inexistente → 404 | Sección 9 |
| 7. PostgreSQL dockerizado | Sección 12.1 |
| 8. Arquitectura en capas + excepciones centralizadas | Secciones 4, 5, 9 |
