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

    private String nombre;
    private String formularioId;
    private int orden;
    private boolean esObligatorio;
    private String departamentoId;
    /** Tipo de nodo UML 2.5: inicio | fin | tarea | decision | fork | join */
    private String tipoNodo;
}
