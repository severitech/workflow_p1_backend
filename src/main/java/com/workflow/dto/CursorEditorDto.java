package com.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Mensaje de posición de cursor para colaboración en tiempo real en el editor de flujos */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorEditorDto {
    private String usuarioId;
    private String nombre;
    private String color;
    /** moviendo | editando-campo | saliendo */
    private String accion;
    /** ID del flujo sobre el que está el cursor */
    private String nodoId;
    /** ID del componente que está siendo editado en el formulario */
    private String campoId;
    /** diagrama | formulario */
    private String contexto;
    private String politicaId;
    private String formularioId;
    /** UUID de instancia del cliente para filtrar mensajes propios */
    private String clienteId;
}
