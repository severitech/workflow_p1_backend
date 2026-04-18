package com.workflow.dto;

import lombok.*;

/** Respuesta al iniciar sesión — incluye token de sesión y datos del usuario */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String id;
    private String nombre;
    private String apellido;
    private String correo;
    private String rolId;
    private String nombreRol;
    private String empresaId;
    private String departamentoId;
}
