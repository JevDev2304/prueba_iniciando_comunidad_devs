-- Soft delete: columna nullable en las 3 entidades principales.
-- NULL = registro activo. Con fecha = eliminado logicamente en ese momento.
ALTER TABLE profesor ADD COLUMN eliminado_en TIMESTAMPTZ;
ALTER TABLE curso ADD COLUMN eliminado_en TIMESTAMPTZ;
ALTER TABLE estudiante ADD COLUMN eliminado_en TIMESTAMPTZ;

-- El UNIQUE de email debe aplicar solo a registros activos: si se elimina
-- logicamente un profesor/estudiante, su email queda libre para reutilizarse.
ALTER TABLE profesor DROP CONSTRAINT profesor_email_key;
CREATE UNIQUE INDEX ux_profesor_email_activo ON profesor (email) WHERE eliminado_en IS NULL;

ALTER TABLE estudiante DROP CONSTRAINT estudiante_email_key;
CREATE UNIQUE INDEX ux_estudiante_email_activo ON estudiante (email) WHERE eliminado_en IS NULL;

-- Tabla de auditoria: historial generico de creaciones, actualizaciones y
-- eliminaciones de las 3 entidades. "detalle" guarda un snapshot en JSON
-- del estado del recurso en el momento de la accion.
CREATE TABLE auditoria (
    id BIGSERIAL PRIMARY KEY,
    entidad VARCHAR(50) NOT NULL,
    entidad_id BIGINT NOT NULL,
    accion VARCHAR(20) NOT NULL,
    detalle TEXT,
    fecha TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_auditoria_entidad ON auditoria (entidad, entidad_id);
