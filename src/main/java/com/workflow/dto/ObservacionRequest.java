package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObservacionRequest {

    @NotBlank
    private String tramiteId;

    @NotBlank
    private String actividadId;

    private String componenteId;

    @NotBlank
    private String usuarioId;

    @NotBlank
    private String descripcion;

    /** Valores: observado, aprobado, rechazado */
    private String estado;
}
