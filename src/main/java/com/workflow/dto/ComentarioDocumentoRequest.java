package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Solicitud para agregar un comentario a la bitácora de un documento */
@Data
public class ComentarioDocumentoRequest {

    @NotBlank
    private String usuarioId;

    @NotBlank
    private String comentario;
}
