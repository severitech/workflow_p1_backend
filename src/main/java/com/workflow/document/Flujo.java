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
@Document(collection = "flujos")
public class Flujo {

    @Id
    private String id;

    @Indexed
    private String politicaId;

    private String formularioId;
    private int orden;
    private boolean esObligatorio;

    /** Departamento responsable de esta actividad */
    private String departamentoId;

    /** Posición X del nodo en el canvas del editor */
    private Double posicionX;

    /** Posición Y del nodo en el canvas del editor */
    private Double posicionY;

    @CreatedDate
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    private LocalDateTime fechaModificacion;
}
