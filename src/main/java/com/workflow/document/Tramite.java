package com.workflow.document;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tramites")
@CompoundIndexes({
    @CompoundIndex(name = "empresa_estado", def = "{'empresaId': 1, 'estado': 1}"),
    @CompoundIndex(name = "recepcionista_semaforo", def = "{'recepcionistaId': 1, 'semaforo': 1}")
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

    /** Valores: verde, amarillo, rojo */
    @Indexed
    private String semaforo;

    /** Actividades embebidas — siempre se leen con el trámite */
    @Builder.Default
    private List<Actividad> actividades = new ArrayList<>();

    private LocalDateTime fecha;
    private LocalDateTime fechaFin;

    @CreatedDate
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    private LocalDateTime fechaModificacion;

    /** Calcula el semáforo según el tiempo transcurrido vs tiempo límite de la actividad activa */
    public String calcularSemaforo() {
        Actividad actividadActiva = actividades.stream()
                .filter(a -> "activo".equals(a.getEstado()))
                .findFirst()
                .orElse(null);

        if (actividadActiva == null || actividadActiva.getFechaInicio() == null) {
            return "verde";
        }

        long horasTranscurridas = ChronoUnit.HOURS.between(actividadActiva.getFechaInicio(), LocalDateTime.now());
        int tiempoLimite = actividadActiva.getTiempoLimite();

        if (tiempoLimite <= 0) return "verde";

        if (horasTranscurridas >= tiempoLimite) return "rojo";
        if (horasTranscurridas >= tiempoLimite * 0.75) return "amarillo";
        return "verde";
    }
}
