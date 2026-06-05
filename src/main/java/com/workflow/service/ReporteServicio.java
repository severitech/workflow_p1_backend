package com.workflow.service;

import com.workflow.dto.ReporteRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Ejecuta reportes dinámicos contra MongoDB y los exporta a PDF/Excel */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteServicio {

    private final MongoTemplate mongoTemplate;

    /** Colecciones permitidas para consultas dinámicas */
    private static final Set<String> COLECCIONES_PERMITIDAS = Set.of(
        "tramites", "usuarios", "politicas", "documentos",
        "flujos", "formularios", "bitacora_documentos"
    );

    /**
     * Ejecuta la query dinámica y devuelve los resultados como lista de mapas.
     * Valida que la colección sea una de las permitidas para evitar acceso no autorizado.
     */
    public List<Map> ejecutarReporte(ReporteRequest req) {
        String coleccion = req.getColeccion();
        if (!COLECCIONES_PERMITIDAS.contains(coleccion)) {
            throw new IllegalArgumentException("Colección no permitida: " + coleccion);
        }

        Query query = new Query();

        // Aplica filtros si existen
        if (req.getFiltros() != null) {
            req.getFiltros().forEach((campo, valor) -> {
                if (valor != null && !valor.toString().isBlank()) {
                    query.addCriteria(Criteria.where(campo).is(valor));
                }
            });
        }

        // Aplica rango de fechas si se especificó (campo "fecha" por defecto)
        if (req.getFechaDesde() != null || req.getFechaHasta() != null) {
            Criteria fechaCriteria = Criteria.where("fecha");
            if (req.getFechaDesde() != null) fechaCriteria = fechaCriteria.gte(req.getFechaDesde());
            if (req.getFechaHasta() != null) fechaCriteria = fechaCriteria.lte(req.getFechaHasta());
            query.addCriteria(fechaCriteria);
        }

        // Proyección de campos
        if (req.getCampos() != null && !req.getCampos().isEmpty()) {
            org.springframework.data.mongodb.core.query.Field fields = query.fields();
            req.getCampos().forEach(c -> fields.include(c));
        }

        // Límite
        int limite = req.getLimite() != null ? Math.min(req.getLimite(), 500) : 100;
        query.limit(limite);

        return mongoTemplate.find(query, Map.class, coleccion);
    }

    /** Exporta los resultados a un archivo Excel (.xlsx) */
    public byte[] exportarExcel(List<Map> datos, String titulo) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(titulo.length() > 31 ? titulo.substring(0, 31) : titulo);

            if (datos.isEmpty()) {
                sheet.createRow(0).createCell(0).setCellValue("Sin resultados");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                return out.toByteArray();
            }

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Encabezados usando las claves del primer registro
            List<String> columnas = datos.get(0).keySet().stream()
                .filter(k -> !k.equals("_id") && !k.equals("class"))
                .sorted()
                .toList();

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Datos
            for (int rowIdx = 0; rowIdx < datos.size(); rowIdx++) {
                Row row = sheet.createRow(rowIdx + 1);
                Map fila = datos.get(rowIdx);
                for (int colIdx = 0; colIdx < columnas.size(); colIdx++) {
                    Object valor = fila.get(columnas.get(colIdx));
                    row.createCell(colIdx).setCellValue(valor != null ? valor.toString() : "");
                }
            }

            // Autoajuste de columnas
            for (int i = 0; i < columnas.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /** Exporta los resultados a PDF usando iText */
    public byte[] exportarPdf(List<Map> datos, String titulo) throws IOException {
        try (com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(
                new ByteArrayOutputStream())) {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(
                new com.itextpdf.kernel.pdf.PdfWriter(baos)
            );
            com.itextpdf.layout.Document doc = new com.itextpdf.layout.Document(pdfDoc,
                com.itextpdf.kernel.geom.PageSize.A4.rotate());

            // Título
            doc.add(new com.itextpdf.layout.element.Paragraph(titulo)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

            if (datos.isEmpty()) {
                doc.add(new com.itextpdf.layout.element.Paragraph("Sin resultados"));
                doc.close();
                return baos.toByteArray();
            }

            List<String> columnas = datos.get(0).keySet().stream()
                .filter(k -> !k.equals("_id") && !k.equals("class"))
                .sorted()
                .toList();

            // Tabla
            float[] anchos = new float[columnas.size()];
            java.util.Arrays.fill(anchos, 1f);
            com.itextpdf.layout.element.Table tabla =
                new com.itextpdf.layout.element.Table(anchos).useAllAvailableWidth();

            // Encabezados
            for (String col : columnas) {
                tabla.addHeaderCell(new com.itextpdf.layout.element.Cell()
                    .add(new com.itextpdf.layout.element.Paragraph(col).setBold().setFontSize(8))
                    .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            }

            // Filas
            for (Map fila : datos) {
                for (String col : columnas) {
                    Object valor = fila.get(col);
                    tabla.addCell(new com.itextpdf.layout.element.Cell()
                        .add(new com.itextpdf.layout.element.Paragraph(
                            valor != null ? valor.toString() : "").setFontSize(7)));
                }
            }

            doc.add(tabla);
            doc.close();
            return baos.toByteArray();
        }
    }
}
