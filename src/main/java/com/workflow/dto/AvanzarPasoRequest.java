package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvanzarPasoRequest {

    @NotBlank(message = "El usuarioId es obligatorio")
    private String usuarioId;

    /** ID de la actividad a completar (obligatorio para flujos paralelos) */
    private String actividadId;

    private String observacion;

    /** Lista de valores {componenteId, etiqueta, valor u opcionalmente filas} del formulario */
    private List<DatoFormularioRequest> datosForm;
}
