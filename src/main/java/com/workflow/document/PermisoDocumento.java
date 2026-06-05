package com.workflow.document;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Permiso de acceso de un usuario a un documento específico */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "permisos_documentos")
public class PermisoDocumento {

    @Id
    private String id;

    private String documentoId;
    private String usuarioId;

    /**
     * Nivel de acceso:
     * "editor"      — puede subir nuevas versiones y comentar
     * "comentador"  — puede comentar pero no subir versiones
     * "visualizador"— solo puede ver y descargar
     */
    private String nivel;

    /** Quién otorgó el permiso */
    private String otorgadoPor;
}
