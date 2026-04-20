package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IniciarTramiteRequest {

    @NotBlank(message = "El politicaId es obligatorio")
    private String politicaId;

    @NotBlank(message = "El clienteId es obligatorio")
    private String clienteId;

    @NotBlank(message = "El recepcionistaId es obligatorio")
    private String recepcionistaId;

    @NotBlank(message = "El empresaId es obligatorio")
    private String empresaId;

    /** Valores: normal, urgente */
    private String prioridad;
}
