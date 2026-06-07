package com.workflow.controller;

import com.workflow.document.BitacoraDocumento;
import com.workflow.document.Documento;
import com.workflow.document.PermisoDocumento;
import com.workflow.dto.ComentarioDocumentoRequest;
import com.workflow.dto.CrearDocumentoTextoRequest;
import com.workflow.dto.DocumentoRespuesta;
import com.workflow.dto.PermisoDocumentoRequest;
import com.workflow.service.AlmacenamientoServicio;
import com.workflow.service.DocumentoServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión documental.
 * Base: POST /api/documentos
 */
@RestController
@RequestMapping("/documentos")
@RequiredArgsConstructor
public class DocumentoControlador {

    private final DocumentoServicio documentoServicio;
    private final AlmacenamientoServicio almacenamientoServicio;

    /** Crea un documento editable generando el archivo .docx/.xlsx real */
    @PostMapping("/crear-texto")
    public ResponseEntity<DocumentoRespuesta> crearDocumentoTexto(@RequestBody CrearDocumentoTextoRequest req) throws IOException {
        return ResponseEntity.ok(documentoServicio.crearDocumentoTexto(req));
    }

    /** Obtiene un documento por ID */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentoRespuesta> obtener(
            @PathVariable String id,
            @RequestParam String usuarioId
    ) {
        return ResponseEntity.ok(documentoServicio.obtenerDocumento(id, usuarioId));
    }

    /**
     * Sirve el archivo binario crudo para que OnlyOffice Document Server pueda cargarlo.
     * No requiere auth — OnlyOffice lo llama directamente desde el servidor Docker.
     */
    @GetMapping("/{id}/archivo-raw")
    public ResponseEntity<byte[]> archivoRaw(@PathVariable String id) throws IOException {
        Documento doc = documentoServicio.obtenerRaw(id);
        byte[] bytes = almacenamientoServicio.leerBytes(doc.getClaveAlmacenamiento());

        String contentType = switch (doc.getTipo()) {
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "pdf"  -> "application/pdf";
            default     -> "application/octet-stream";
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getNombre() + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .body(bytes);
    }

    /**
     * Callback de OnlyOffice Document Server.
     * Se llama cuando el usuario cierra el documento o cada vez que OnlyOffice guarda.
     * Responde {"error":0} para confirmar recepción.
     */
    @PostMapping("/{id}/onlyoffice-callback")
    public ResponseEntity<Map<String, Integer>> onlyOfficeCallback(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload
    ) {
        int status = ((Number) payload.get("status")).intValue();
        // status 2 = listo para guardar; status 6 = forzar guardado
        if (status == 2 || status == 6) {
            String url = (String) payload.get("url");
            String usuarioId = extraerUsuarioEditor(payload);
            documentoServicio.guardarDesdeOnlyOffice(id, url, usuarioId);
        }
        return ResponseEntity.ok(Map.of("error", 0));
    }

    /** Extrae el ID del usuario que realizó la edición desde el payload del callback de OnlyOffice */
    @SuppressWarnings("unchecked")
    private String extraerUsuarioEditor(Map<String, Object> payload) {
        Object actions = payload.get("actions");
        if (actions instanceof List<?> lista && !lista.isEmpty() && lista.get(0) instanceof Map<?, ?> primera) {
            Object userId = primera.get("userid");
            if (userId != null) return userId.toString();
        }
        Object users = payload.get("users");
        if (users instanceof List<?> lista && !lista.isEmpty()) {
            return lista.get(0).toString();
        }
        return null;
    }

    /** Sube un documento nuevo al sistema */
    @PostMapping(value = "/subir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoRespuesta> subirDocumento(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("empresaId") String empresaId,
            @RequestParam("usuarioId") String usuarioId,
            @RequestParam(value = "tramiteId", required = false) String tramiteId,
            @RequestParam(value = "actividadId", required = false) String actividadId,
            @RequestParam(value = "politicaId", required = false) String politicaId,
            @RequestParam(value = "descripcion", required = false) String descripcion
    ) throws IOException {
        DocumentoRespuesta respuesta = documentoServicio.subirDocumento(
                archivo, empresaId, usuarioId, tramiteId, actividadId, politicaId, descripcion);
        return ResponseEntity.ok(respuesta);
    }

    /** Lista documentos asociados a una política de negocio */
    @GetMapping("/politica/{politicaId}")
    public ResponseEntity<List<DocumentoRespuesta>> listarPorPolitica(
            @PathVariable String politicaId,
            @RequestParam String usuarioId
    ) {
        return ResponseEntity.ok(documentoServicio.listarPorPolitica(politicaId, usuarioId));
    }

    /** Lista los documentos asociados a un trámite */
    @GetMapping("/tramite/{tramiteId}")
    public ResponseEntity<List<DocumentoRespuesta>> listarPorTramite(
            @PathVariable String tramiteId,
            @RequestParam String usuarioId
    ) {
        return ResponseEntity.ok(documentoServicio.listarPorTramite(tramiteId, usuarioId));
    }

    /** Lista todos los documentos de una empresa (repositorio general) */
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<DocumentoRespuesta>> listarPorEmpresa(
            @PathVariable String empresaId,
            @RequestParam String usuarioId
    ) {
        return ResponseEntity.ok(documentoServicio.listarPorEmpresa(empresaId, usuarioId));
    }

    /** Genera URL temporal de descarga para el documento */
    @GetMapping("/{id}/url-descarga")
    public ResponseEntity<String> urlDescarga(
            @PathVariable String id,
            @RequestParam String usuarioId
    ) {
        return ResponseEntity.ok(documentoServicio.generarUrlDescarga(id, usuarioId));
    }

    /**
     * Sirve el archivo directamente (solo para almacenamiento local).
     * En producción con S3, se usa la URL prefirmada.
     */
    @GetMapping("/archivo/**")
    public ResponseEntity<Resource> servirArchivoLocal(jakarta.servlet.http.HttpServletRequest request) throws IOException {
        String ruta = (String) request.getAttribute(org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String clave = ruta.substring(ruta.indexOf("/archivo/") + "/archivo/".length());
        Resource recurso = almacenamientoServicio.obtenerComoResource(clave);

        String nombre = recurso.getFilename() != null ? recurso.getFilename() : "";
        String extension = nombre.contains(".") ? nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase() : "";
        String contentType = switch (extension) {
            case "pdf"  -> "application/pdf";
            case "png"  -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif"  -> "image/gif";
            case "webp" -> "image/webp";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default     -> "application/octet-stream";
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombre + "\"")
                .body(recurso);
    }

    /** Sube una nueva versión de un documento existente */
    @PostMapping(value = "/{id}/nueva-version", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoRespuesta> nuevaVersion(
            @PathVariable String id,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("usuarioId") String usuarioId
    ) throws IOException {
        return ResponseEntity.ok(documentoServicio.subirNuevaVersion(id, archivo, usuarioId));
    }

    /** Historial de versiones anteriores de un documento */
    @GetMapping("/{id}/versiones")
    public ResponseEntity<List<DocumentoRespuesta.VersionInfo>> listarVersiones(
            @PathVariable String id,
            @RequestParam String usuarioId
    ) {
        return ResponseEntity.ok(documentoServicio.listarVersiones(id, usuarioId));
    }

    /** Asigna o actualiza el permiso de un usuario sobre el documento */
    @PostMapping("/{id}/permisos")
    public ResponseEntity<Void> asignarPermiso(
            @PathVariable String id,
            @RequestParam String solicitanteId,
            @RequestBody PermisoDocumentoRequest req
    ) {
        documentoServicio.asignarPermiso(id, solicitanteId, req);
        return ResponseEntity.ok().build();
    }

    /** Revoca el acceso de un usuario sobre el documento */
    @DeleteMapping("/{id}/permisos/{usuarioId}")
    public ResponseEntity<Void> revocarPermiso(
            @PathVariable String id,
            @PathVariable String usuarioId,
            @RequestParam String solicitanteId
    ) {
        documentoServicio.revocarPermiso(id, solicitanteId, usuarioId);
        return ResponseEntity.ok().build();
    }

    /** Lista los permisos actuales del documento */
    @GetMapping("/{id}/permisos")
    public ResponseEntity<List<PermisoDocumento>> listarPermisos(
            @PathVariable String id,
            @RequestParam String usuarioId
    ) {
        return ResponseEntity.ok(documentoServicio.listarPermisos(id, usuarioId));
    }

    /** Agrega un comentario a la bitácora del documento */
    @PostMapping("/{id}/comentario")
    public ResponseEntity<Void> comentar(
            @PathVariable String id,
            @RequestBody ComentarioDocumentoRequest req
    ) {
        documentoServicio.comentar(id, req.getUsuarioId(), req.getComentario());
        return ResponseEntity.ok().build();
    }

    /** Lista la bitácora de actividad del documento */
    @GetMapping("/{id}/bitacora")
    public ResponseEntity<List<BitacoraDocumento>> listarBitacora(
            @PathVariable String id,
            @RequestParam String usuarioId
    ) {
        return ResponseEntity.ok(documentoServicio.listarBitacora(id, usuarioId));
    }

    /** Elimina lógicamente el documento */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String id,
            @RequestParam String usuarioId
    ) {
        documentoServicio.eliminar(id, usuarioId);
        return ResponseEntity.ok().build();
    }
}
