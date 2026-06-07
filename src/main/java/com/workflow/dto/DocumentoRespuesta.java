package com.workflow.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DocumentoRespuesta {

    private String id;
    private String nombre;
    private String tipo;
    private long tamanio;
    private String empresaId;
    private String tramiteId;
    private String actividadId;
    private String version;
    private String creadoPor;
    private LocalDateTime fechaCreacion;
    private boolean activo;
    private String descripcion;
    private String politicaId;
    private String contenido;
    private boolean esTexto;
    private String urlDescarga;
    private String miPermiso;
    private List<VersionInfo> versionesAnteriores;

    @Getter
    @Builder
    public static class VersionInfo {
        private String version;
        private LocalDateTime fecha;
        private String subidoPor;
    }
}
