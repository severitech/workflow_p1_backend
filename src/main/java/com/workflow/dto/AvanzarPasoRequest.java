package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvanzarPasoRequest {

    @NotBlank(message = "El usuarioId es obligatorio")
    private String usuarioId;

    private String observacion;

    /** Lista de pares {componenteId, etiqueta, valor} del formulario */
    private List<Map<String, String>> datosForm;
}
