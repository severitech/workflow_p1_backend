package com.workflow.document;

import lombok.*;

/** Embebido en DatosClienteForm — valor de un campo del formulario */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatoForm {

    private String componenteId;
    private String etiqueta;
    private String valor;
}
