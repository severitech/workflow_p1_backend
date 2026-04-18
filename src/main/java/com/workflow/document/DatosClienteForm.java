package com.workflow.document;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Embebida en Actividad — datos llenados por el cliente en un formulario */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosClienteForm {

    private String formularioId;

    /** Valores: en_proceso, completado, observado */
    private String estado;

    @Builder.Default
    private List<DatoForm> datos = new ArrayList<>();

    @Builder.Default
    private List<Observacion> observaciones = new ArrayList<>();

    private LocalDateTime fecha;
}
