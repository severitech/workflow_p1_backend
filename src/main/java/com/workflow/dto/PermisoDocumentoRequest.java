package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Solicitud para asignar o actualizar el permiso de un usuario sobre un documento */
@Data
public class PermisoDocumentoRequest {

    @NotBlank
    private String usuarioId;

    /** "editor", "comentador" o "visualizador" */
    @NotBlank
    private String nivel;
}
