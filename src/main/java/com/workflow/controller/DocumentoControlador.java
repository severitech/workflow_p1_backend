package com.workflow.controller;

import com.workflow.document.BitacoraDocumento;
import com.workflow.document.PermisoDocumento;
import com.workflow.dto.ComentarioDocumentoRequest;
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

    /** Sube un documento nuevo al sistema */
    @PostMapping(value = "/subir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoRespuesta> subirDocumento(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("empresaId") String empresaId,
            @RequestParam("usuarioId") String usuarioId,
            @RequestParam(value = "tramiteId", required = false) String tramiteId,
            @RequestParam(value = "actividadId", required = false) String actividadId,
            @RequestParam(value = "descripcion", required = false) String descripcion
    ) throws IOException {
        DocumentoRespuesta respuesta = documentoServicio.subirDocumento(
                archivo, empresaId, usuarioId, tramiteId, actividadId, descripcion);
        return ResponseEntity.ok(respuesta);
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
    public ResponseEntity<Resource> servirArchivoLocal(@RequestParam String clave) throws IOException {
        Resource recurso = almacenamientoServicio.obtenerComoResource(clave);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getFilename() + "\"")
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
