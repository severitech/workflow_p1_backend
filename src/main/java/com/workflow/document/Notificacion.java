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
@Document(collection = "notificaciones")
public class Notificacion {

    @Id
    private String id;

    @Indexed
    private String usuarioId;

    private String tramiteId;
    private String actividadId;
    private String tipo;
    private String mensaje;
    private boolean leida;

    @CreatedDate
    private LocalDateTime fecha;
}
