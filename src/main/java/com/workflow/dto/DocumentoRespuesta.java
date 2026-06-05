package com.workflow.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** DTO de respuesta para operaciones sobre documentos */
@Data
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

    /** URL temporal para descargar/visualizar el documento */
    private String urlDescarga;

    /** Permiso del usuario solicitante sobre este documento */
    private String miPermiso;

    /** Historial de versiones anteriores */
    private List<VersionInfo> versionesAnteriores;

    @Data
    @Builder
    public static class VersionInfo {
        private String version;
        private LocalDateTime fecha;
        private String subidoPor;
    }
}
