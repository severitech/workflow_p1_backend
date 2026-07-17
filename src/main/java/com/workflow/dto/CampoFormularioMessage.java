package com.workflow.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

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

    /** Filas de datos, solo si el componente es de tipo tabla */
    private List<Map<String, String>> filas;
}
