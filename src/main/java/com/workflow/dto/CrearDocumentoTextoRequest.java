package com.workflow.dto;

import lombok.Data;

/** Petición REST para crear un documento editable (docx o xlsx) */
@Data
public class CrearDocumentoTextoRequest {
    private String nombre;
    /** "docx" o "xlsx" */
    private String tipo;
    private String empresaId;
    private String creadoPor;
    private String politicaId;
    private String descripcion;
    private String contenido;
}
