-- Datos de ejemplo para poder probar la API de inmediato sin tener que crear
-- todo a mano. Se usan ids explicitos (predecibles: profesores y cursos 1-4,
-- estudiantes 1-10) y se reinicia la secuencia despues, para que los proximos
-- INSERT hechos por la aplicacion sigan contando desde ahi sin chocar.

INSERT INTO profesor (id, nombre, apellido, email, especialidad) VALUES
    (1, 'Ana', 'Gomez', 'ana.gomez@colegio.edu', 'Matematicas'),
    (2, 'Carlos', 'Rodriguez', 'carlos.rodriguez@colegio.edu', 'Historia'),
    (3, 'Lucia', 'Fernandez', 'lucia.fernandez@colegio.edu', 'Biologia'),
    (4, 'Miguel', 'Torres', 'miguel.torres@colegio.edu', 'Educacion Fisica');

INSERT INTO curso (id, nombre, descripcion, profesor_id) VALUES
    (1, 'Algebra I', 'Introduccion a ecuaciones y funciones basicas', 1),
    (2, 'Historia Universal', 'Panorama de los principales procesos historicos', 2),
    (3, 'Biologia Celular', 'Estructura y funcion de la celula', 3),
    (4, 'Educacion Fisica', 'Acondicionamiento fisico y deportes de equipo', 4);

INSERT INTO estudiante (id, nombre, apellido, email, fecha_nacimiento) VALUES
    (1, 'Juan', 'Perez', 'juan.perez@colegio.edu', '2012-03-14'),
    (2, 'Maria', 'Lopez', 'maria.lopez@colegio.edu', '2011-07-22'),
    (3, 'Pedro', 'Sanchez', 'pedro.sanchez@colegio.edu', '2012-11-05'),
    (4, 'Sofia', 'Ramirez', 'sofia.ramirez@colegio.edu', '2011-02-18'),
    (5, 'Diego', 'Martinez', 'diego.martinez@colegio.edu', '2010-09-30'),
    (6, 'Valentina', 'Castro', 'valentina.castro@colegio.edu', '2012-05-09'),
    (7, 'Andres', 'Morales', 'andres.morales@colegio.edu', '2011-12-01'),
    (8, 'Camila', 'Ortiz', 'camila.ortiz@colegio.edu', '2010-06-27'),
    (9, 'Santiago', 'Vargas', 'santiago.vargas@colegio.edu', '2012-08-16'),
    (10, 'Isabella', 'Rojas', 'isabella.rojas@colegio.edu', '2011-04-03');

-- Algunas inscripciones de ejemplo, para que los cursos no se vean vacios al
-- probar GET /api/v1/cursos/{id}. El estudiante 3 queda en dos cursos para
-- mostrar la relacion muchos a muchos.
INSERT INTO curso_estudiante (curso_id, estudiante_id) VALUES
    (1, 1), (1, 2), (1, 3),
    (2, 3), (2, 4), (2, 5), (2, 6),
    (3, 5), (3, 6), (3, 7), (3, 8),
    (4, 1), (4, 7), (4, 9), (4, 10);

SELECT setval('profesor_id_seq', (SELECT MAX(id) FROM profesor));
SELECT setval('curso_id_seq', (SELECT MAX(id) FROM curso));
SELECT setval('estudiante_id_seq', (SELECT MAX(id) FROM estudiante));
