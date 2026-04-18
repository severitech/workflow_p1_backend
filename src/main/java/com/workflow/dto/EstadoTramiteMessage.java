package com.workflow.dto;

import lombok.*;

/** Mensaje WebSocket para consultar estado de un trámite */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoTramiteMessage {

    private String tramiteId;
}
