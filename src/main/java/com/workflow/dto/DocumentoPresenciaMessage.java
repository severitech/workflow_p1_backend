package com.workflow.dto;

import lombok.Data;

/** Mensaje WebSocket para colaboración en tiempo real en un documento */
@Data
public class DocumentoPresenciaMessage {

    private String documentoId;
    private String usuarioId;
    private String nombreUsuario;
    private String color;

    /** "unirse", "salir", "moviendo" */
    private String accion;

    /** Posición X del cursor (porcentaje relativo al contenedor) */
    private double posX;

    /** Posición Y del cursor (porcentaje relativo al contenedor) */
    private double posY;
}
