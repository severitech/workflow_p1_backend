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

    private String nombre;
    private String formularioId;
    private int orden;
    private boolean esObligatorio;
    /** Departamento responsable — define la calle (swimlane) en el diagrama UML */
    private String departamentoId;

    /** Tipo de nodo UML 2.5: inicio | fin | tarea | decision | fork | join */
    private String tipoNodo;

    @CreatedDate
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    private LocalDateTime fechaModificacion;
}
