package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlujoRelacionRequest {

    @NotBlank(message = "El politicaId es obligatorio")
    private String politicaId;

    @NotBlank(message = "El padreId es obligatorio")
    private String padreId;

    @NotBlank(message = "El hijoId es obligatorio")
    private String hijoId;

    /** Campo del formulario del padre a evaluar (null = sin condición) */
    private String condicionCampo;

    /** Valor esperado del campo para activar la relación */
    private String condicionValor;

    /** "condicional" para ramas, "siguiente" para convergencia */
    private String tipo;
}
