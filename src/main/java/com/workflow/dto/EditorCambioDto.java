package com.workflow.dto;

import com.workflow.document.Componente;
import lombok.*;

/** Mensaje de cambio en el editor de flujos o de formularios para sincronización colaborativa */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditorCambioDto {

    /** conectar | desconectar | eliminar-flujo | actualizar | mover-componente | crear-componente | editar-componente | eliminar-componente */
    private String tipo;

    private String flujoId;

    /** Nuevo padre al conectar, null al desconectar */
    private String flujoPadreId;

    private String politicaId;
    private String usuarioId;
    /** UUID de instancia del cliente para filtrar mensajes propios */
    private String clienteId;

    /** ID del formulario, para cambios en el editor de componentes de formulario */
    private String formularioId;

    /** ID del componente afectado (mover, editar, eliminar) */
    private String componenteId;

    /** Snapshot completo del componente, para crear/mover/editar */
    private Componente componente;
}
