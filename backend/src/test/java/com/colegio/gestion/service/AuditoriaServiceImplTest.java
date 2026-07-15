package com.colegio.gestion.service;

import com.colegio.gestion.domain.AccionAuditoria;
import com.colegio.gestion.domain.Auditoria;
import com.colegio.gestion.dto.response.AuditoriaResponse;
import com.colegio.gestion.dto.response.ProfesorResumenResponse;
import com.colegio.gestion.repository.AuditoriaRepository;
import com.colegio.gestion.service.impl.AuditoriaServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceImplTest {

    @Mock
    private AuditoriaRepository auditoriaRepository;

    private AuditoriaServiceImpl auditoriaService;

    @BeforeEach
    void setUp() {
        auditoriaService = new AuditoriaServiceImpl(auditoriaRepository, new ObjectMapper());
    }

    @Test
    void registrar_serializaElSnapshotComoJsonYGuarda() {
        ProfesorResumenResponse snapshot = new ProfesorResumenResponse(1L, "Ana", "Gomez", "ana@x.com", "Matematicas");

        auditoriaService.registrar("PROFESOR", 1L, AccionAuditoria.CREAR, snapshot);

        ArgumentCaptor<Auditoria> captor = ArgumentCaptor.forClass(Auditoria.class);
        verify(auditoriaRepository).save(captor.capture());

        Auditoria guardada = captor.getValue();
        assertThat(guardada.getEntidad()).isEqualTo("PROFESOR");
        assertThat(guardada.getEntidadId()).isEqualTo(1L);
        assertThat(guardada.getAccion()).isEqualTo(AccionAuditoria.CREAR);
        assertThat(guardada.getDetalle()).contains("\"nombre\":\"Ana\"");
    }

    @Test
    void registrar_conFalloDeSerializacion_guardaElToStringComoRespaldo() throws Exception {
        ObjectMapper objectMapperQueFalla = org.mockito.Mockito.mock(ObjectMapper.class);
        when(objectMapperQueFalla.writeValueAsString(any()))
                .thenThrow(org.mockito.Mockito.mock(JsonProcessingException.class));
        AuditoriaServiceImpl serviceConMapperRoto = new AuditoriaServiceImpl(auditoriaRepository, objectMapperQueFalla);

        serviceConMapperRoto.registrar("PROFESOR", 1L, AccionAuditoria.CREAR, new Object());

        ArgumentCaptor<Auditoria> captor = ArgumentCaptor.forClass(Auditoria.class);
        verify(auditoriaRepository).save(captor.capture());
        assertThat(captor.getValue().getDetalle()).isNotNull();
    }

    @Test
    void listarTodo_mapeaLasEntidadesAResponse() {
        Auditoria auditoria = new Auditoria();
        auditoria.setId(1L);
        auditoria.setEntidad("PROFESOR");
        auditoria.setEntidadId(1L);
        auditoria.setAccion(AccionAuditoria.CREAR);
        auditoria.setDetalle("{}");
        auditoria.setFecha(Instant.now());

        when(auditoriaRepository.findAllByOrderByFechaDesc()).thenReturn(List.of(auditoria));

        List<AuditoriaResponse> resultado = auditoriaService.listarTodo();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).entidad()).isEqualTo("PROFESOR");
    }

    @Test
    void listarPorEntidad_delegaEnElRepositorioConLosFiltros() {
        when(auditoriaRepository.findByEntidadAndEntidadIdOrderByFechaDesc("CURSO", 3L)).thenReturn(List.of());

        List<AuditoriaResponse> resultado = auditoriaService.listarPorEntidad("CURSO", 3L);

        assertThat(resultado).isEmpty();
        verify(auditoriaRepository).findByEntidadAndEntidadIdOrderByFechaDesc("CURSO", 3L);
    }
}
