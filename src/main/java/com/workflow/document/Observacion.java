package com.workflow.document;

import lombok.*;

import java.time.LocalDateTime;

/** Embebida en DatosClienteForm */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Observacion {

    private String usuarioId;
    private String descripcion;

    /** Valores: observado, aprobado, rechazado */
    private String estado;

    private LocalDateTime fecha;
}
