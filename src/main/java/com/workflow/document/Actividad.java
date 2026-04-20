package com.workflow.document;

import lombok.*;

import java.time.LocalDateTime;

/** Embebida en Tramite — representa un paso del flujo de trabajo */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Actividad {

    /** UUID generado al crear la actividad */
    private String id;

    private String flujoId;
    private String usuarioId;
    private String departamentoId;
    private String nombre;

    /** Valores: espera, activo, completado, rechazado */
    private String estado;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String observacion;

    private DatosClienteForm datosForm;
}
