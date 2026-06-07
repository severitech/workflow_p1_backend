package com.workflow.dto;

import lombok.Data;

/** Mensaje WebSocket enviado cuando un usuario edita el contenido de un documento de texto */
@Data
public class EditarDocumentoMsg {
    private String documentoId;
    private String usuarioId;
    private String nombreUsuario;
    private String contenido;
}
