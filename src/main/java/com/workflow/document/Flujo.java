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

    private String nombre;

    @Indexed
    private String politicaId;

    private String formularioId;
    private int orden;
    private boolean esObligatorio;

    /** Null si es raíz; apunta al flujo padre para flujos condicionales */
    private String flujoPadreId;

    /** Campo del formulario padre a evaluar */
    private String condicionCampo;

    /** Valor esperado del campo para activar este flujo */
    private String condicionValor;

    /** Departamento responsable de esta actividad */
    private String departamentoId;

    /** Tiempo límite en horas para completar la actividad */
    private int tiempoLimiteHoras;

    @CreatedDate
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    private LocalDateTime fechaModificacion;
}
