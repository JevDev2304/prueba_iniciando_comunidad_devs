package com.colegio.gestion.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Registro inmutable de auditoria: nunca se actualiza ni se borra despues de
 * creado, solo se inserta uno nuevo por cada CREAR/ACTUALIZAR/ELIMINAR sobre
 * Profesor, Curso o Estudiante. "detalle" guarda un snapshot en JSON del
 * DTO de respuesta en el momento de la accion.
 */
@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String entidad;

    @Column(name = "entidad_id", nullable = false)
    private Long entidadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccionAuditoria accion;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @CreationTimestamp
    @Column(nullable = false)
    private Instant fecha;

    public Auditoria() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(Long entidadId) {
        this.entidadId = entidadId;
    }

    public AccionAuditoria getAccion() {
        return accion;
    }

    public void setAccion(AccionAuditoria accion) {
        this.accion = accion;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }
}
