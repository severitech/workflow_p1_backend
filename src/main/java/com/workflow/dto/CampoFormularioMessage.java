package com.workflow.dto;

import lombok.*;

/** Mensaje WebSocket para autoguardado de campo de formulario */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampoFormularioMessage {

    private String tramiteId;
    private String actividadId;
    private String componenteId;
    private String etiqueta;
    private String valor;
    private String usuarioId;
}
