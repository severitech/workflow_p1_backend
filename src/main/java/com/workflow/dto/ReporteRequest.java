package com.workflow.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/** Solicitud para ejecutar un reporte dinámico generado por la IA */
@Data
public class ReporteRequest {
    /** Colección MongoDB a consultar: "tramites", "usuarios", "politicas", "documentos" */
    private String coleccion;

    /** Filtros campo-valor aplicados a la query */
    private Map<String, Object> filtros;

    /** Campos a proyectar en los resultados (vacío = todos) */
    private List<String> campos;

    private String fechaDesde;
    private String fechaHasta;
    private Integer limite = 100;
}
