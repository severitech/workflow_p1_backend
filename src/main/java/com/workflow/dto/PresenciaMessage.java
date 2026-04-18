package com.workflow.dto;

import lombok.*;

/** Mensaje WebSocket para indicar presencia en un formulario */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenciaMessage {

    private String tramiteId;
    private String actividadId;
    private String usuarioId;

    /** Valores: unirse, salir, escribiendo */
    private String accion;
}
