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
@Document(collection = "bitacora")
public class Bitacora {

    @Id
    private String id;

    @Indexed
    private String tramiteId;

    @Indexed
    private String usuarioId;

    private String accion;
    private String descripcion;
    private String estadoAnterior;
    private String estadoNuevo;

    @CreatedDate
    private LocalDateTime fecha;
}
