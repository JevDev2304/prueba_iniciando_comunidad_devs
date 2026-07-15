package com.colegio.gestion.repository;

import com.colegio.gestion.domain.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    boolean existsByProfesorId(Long profesorId);
}
