package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/** Datos para cerrar sesión — requiere el token activo del usuario */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CerrarSesionRequest {

    @NotBlank(message = "El token es obligatorio")
    private String token;
}
