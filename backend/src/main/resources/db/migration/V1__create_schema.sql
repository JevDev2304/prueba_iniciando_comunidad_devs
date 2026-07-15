CREATE TABLE profesor (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    especialidad VARCHAR(100) NOT NULL
);

CREATE TABLE curso (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(1000),
    profesor_id BIGINT NOT NULL,
    CONSTRAINT fk_curso_profesor FOREIGN KEY (profesor_id) REFERENCES profesor (id)
);

CREATE INDEX idx_curso_profesor_id ON curso (profesor_id);

CREATE TABLE estudiante (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    fecha_nacimiento DATE NOT NULL
);

CREATE TABLE curso_estudiante (
    curso_id BIGINT NOT NULL,
    estudiante_id BIGINT NOT NULL,
    PRIMARY KEY (curso_id, estudiante_id),
    CONSTRAINT fk_ce_curso FOREIGN KEY (curso_id) REFERENCES curso (id) ON DELETE CASCADE,
    CONSTRAINT fk_ce_estudiante FOREIGN KEY (estudiante_id) REFERENCES estudiante (id) ON DELETE CASCADE
);
