package com.workflow.dto;

import com.workflow.document.Componente;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormularioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotBlank(message = "El empresaId es obligatorio")
    private String empresaId;

    private List<Componente> componentes;
}
