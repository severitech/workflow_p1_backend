package com.workflow.controller;

import com.workflow.dto.ReporteRequest;
import com.workflow.service.ReporteServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controlador para reportes dinámicos generados por IA.
 * La IA construye la query estructurada y el backend la ejecuta aquí.
 */
@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteControlador {

    private final ReporteServicio reporteServicio;

    /** Ejecuta la query y devuelve los datos como JSON */
    @PostMapping("/ejecutar")
    public ResponseEntity<List<Map>> ejecutar(@RequestBody ReporteRequest req) {
        List<Map> resultados = reporteServicio.ejecutarReporte(req);
        return ResponseEntity.ok(resultados);
    }

    /** Exporta los resultados a Excel (.xlsx) */
    @PostMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestBody ReporteRequest req,
            @RequestParam(defaultValue = "Reporte") String titulo
    ) throws IOException {
        List<Map> datos = reporteServicio.ejecutarReporte(req);
        byte[] excel = reporteServicio.exportarExcel(datos, titulo);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(titulo + ".xlsx").build().toString())
            .body(excel);
    }

    /** Exporta los resultados a PDF */
    @PostMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestBody ReporteRequest req,
            @RequestParam(defaultValue = "Reporte") String titulo
    ) throws IOException {
        List<Map> datos = reporteServicio.ejecutarReporte(req);
        byte[] pdf = reporteServicio.exportarPdf(datos, titulo);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(titulo + ".pdf").build().toString())
            .body(pdf);
    }
}
