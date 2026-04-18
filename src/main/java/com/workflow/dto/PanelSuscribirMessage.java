package com.workflow.dto;

import lombok.*;

/** Mensaje WebSocket para suscribirse al panel de recepcionista */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PanelSuscribirMessage {

    private String usuarioId;
    private String empresaId;
}
