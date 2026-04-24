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
@Document(collection = "politicas_negocio")
public class PoliticaNegocio {

    @Id
    private String id;

    private String nombre;
    private String descripcion;

    /** Valores: borrador, activa, archivada */
    @Indexed
    private String estado;

    private int version;

    @Indexed
    private String empresaId;

    private String creadoPorId;

    @CreatedDate
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    private LocalDateTime fechaModificacion;
}
