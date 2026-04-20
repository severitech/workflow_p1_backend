package com.workflow.document;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tramites")
@CompoundIndexes({
    @CompoundIndex(name = "empresa_estado", def = "{'empresaId': 1, 'estado': 1}"),
    @CompoundIndex(name = "recepcionista_prioridad", def = "{'recepcionistaId': 1, 'prioridad': 1}")
})
public class Tramite {

    @Id
    private String id;

    private String politicaId;

    @Indexed
    private String clienteId;

    @Indexed
    private String recepcionistaId;

    @Indexed
    private String empresaId;

    /** Valores: pendiente, proceso, urgente, completado, cancelado */
    @Indexed
    private String estado;

    /** Valores: normal, urgente */
    @Indexed
    private String prioridad;

    /** Actividades embebidas — siempre se leen con el trámite */
    @Builder.Default
    private List<Actividad> actividades = new ArrayList<>();

    private LocalDateTime fecha;
    private LocalDateTime fechaFin;

    @CreatedDate
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    private LocalDateTime fechaModificacion;

}
