package com.workflow.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/** Valor enviado por el cliente al avanzar un paso, para un componente del formulario */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatoFormularioRequest {

    private String componenteId;
    private String etiqueta;

    /** Valor simple, para componentes no tabulares */
    private String valor;

    /** Filas de datos para componentes de tipo tabla: cada fila es un mapa columna→valor */
    private List<Map<String, String>> filas;
}
