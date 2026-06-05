package com.workflow.service;

import com.workflow.document.BitacoraDocumento;
import com.workflow.document.Documento;
import com.workflow.document.Documento.VersionHistorial;
import com.workflow.document.PermisoDocumento;
import com.workflow.dto.DocumentoRespuesta;
import com.workflow.dto.PermisoDocumentoRequest;
import com.workflow.repository.BitacoraDocumentoRepositorio;
import com.workflow.repository.DocumentoRepositorio;
import com.workflow.repository.PermisoDocumentoRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Lógica de negocio para gestión documental */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentoServicio {

    private final DocumentoRepositorio documentoRepositorio;
    private final PermisoDocumentoRepositorio permisoRepositorio;
    private final BitacoraDocumentoRepositorio bitacoraRepositorio;
    private final AlmacenamientoServicio almacenamiento;

    /**
     * Sube un nuevo documento al sistema.
     * El dueño (creadoPor) tiene nivel "editor" por defecto.
     */
    public DocumentoRespuesta subirDocumento(
            MultipartFile archivo,
            String empresaId,
            String creadoPor,
            String tramiteId,
            String actividadId,
            String descripcion
    ) throws IOException {

        String tipo = obtenerTipo(archivo.getOriginalFilename());
        String clave = almacenamiento.guardar(archivo, empresaId);

        Documento doc = Documento.builder()
                .nombre(archivo.getOriginalFilename())
                .tipo(tipo)
                .tamanio(archivo.getSize())
                .empresaId(empresaId)
                .tramiteId(tramiteId)
                .actividadId(actividadId)
                .claveAlmacenamiento(clave)
                .version("1.0")
                .versionesAnteriores(new ArrayList<>())
                .creadoPor(creadoPor)
                .fechaCreacion(LocalDateTime.now())
                .activo(true)
                .descripcion(descripcion)
                .build();

        doc = documentoRepositorio.save(doc);

        // El creador tiene permiso de editor por defecto
        permisoRepositorio.save(PermisoDocumento.builder()
                .documentoId(doc.getId())
                .usuarioId(creadoPor)
                .nivel("editor")
                .otorgadoPor(creadoPor)
                .build());

        registrarBitacora(doc.getId(), creadoPor, "subio",
                "Subió el documento '" + doc.getNombre() + "' v" + doc.getVersion());

        return toRespuesta(doc, "editor");
    }

    /**
     * Sube una nueva versión de un documento existente.
     * Solo usuarios con nivel "editor" pueden hacerlo.
     */
    public DocumentoRespuesta subirNuevaVersion(
            String documentoId,
            MultipartFile archivo,
            String usuarioId
    ) throws IOException {

        Documento doc = obtenerOLanzar(documentoId);
        verificarPermiso(documentoId, usuarioId, "editor");

        // Guarda la versión actual en el historial
        VersionHistorial historial = VersionHistorial.builder()
                .version(doc.getVersion())
                .claveAlmacenamiento(doc.getClaveAlmacenamiento())
                .fecha(LocalDateTime.now())
                .subidoPor(usuarioId)
                .build();

        doc.getVersionesAnteriores().add(historial);

        // Calcula el siguiente número de versión
        String nuevaVersion = incrementarVersion(doc.getVersion());
        String nuevaClave = almacenamiento.guardar(archivo, doc.getEmpresaId());

        doc.setVersion(nuevaVersion);
        doc.setClaveAlmacenamiento(nuevaClave);
        doc.setNombre(archivo.getOriginalFilename());
        doc.setTamanio(archivo.getSize());
        doc.setTipo(obtenerTipo(archivo.getOriginalFilename()));

        doc = documentoRepositorio.save(doc);

        registrarBitacora(doc.getId(), usuarioId, "nueva_version",
                "Subió nueva versión " + nuevaVersion + " del documento '" + doc.getNombre() + "'");

        String nivel = obtenerNivelPermiso(documentoId, usuarioId);
        return toRespuesta(doc, nivel);
    }

    /** Lista documentos de un trámite específico */
    public List<DocumentoRespuesta> listarPorTramite(String tramiteId, String usuarioId) {
        return documentoRepositorio.findByTramiteIdAndActivoTrue(tramiteId)
                .stream()
                .map(doc -> toRespuesta(doc, obtenerNivelPermiso(doc.getId(), usuarioId)))
                .collect(Collectors.toList());
    }

    /** Lista todos los documentos de una empresa (repositorio general) */
    public List<DocumentoRespuesta> listarPorEmpresa(String empresaId, String usuarioId) {
        return documentoRepositorio.findByEmpresaIdAndActivoTrue(empresaId)
                .stream()
                .map(doc -> toRespuesta(doc, obtenerNivelPermiso(doc.getId(), usuarioId)))
                .collect(Collectors.toList());
    }

    /** Genera URL de descarga temporal y registra en bitácora */
    public String generarUrlDescarga(String documentoId, String usuarioId) {
        Documento doc = obtenerOLanzar(documentoId);
        verificarAccesoMinimo(documentoId, usuarioId);

        registrarBitacora(documentoId, usuarioId, "descargo",
                "Descargó el documento '" + doc.getNombre() + "'");

        return almacenamiento.generarUrlDescarga(doc.getClaveAlmacenamiento());
    }

    /** Historial de versiones de un documento */
    public List<DocumentoRespuesta.VersionInfo> listarVersiones(String documentoId, String usuarioId) {
        Documento doc = obtenerOLanzar(documentoId);
        verificarAccesoMinimo(documentoId, usuarioId);

        return doc.getVersionesAnteriores().stream()
                .map(v -> DocumentoRespuesta.VersionInfo.builder()
                        .version(v.getVersion())
                        .fecha(v.getFecha())
                        .subidoPor(v.getSubidoPor())
                        .build())
                .collect(Collectors.toList());
    }

    /** Asigna o actualiza el permiso de un usuario sobre un documento */
    public void asignarPermiso(String documentoId, String solicitanteId, PermisoDocumentoRequest req) {
        verificarPermiso(documentoId, solicitanteId, "editor");

        var permisoExistente = permisoRepositorio
                .findByDocumentoIdAndUsuarioId(documentoId, req.getUsuarioId());

        if (permisoExistente.isPresent()) {
            PermisoDocumento permiso = permisoExistente.get();
            permiso.setNivel(req.getNivel());
            permisoRepositorio.save(permiso);
        } else {
            permisoRepositorio.save(PermisoDocumento.builder()
                    .documentoId(documentoId)
                    .usuarioId(req.getUsuarioId())
                    .nivel(req.getNivel())
                    .otorgadoPor(solicitanteId)
                    .build());
        }

        registrarBitacora(documentoId, solicitanteId, "cambio_permiso",
                "Asignó permiso '" + req.getNivel() + "' al usuario " + req.getUsuarioId());
    }

    /** Lista los permisos de un documento */
    public List<PermisoDocumento> listarPermisos(String documentoId, String usuarioId) {
        verificarAccesoMinimo(documentoId, usuarioId);
        return permisoRepositorio.findByDocumentoId(documentoId);
    }

    /** Agrega un comentario a la bitácora */
    public void comentar(String documentoId, String usuarioId, String comentario) {
        verificarAccesoMinimo(documentoId, usuarioId, "comentador");
        registrarBitacora(documentoId, usuarioId, "comento", comentario);
    }

    /** Retorna la bitácora del documento */
    public List<BitacoraDocumento> listarBitacora(String documentoId, String usuarioId) {
        verificarAccesoMinimo(documentoId, usuarioId);
        return bitacoraRepositorio.findByDocumentoIdOrderByFechaDesc(documentoId);
    }

    /** Elimina lógicamente el documento (no borra el archivo físico) */
    public void eliminar(String documentoId, String usuarioId) {
        verificarPermiso(documentoId, usuarioId, "editor");
        Documento doc = obtenerOLanzar(documentoId);
        doc.setActivo(false);
        documentoRepositorio.save(doc);
        registrarBitacora(documentoId, usuarioId, "elimino",
                "Eliminó el documento '" + doc.getNombre() + "'");
    }

    // ─── Helpers privados ──────────────────────────────────────────────────────

    private Documento obtenerOLanzar(String id) {
        return documentoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + id));
    }

    private String obtenerNivelPermiso(String documentoId, String usuarioId) {
        return permisoRepositorio.findByDocumentoIdAndUsuarioId(documentoId, usuarioId)
                .map(PermisoDocumento::getNivel)
                .orElse("sin_permiso");
    }

    private void verificarPermiso(String documentoId, String usuarioId, String nivelRequerido) {
        String nivel = obtenerNivelPermiso(documentoId, usuarioId);
        if (!tieneNivelSuficiente(nivel, nivelRequerido)) {
            throw new RuntimeException("Sin permiso suficiente. Requerido: " + nivelRequerido + ", tienes: " + nivel);
        }
    }

    private void verificarAccesoMinimo(String documentoId, String usuarioId) {
        verificarAccesoMinimo(documentoId, usuarioId, "visualizador");
    }

    private void verificarAccesoMinimo(String documentoId, String usuarioId, String nivelMinimo) {
        verificarPermiso(documentoId, usuarioId, nivelMinimo);
    }

    /** Jerarquía: editor > comentador > visualizador */
    private boolean tieneNivelSuficiente(String nivelActual, String nivelRequerido) {
        List<String> jerarquia = List.of("visualizador", "comentador", "editor");
        int actual = jerarquia.indexOf(nivelActual);
        int requerido = jerarquia.indexOf(nivelRequerido);
        return actual >= requerido;
    }

    private void registrarBitacora(String documentoId, String usuarioId, String accion, String detalle) {
        bitacoraRepositorio.save(BitacoraDocumento.builder()
                .documentoId(documentoId)
                .usuarioId(usuarioId)
                .accion(accion)
                .detalle(detalle)
                .fecha(LocalDateTime.now())
                .build());
    }

    private String incrementarVersion(String version) {
        try {
            String[] partes = version.split("\\.");
            int mayor = Integer.parseInt(partes[0]);
            int menor = Integer.parseInt(partes[1]);
            return mayor + "." + (menor + 1);
        } catch (Exception e) {
            return version + ".1";
        }
    }

    private String obtenerTipo(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) return "bin";
        return nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1).toLowerCase();
    }

    private DocumentoRespuesta toRespuesta(Documento doc, String nivelPermiso) {
        List<DocumentoRespuesta.VersionInfo> historial = doc.getVersionesAnteriores().stream()
                .map(v -> DocumentoRespuesta.VersionInfo.builder()
                        .version(v.getVersion())
                        .fecha(v.getFecha())
                        .subidoPor(v.getSubidoPor())
                        .build())
                .collect(Collectors.toList());

        return DocumentoRespuesta.builder()
                .id(doc.getId())
                .nombre(doc.getNombre())
                .tipo(doc.getTipo())
                .tamanio(doc.getTamanio())
                .empresaId(doc.getEmpresaId())
                .tramiteId(doc.getTramiteId())
                .actividadId(doc.getActividadId())
                .version(doc.getVersion())
                .creadoPor(doc.getCreadoPor())
                .fechaCreacion(doc.getFechaCreacion())
                .activo(doc.isActivo())
                .descripcion(doc.getDescripcion())
                .urlDescarga(almacenamiento.generarUrlDescarga(doc.getClaveAlmacenamiento()))
                .miPermiso(nivelPermiso)
                .versionesAnteriores(historial)
                .build();
    }
}
