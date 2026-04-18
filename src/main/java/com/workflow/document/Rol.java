package com.workflow.document;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "roles")
public class Rol {

    @Id
    private String id;

    /** Valores posibles: cliente, recepcionista, administrador, tecnico, legal */
    private String nombre;
    private String descripcion;

    @CreatedDate
    private LocalDateTime fechaCreacion;
}
