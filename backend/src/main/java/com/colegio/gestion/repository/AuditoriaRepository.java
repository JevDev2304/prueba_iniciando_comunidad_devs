package com.colegio.gestion.repository;

import com.colegio.gestion.domain.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findByEntidadAndEntidadIdOrderByFechaDesc(String entidad, Long entidadId);

    List<Auditoria> findAllByOrderByFechaDesc();
}
