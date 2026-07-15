# Sistema de Gestión Escolar — Backend

Documento de referencia general del proyecto: qué hace, cómo está organizado y qué reglas sigue. Pensado para poder entenderse sin ser el desarrollador que lo escribió, sin bajar al detalle de cada línea de código.

## 1. ¿Qué es?

Una API para que un colegio administre su información académica básica: **estudiantes**, **profesores** y **cursos**, y la relación entre los tres. Reemplaza llevar esa información dispersa (planillas, listas sueltas) por un solo sistema centralizado y consultable.

## 2. Stack tecnológico

| Componente | Elección |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot |
| Base de datos | PostgreSQL, corriendo en un contenedor Docker |
| Control de versiones del esquema de la base de datos | Flyway (los cambios a la base de datos quedan versionados como scripts, no se modifican a mano) |
| Documentación de la API | Swagger — interfaz visual para probar cada funcionalidad desde el navegador |
| Empaquetado / build | Gradle |

## 3. Cómo está organizado el código

El proyecto sigue una arquitectura en tres capas: cada una tiene una responsabilidad única, y solo se comunica con la capa de abajo a través de un contrato definido (una interfaz), nunca de su implementación concreta. Esto permite cambiar el "cómo" de una capa sin afectar a las demás.

```
┌─────────────────────────────────────────────────────────┐
│  Controller (capa de presentación)                      │
│  - Recibe la petición, valida los datos de entrada       │
│  - Traduce entre lo que llega/sale y lo que usa el resto │
│  - Depende de interfaces de Service (no de su impl.)     │
└───────────────────────┬───────────────────────────────────┘
                         │ depende de (interfaz)
┌───────────────────────▼───────────────────────────────────┐
│  Service (capa de negocio)                               │
│  - Interfaz: el contrato (p.ej. EstudianteService)        │
│  - Impl: las reglas de negocio reales (sección 5)         │
│  - Depende de interfaces de Repository                    │
└───────────────────────┬───────────────────────────────────┘
                         │ depende de (interfaz)
┌───────────────────────▼───────────────────────────────────┐
│  Repository (capa de persistencia)                        │
│  - Habla directamente con la base de datos                │
└─────────────────────────────────────────────────────────┘
```

Cada una de las cuatro cosas que maneja el sistema (Estudiante, Profesor, Curso, y el historial de Auditoría) tiene su propio controller, service y repository — así, agregar una funcionalidad a "Cursos" nunca implica tocar el código de "Estudiantes".

```
gestion-escolar-backend/
├── src/
│   ├── main/
│   │   ├── java/com/colegio/gestion/
│   │   │   ├── GestionEscolarApplication.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   └── OpenApiConfig.java              # configuración de Swagger
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── EstudianteController.java
│   │   │   │   ├── ProfesorController.java
│   │   │   │   ├── CursoController.java
│   │   │   │   └── AuditoriaController.java        # historial, solo lectura
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── EstudianteService.java          # interfaz (el contrato)
│   │   │   │   ├── ProfesorService.java
│   │   │   │   ├── CursoService.java
│   │   │   │   ├── AuditoriaService.java
│   │   │   │   └── impl/                           # la implementación real
│   │   │   │       ├── EstudianteServiceImpl.java
│   │   │   │       ├── ProfesorServiceImpl.java
│   │   │   │       ├── CursoServiceImpl.java
│   │   │   │       └── AuditoriaServiceImpl.java
│   │   │   │
│   │   │   ├── repository/                         # acceso a la base de datos
│   │   │   │   ├── EstudianteRepository.java
│   │   │   │   ├── ProfesorRepository.java
│   │   │   │   ├── CursoRepository.java
│   │   │   │   └── AuditoriaRepository.java
│   │   │   │
│   │   │   ├── domain/                             # representa las tablas
│   │   │   │   ├── Estudiante.java
│   │   │   │   ├── Profesor.java
│   │   │   │   ├── Curso.java
│   │   │   │   ├── Auditoria.java
│   │   │   │   └── AccionAuditoria.java            # CREAR / ACTUALIZAR / ELIMINAR
│   │   │   │
│   │   │   ├── dto/                                # lo que entra y sale por la API
│   │   │   │   ├── request/
│   │   │   │   │   ├── EstudianteRequest.java
│   │   │   │   │   ├── ProfesorRequest.java
│   │   │   │   │   └── CursoRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── EstudianteResponse.java
│   │   │   │       ├── EstudianteResumenResponse.java  # vista liviana para listados
│   │   │   │       ├── ProfesorResponse.java
│   │   │   │       ├── ProfesorResumenResponse.java
│   │   │   │       ├── CursoResponse.java
│   │   │   │       ├── CursoResumenResponse.java
│   │   │   │       └── AuditoriaResponse.java
│   │   │   │
│   │   │   ├── mapper/                             # convierte tabla ↔ API
│   │   │   │   ├── EstudianteMapper.java
│   │   │   │   ├── ProfesorMapper.java
│   │   │   │   ├── CursoMapper.java
│   │   │   │   └── AuditoriaMapper.java
│   │   │   │
│   │   │   └── exception/                          # manejo de errores, centralizado
│   │   │       ├── ResourceNotFoundException.java      # → 404
│   │   │       ├── DuplicateEmailException.java        # → 409
│   │   │       ├── ProfesorInvalidoException.java      # → 400
│   │   │       ├── ProfesorConCursosAsignadosException.java  # → 409
│   │   │       ├── GlobalExceptionHandler.java         # captura todo en un solo lugar
│   │   │       ├── ApiError.java                       # forma estándar de un error
│   │   │       └── FieldValidationError.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       └── db/migration/
│   │           ├── V1__create_schema.sql                      # esquema inicial
│   │           └── V2__agregar_soft_delete_y_auditoria.sql    # eliminación lógica + historial
│   │
│   └── test/
│       └── java/com/colegio/gestion/
│           └── ... (pendiente: pruebas unitarias e de integración)
│
├── docker-compose.yml
├── Dockerfile
├── build.gradle.kts
└── README.md
```

## 4. Modelo de datos

```
Profesor                    Curso                      Estudiante
─────────                   ─────                      ──────────
id                           id                          id
nombre                       nombre                      nombre
apellido                     descripción                 apellido
email (único)                profesor ──obligatorio──▶   email (único)
especialidad                 estudiantes ◀──inscritos──▶ cursos
```

- Un **profesor** puede tener varios **cursos** a cargo.
- Un **curso** tiene siempre **un solo** profesor responsable (obligatorio).
- Un **curso** puede tener varios **estudiantes** inscritos, y un **estudiante** puede estar en varios **cursos** — relación de muchos a muchos.
- Existe además una tabla de **auditoría**, que no forma parte del modelo académico en sí, sino que registra el historial de cambios de las otras tres (ver sección 7).

## 5. Reglas de negocio

Condiciones que el sistema hace cumplir siempre, sin excepción:

1. **Un curso siempre necesita un profesor válido y existente.** Crear o actualizar un curso sin un profesor real se rechaza con un error explicativo.
2. **No se puede eliminar un profesor que todavía tiene cursos a cargo.** Hay que reasignar esos cursos a otro profesor primero — evita que un curso quede sin responsable.
3. **Los datos inválidos nunca se guardan.** Campos vacíos, correos mal formados, fechas de nacimiento inconsistentes, etc. se rechazan antes de tocar la base de datos, indicando exactamente qué campo falló.
4. **Los correos son únicos** entre profesores y entre estudiantes.
5. **Inscribir o retirar a un estudiante de un curso actualiza la relación en ambos sentidos** — nunca queda un lado desactualizado respecto al otro.
6. **Consultar algo que no existe da una respuesta clara y predecible**, en vez de un error genérico.

## 6. Casos de uso

Para cada una de las tres entidades (estudiantes, profesores, cursos) se puede: listar todos, ver el detalle de uno, crear, actualizar y eliminar. Sobre los cursos, además: inscribir y retirar estudiantes.

| Acción | Ruta |
|---|---|
| Listar / crear | `GET` / `POST` en `/api/v1/estudiantes`, `/api/v1/profesores`, `/api/v1/cursos` |
| Ver, actualizar o eliminar uno | `GET` / `PUT` / `DELETE` en `.../{id}` |
| Inscribir o retirar un estudiante de un curso | `POST` / `DELETE` en `/api/v1/cursos/{cursoId}/estudiantes/{estudianteId}` |
| Consultar el historial de cambios | `GET` en `/api/v1/auditoria`, con filtros opcionales |

Todos estos endpoints se pueden probar directamente desde Swagger, sin necesidad de escribir código.

## 7. Eliminación lógica y auditoría

**Eliminar no borra la información.** Cuando se elimina un estudiante, profesor o curso, el registro se marca como inactivo en vez de destruirse: deja de aparecer en cualquier consulta normal (para quien lo usa, es como si ya no existiera), pero la información sigue guardada. Esto evita pérdidas por error y permite, por ejemplo, reutilizar el correo de alguien que fue eliminado.

**Todo cambio queda registrado.** Cada vez que se crea, actualiza o elimina un estudiante, profesor o curso, queda una entrada en un historial de auditoría: qué acción fue, sobre qué registro, en qué momento, y cómo quedaron los datos en ese instante. Ese historial es de solo consulta.

## 8. Cómo levantarlo

Los pasos concretos para instalar y correr el proyecto (requisitos, comandos, dónde probarlo) están en [`backend/README.md`](backend/README.md).
