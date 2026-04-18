package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String nit;
    private String direccion;
    private String telefono;
    private String correo;
}
