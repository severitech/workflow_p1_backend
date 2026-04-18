package com.workflow.document;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tipo_componentes")
public class TipoComponente {

    @Id
    private String id;

    /** Valores posibles: texto, textarea, select, boolean, fecha, archivo, numero */
    private String nombre;
    private String descripcion;

    @CreatedDate
    private LocalDateTime fechaCreacion;
}
