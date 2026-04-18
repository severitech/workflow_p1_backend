package com.workflow.document;

import lombok.*;

/** Embebido dentro de Formulario */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Componente {

    /** UUID generado al crear el componente */
    private String id;

    private String tipoId;
    private String etiqueta;
    private String placeholder;
    private int orden;
    private int posicionX;
    private int posicionY;
    private boolean requerido;

    /** Opciones para componentes de tipo select */
    private java.util.List<String> opciones;
}
