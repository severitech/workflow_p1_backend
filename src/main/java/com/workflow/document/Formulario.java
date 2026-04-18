package com.workflow.document;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "formularios")
public class Formulario {

    @Id
    private String id;

    private String nombre;
    private String descripcion;

    @Indexed
    private String empresaId;

    /** Componentes siempre se leen junto al formulario → embebidos */
    private List<Componente> componentes;

    private boolean activo;

    @CreatedDate
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    private LocalDateTime fechaModificacion;
}
