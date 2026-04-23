package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlujoRequest {

    @NotBlank(message = "El politicaId es obligatorio")
    private String politicaId;

    private String formularioId;
    private int orden;
    private boolean esObligatorio;
    private String departamentoId;
    private Double posicionX;
    private Double posicionY;
}
