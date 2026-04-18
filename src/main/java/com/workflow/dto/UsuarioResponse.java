package com.workflow.dto;

import lombok.*;

/** Respuesta de usuario sin campo password */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {

    private String id;
    private String nombre;
    private String apellido;
    private String correo;
    private String rolId;
    private String empresaId;
    private String departamentoId;
    private boolean activo;
}
