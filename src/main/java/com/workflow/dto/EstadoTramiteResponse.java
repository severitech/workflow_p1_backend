package com.workflow.dto;

import lombok.*;

/** Respuesta WebSocket con el estado actual del trámite */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoTramiteResponse {

    private String id;
    private String estado;
    private String semaforo;
    private int pasoActual;
    private String actividadActualId;
    private String actividadActualNombre;
}
