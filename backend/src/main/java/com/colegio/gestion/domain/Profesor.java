package com.colegio.gestion.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @SQLRestriction aplica "eliminado_en IS NULL" a TODAS las consultas generadas
 * por Hibernate para esta entidad (findById, findAll, existsBy..., colecciones
 * relacionadas), incluyendo las derivadas por Spring Data. Es lo que hace que
 * el soft delete sea transparente para el resto del codigo: un registro
 * eliminado logicamente se comporta como si no existiera.
 */
@Entity
@Table(name = "profesor")
@SQLRestriction("eliminado_en IS NULL")
public class Profesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "eliminado_en")
    private Instant eliminadoEn;

    public Profesor() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }

    public Instant getEliminadoEn() {
        return eliminadoEn;
    }

    public void setEliminadoEn(Instant eliminadoEn) {
        this.eliminadoEn = eliminadoEn;
    }
}
