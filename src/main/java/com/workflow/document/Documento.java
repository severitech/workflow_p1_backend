package com.workflow.document;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Entidad que representa un documento subido al sistema */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "documentos")
public class Documento {

    @Id
    private String id;

    /** Nombre original del archivo */
    private String nombre;

    /** Extensión/tipo: "pdf", "xlsx", "docx", "png", etc. */
    private String tipo;

    /** Tamaño en bytes */
    private long tamanio;

    private String empresaId;

    /** Puede ser null — si es null pertenece al repositorio general */
    private String tramiteId;

    /** Puede ser null */
    private String actividadId;

    /** Clave de almacenamiento: ruta relativa (local) o key en S3 */
    private String claveAlmacenamiento;

    /** Número de versión actual: "1.0", "1.1", "2.0" */
    private String version;

    /** Claves de versiones anteriores para historial */
    @Builder.Default
    private List<VersionHistorial> versionesAnteriores = new ArrayList<>();

    /** ID del usuario que subió el documento */
    private String creadoPor;

    private LocalDateTime fechaCreacion;

    /** false = eliminado lógicamente, sigue en S3/disco */
    @Builder.Default
    private boolean activo = true;

    /** Descripción opcional del documento */
    private String descripcion;

    /** Registro de una versión anterior */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VersionHistorial {
        private String version;
        private String claveAlmacenamiento;
        private LocalDateTime fecha;
        private String subidoPor;
    }
}
