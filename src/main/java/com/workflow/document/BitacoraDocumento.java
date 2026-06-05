package com.workflow.document;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/** Registro de auditoría de acciones realizadas sobre un documento */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bitacora_documentos")
public class BitacoraDocumento {

    @Id
    private String id;

    private String documentoId;
    private String usuarioId;

    /**
     * Tipo de acción:
     * "subio", "descargo", "comento", "nueva_version",
     * "cambio_permiso", "elimino", "visualizo"
     */
    private String accion;

    /** Detalle legible de la acción */
    private String detalle;

    private LocalDateTime fecha;
}
