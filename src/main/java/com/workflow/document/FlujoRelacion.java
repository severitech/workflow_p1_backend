package com.workflow.document;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "flujo_relaciones")
public class FlujoRelacion {

    @Id
    private String id;

    /** Política a la que pertenece esta relación */
    @Indexed
    private String politicaId;

    /** Flujo de origen (nodo padre) */
    @Indexed
    private String padreId;

    /** Flujo de destino (nodo hijo) */
    @Indexed
    private String hijoId;

    /** Campo del formulario del padre a evaluar para activar esta relación (null = sin condición) */
    private String condicionCampo;

    /** Valor esperado del campo para activar esta relación */
    private String condicionValor;

    /** Tipo de relación: "condicional" para ramas, "siguiente" para convergencia */
    private String tipo;

    @CreatedDate
    private LocalDateTime fechaCreacion;
}
