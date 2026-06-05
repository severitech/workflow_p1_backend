package com.workflow.dto;

import lombok.*;

/** Mensaje de cambio en el editor de flujos para sincronización colaborativa */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditorCambioDto {

    /** conectar | desconectar | eliminar-flujo | actualizar */
    private String tipo;

    private String flujoId;

    /** Nuevo padre al conectar, null al desconectar */
    private String flujoPadreId;

    private String politicaId;
    private String usuarioId;
    /** UUID de instancia del cliente para filtrar mensajes propios */
    private String clienteId;
}
