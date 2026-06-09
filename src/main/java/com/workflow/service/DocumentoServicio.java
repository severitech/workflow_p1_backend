package com.workflow.service;

import com.workflow.document.BitacoraDocumento;
import com.workflow.document.Documento;
import com.workflow.document.Documento.VersionHistorial;
import com.workflow.document.PermisoDocumento;
import com.workflow.dto.CrearDocumentoTextoRequest;
import com.workflow.dto.DocumentoRespuesta;
import com.workflow.dto.EditarDocumentoMsg;
import com.workflow.dto.PermisoDocumentoRequest;
import com.workflow.exception.AccesoDenegadoException;
import com.workflow.document.Notificacion;
import com.workflow.repository.BitacoraDocumentoRepositorio;
import com.workflow.repository.DocumentoRepositorio;
import com.workflow.repository.NotificacionRepository;
import com.workflow.repository.PermisoDocumentoRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
    private final com.workflow.repository.UsuarioRepository usuarioRepository;
    private final com.workflow.repository.EmpresaRepository empresaRepository;
    private final com.workflow.repository.PoliticaNegocioRepository politicaRepository;
    private final AlmacenamientoServicio almacenamiento;
    private final SimpMessagingTemplate mensajeria;
    private final NotificacionRepository notificacionRepository;

    @org.springframework.beans.factory.annotation.Value("${onlyoffice.url-publica}")
    private String onlyOfficeUrlPublica;

    /**
     * Construye la ruta de almacenamiento de un documento de política/workflow:
     *   {nombreEmpresa}/workflow/{nombrePolitica}
     * Si el documento no está asociado a una política, se usa "general".
     */
    private String construirRutaWorkflow(String empresaId, String politicaId) {
        String nombreEmpresa = empresaRepository.findById(empresaId)
                .map(com.workflow.document.Empresa::getNombre).orElse(empresaId);
        String nombrePolitica = (politicaId == null || politicaId.isBlank())
                ? "general"
                : politicaRepository.findById(politicaId)
                        .map(com.workflow.document.PoliticaNegocio::getNombre).orElse(politicaId);

        return sanearRuta(nombreEmpresa) + "/workflow/" + sanearRuta(nombrePolitica);
    }

    /** Reemplaza caracteres no válidos para una ruta/clave de S3 (barras, espacios extra, etc.) */
    private String sanearRuta(String segmento) {
        if (segmento == null || segmento.isBlank()) return "sin-nombre";
        return segmento.trim().replaceAll("[/\\\\]+", "-").replaceAll("\\s+", " ");
    }

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
            String politicaId,
            String descripcion
    ) throws IOException {

        String tipo = obtenerTipo(archivo.getOriginalFilename());
        String clave = almacenamiento.guardar(archivo, construirRutaWorkflow(empresaId, politicaId));

        Documento doc = Documento.builder()
                .nombre(archivo.getOriginalFilename())
                .tipo(tipo)
                .tamanio(archivo.getSize())
                .empresaId(empresaId)
                .tramiteId(tramiteId)
                .actividadId(actividadId)
                .politicaId(politicaId)
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

        registrarBitacora(doc.getId(), creadoPor, creadoPor, "subio",
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
        String nuevaClave = almacenamiento.guardar(archivo, construirRutaWorkflow(doc.getEmpresaId(), doc.getPoliticaId()));

        doc.setVersion(nuevaVersion);
        doc.setClaveAlmacenamiento(nuevaClave);
        doc.setNombre(archivo.getOriginalFilename());
        doc.setTamanio(archivo.getSize());
        doc.setTipo(obtenerTipo(archivo.getOriginalFilename()));

        doc = documentoRepositorio.save(doc);

        registrarBitacora(doc.getId(), usuarioId, usuarioId, "nueva_version",
                "Subió nueva versión " + nuevaVersion + " del documento '" + doc.getNombre() + "'");

        String nivel = obtenerNivelPermiso(documentoId, usuarioId);
        return toRespuesta(doc, nivel);
    }

    /** Crea un documento editable generando el archivo .docx/.xlsx real con Apache POI */
    public DocumentoRespuesta crearDocumentoTexto(CrearDocumentoTextoRequest req) throws IOException {
        String tipo = req.getTipo() != null ? req.getTipo() : "docx";
        byte[] bytes = generarArchivoVacio(tipo);
        String clave = almacenamiento.guardarBytes(bytes, construirRutaWorkflow(req.getEmpresaId(), req.getPoliticaId()), tipo);

        Documento doc = Documento.builder()
                .nombre(req.getNombre())
                .tipo(tipo)
                .tamanio(bytes.length)
                .empresaId(req.getEmpresaId())
                .politicaId(req.getPoliticaId())
                .claveAlmacenamiento(clave)
                .version("1.0")
                .versionesAnteriores(new ArrayList<>())
                .creadoPor(req.getCreadoPor())
                .fechaCreacion(LocalDateTime.now())
                .activo(true)
                .descripcion(req.getDescripcion())
                .esTexto(true)
                .build();

        doc = documentoRepositorio.save(doc);

        permisoRepositorio.save(PermisoDocumento.builder()
                .documentoId(doc.getId())
                .usuarioId(req.getCreadoPor())
                .nivel("editor")
                .otorgadoPor(req.getCreadoPor())
                .build());

        registrarBitacora(doc.getId(), req.getCreadoPor(), req.getCreadoPor(), "creo",
                "Creó el documento '" + doc.getNombre() + "'");

        return toRespuesta(doc, "editor");
    }

    /** Devuelve la entidad cruda (sin validar permiso) — usado solo para servir el archivo a OnlyOffice */
    public Documento obtenerRaw(String documentoId) {
        return obtenerOLanzar(documentoId);
    }

    /**
     * Descarga el archivo actualizado desde OnlyOffice y lo guarda como nueva versión del documento.
     * Llamado desde el callback de OnlyOffice (status 2 o 6).
     */
    public void guardarDesdeOnlyOffice(String documentoId, String urlArchivo, String usuarioId) {
        try {
            // OnlyOffice devuelve la URL con su host/IP interno del contenedor,
            // que no es alcanzable desde este backend — se reemplaza por la URL pública configurada
            URI original = URI.create(urlArchivo);
            URI publica = URI.create(onlyOfficeUrlPublica);
            URI urlDescarga = new URI(publica.getScheme(), null, publica.getHost(), publica.getPort(),
                    original.getRawPath(), original.getRawQuery(), original.getRawFragment());

            byte[] bytes;
            try (InputStream in = urlDescarga.toURL().openStream()) {
                bytes = in.readAllBytes();
            }

            Documento doc = obtenerOLanzar(documentoId);
            String nuevaClave = almacenamiento.guardarBytes(bytes, construirRutaWorkflow(doc.getEmpresaId(), doc.getPoliticaId()), doc.getTipo());
            doc.setClaveAlmacenamiento(nuevaClave);
            doc.setTamanio(bytes.length);
            documentoRepositorio.save(doc);

            log.info("[OnlyOffice] Documento {} actualizado ({} bytes)", documentoId, bytes.length);

            String nombreUsuario = usuarioId;
            if (usuarioId != null) {
                nombreUsuario = usuarioRepository.findById(usuarioId)
                        .map(u -> u.getNombre() + " " + u.getApellido())
                        .orElse(usuarioId);
            } else {
                usuarioId = "onlyoffice";
                nombreUsuario = "OnlyOffice";
            }

            registrarBitacora(documentoId, usuarioId, nombreUsuario, "edito",
                    "Editó el documento desde OnlyOffice — " + bytes.length + " bytes (v" + doc.getVersion() + ")");
            bitacoraRepositorio.findByDocumentoIdOrderByFechaDesc(documentoId).stream().findFirst().ifPresent(entrada ->
                    mensajeria.convertAndSend("/topic/documento/" + documentoId + "/bitacora", entrada));
        } catch (Exception e) {
            log.error("[OnlyOffice] Error al guardar documento {}: {}", documentoId, e.getMessage());
        }
    }

    /** Genera un archivo .docx o .xlsx vacío y válido usando Apache POI */
    private byte[] generarArchivoVacio(String tipo) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if ("xlsx".equals(tipo)) {
            try (XSSFWorkbook wb = new XSSFWorkbook()) {
                wb.createSheet("Hoja1");
                wb.write(out);
            }
        } else {
            try (XWPFDocument word = new XWPFDocument()) {
                word.createParagraph();
                word.write(out);
            }
        }
        return out.toByteArray();
    }

    /** Obtiene un documento por ID */
    public DocumentoRespuesta obtenerDocumento(String documentoId, String usuarioId) {
        Documento doc = obtenerOLanzar(documentoId);
        verificarAccesoMinimo(documentoId, usuarioId);
        String nivel = obtenerNivelPermiso(documentoId, usuarioId);
        return toRespuesta(doc, nivel);
    }

    /** Actualiza el contenido de texto de un documento vía REST (para editores) */
    public DocumentoRespuesta actualizarContenido(String documentoId, String usuarioId, String nombreUsuario, String contenido) {
        verificarPermiso(documentoId, usuarioId, "editor");
        Documento doc = obtenerOLanzar(documentoId);
        doc.setContenido(contenido);
        doc.setTamanio((long) contenido.length());
        documentoRepositorio.save(doc);

        registrarBitacora(documentoId, usuarioId, nombreUsuario, "edito",
                "Editó el documento — " + contenido.length() + " caracteres");

        String nivel = obtenerNivelPermiso(documentoId, usuarioId);
        return toRespuesta(doc, nivel);
    }

    /**
     * Procesa una edición colaborativa vía WebSocket:
     * guarda el contenido, registra bitácora y hace broadcast.
     */
    public void procesarEdicionWs(EditarDocumentoMsg msg) {
        String documentoId = msg.getDocumentoId();
        verificarPermiso(documentoId, msg.getUsuarioId(), "editor");

        Documento doc = obtenerOLanzar(documentoId);
        doc.setContenido(msg.getContenido());
        doc.setTamanio((long) msg.getContenido().length());
        documentoRepositorio.save(doc);

        BitacoraDocumento entrada = BitacoraDocumento.builder()
                .documentoId(documentoId)
                .usuarioId(msg.getUsuarioId())
                .nombreUsuario(msg.getNombreUsuario())
                .accion("edito")
                .detalle("Editó el documento — " + msg.getContenido().length() + " chars")
                .fecha(LocalDateTime.now())
                .build();
        bitacoraRepositorio.save(entrada);

        // Broadcast contenido a todos los viewers
        mensajeria.convertAndSend("/topic/documento/" + documentoId + "/contenido", msg);
        // Broadcast nueva entrada de bitácora
        mensajeria.convertAndSend("/topic/documento/" + documentoId + "/bitacora", entrada);
    }

    /** Lista documentos de un trámite específico */
    public List<DocumentoRespuesta> listarPorTramite(String tramiteId, String usuarioId) {
        return documentoRepositorio.findByTramiteIdAndActivoTrue(tramiteId)
                .stream()
                .map(doc -> toRespuesta(doc, obtenerNivelPermiso(doc.getId(), usuarioId)))
                .collect(Collectors.toList());
    }

    /** Lista documentos asociados a una política de negocio */
    public List<DocumentoRespuesta> listarPorPolitica(String politicaId, String usuarioId) {
        return documentoRepositorio.findByPoliticaIdAndActivoTrue(politicaId)
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

        registrarBitacora(documentoId, usuarioId, usuarioId, "descargo",
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

        registrarBitacora(documentoId, solicitanteId, solicitanteId, "cambio_permiso",
                "Asignó permiso '" + req.getNivel() + "' al usuario " + req.getUsuarioId());
        mensajeria.convertAndSend("/topic/documento/" + documentoId + "/bitacora",
                bitacoraRepositorio.findByDocumentoIdOrderByFechaDesc(documentoId).stream().findFirst().orElse(null));

        Documento doc = obtenerOLanzar(documentoId);
        String etiquetaNivel = switch (req.getNivel()) {
            case "editor"      -> "editor";
            case "comentador"  -> "comentador";
            case "visualizador"-> "lector";
            default            -> req.getNivel();
        };
        String mensajeNotif = "Se te asignó acceso al documento \"" + doc.getNombre()
                + "\". Ahora eres " + etiquetaNivel + ".";
        Notificacion notif = Notificacion.builder()
                .usuarioId(req.getUsuarioId())
                .tipo("permiso_documento")
                .mensaje(mensajeNotif)
                .leida(false)
                .build();
        notificacionRepository.save(notif);
        mensajeria.convertAndSend("/queue/notificaciones/" + req.getUsuarioId(), notif);
    }

    /** Revoca el acceso de un usuario sobre un documento */
    public void revocarPermiso(String documentoId, String solicitanteId, String usuarioId) {
        verificarPermiso(documentoId, solicitanteId, "editor");

        Documento doc = obtenerOLanzar(documentoId);
        permisoRepositorio.findByDocumentoIdAndUsuarioId(documentoId, usuarioId)
                .ifPresent(permisoRepositorio::delete);

        registrarBitacora(documentoId, solicitanteId, solicitanteId, "cambio_permiso",
                "Revocó el acceso del usuario " + usuarioId);
        mensajeria.convertAndSend("/topic/documento/" + documentoId + "/bitacora",
                bitacoraRepositorio.findByDocumentoIdOrderByFechaDesc(documentoId).stream().findFirst().orElse(null));

        String mensajeRevocado = "Tu acceso al documento \"" + doc.getNombre() + "\" fue revocado.";
        Notificacion notifRevocado = Notificacion.builder()
                .usuarioId(usuarioId)
                .tipo("permiso_documento")
                .mensaje(mensajeRevocado)
                .leida(false)
                .build();
        notificacionRepository.save(notifRevocado);
        mensajeria.convertAndSend("/queue/notificaciones/" + usuarioId, notifRevocado);
    }

    /** Lista los permisos de un documento */
    public List<PermisoDocumento> listarPermisos(String documentoId, String usuarioId) {
        verificarAccesoMinimo(documentoId, usuarioId);
        return permisoRepositorio.findByDocumentoId(documentoId);
    }

    /** Agrega un comentario a la bitácora */
    public void comentar(String documentoId, String usuarioId, String comentario) {
        verificarAccesoMinimo(documentoId, usuarioId, "comentador");
        registrarBitacora(documentoId, usuarioId, usuarioId, "comento", comentario);
        bitacoraRepositorio.findByDocumentoIdOrderByFechaDesc(documentoId).stream().findFirst().ifPresent(entrada ->
                mensajeria.convertAndSend("/topic/documento/" + documentoId + "/bitacora", entrada));
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
        registrarBitacora(documentoId, usuarioId, usuarioId, "elimino",
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
            throw new AccesoDenegadoException("Sin permiso suficiente. Requerido: " + nivelRequerido + ", tienes: " + nivel);
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

    private void registrarBitacora(String documentoId, String usuarioId, String nombreUsuario, String accion, String detalle) {
        bitacoraRepositorio.save(BitacoraDocumento.builder()
                .documentoId(documentoId)
                .usuarioId(usuarioId)
                .nombreUsuario(nombreUsuario)
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
        List<Documento.VersionHistorial> versiones = doc.getVersionesAnteriores();
        if (versiones == null) versiones = new ArrayList<>();

        List<DocumentoRespuesta.VersionInfo> historial = versiones.stream()
                .map(v -> DocumentoRespuesta.VersionInfo.builder()
                        .version(v.getVersion())
                        .fecha(v.getFecha())
                        .subidoPor(v.getSubidoPor())
                        .build())
                .collect(Collectors.toList());

        String urlDescarga = (doc.isEsTexto() || doc.getClaveAlmacenamiento() == null || doc.getClaveAlmacenamiento().isEmpty())
                ? "" : almacenamiento.generarUrlDescarga(doc.getClaveAlmacenamiento());

        return DocumentoRespuesta.builder()
                .id(doc.getId())
                .nombre(doc.getNombre())
                .tipo(doc.getTipo())
                .tamanio(doc.getTamanio())
                .empresaId(doc.getEmpresaId())
                .tramiteId(doc.getTramiteId())
                .actividadId(doc.getActividadId())
                .politicaId(doc.getPoliticaId())
                .version(doc.getVersion())
                .creadoPor(doc.getCreadoPor())
                .fechaCreacion(doc.getFechaCreacion())
                .activo(doc.isActivo())
                .descripcion(doc.getDescripcion())
                .contenido(doc.getContenido())
                .esTexto(doc.isEsTexto())
                .urlDescarga(urlDescarga)
                .miPermiso(nivelPermiso)
                .versionesAnteriores(historial)
                .build();
    }
}
