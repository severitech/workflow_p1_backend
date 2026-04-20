package com.workflow.service;

import com.workflow.document.*;
import com.workflow.dto.*;
import com.workflow.exception.WorkflowException;
import com.workflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final TramiteRepository tramiteRepository;
    private final PoliticaNegocioRepository politicaRepository;
    private final FlujoRepository flujoRepository;
    private final BitacoraRepository bitacoraRepository;
    private final NotificacionRepository notificacionRepository;
    private final SimpMessagingTemplate mensajeria;
    private final UsuarioRepository usuarioRepository;

    /** Crea un nuevo trámite a partir de una política, cliente, recepcionista y prioridad */
    public Tramite iniciarTramite(String politicaId, String clienteId, String recepcionistaId, String empresaId, String prioridad) {
        PoliticaNegocio politica = politicaRepository.findById(politicaId)
                .orElseThrow(() -> new WorkflowException("Política no encontrada: " + politicaId));

        if (!"activa".equals(politica.getEstado())) {
            throw new WorkflowException("La política no está activa");
        }

        Flujo primerFlujo = flujoRepository
                .findByPoliticaIdAndFlujoPadreIdIsNullOrderByOrden(politicaId)
                .stream().findFirst()
                .orElseThrow(() -> new WorkflowException("La política no tiene flujos configurados"));

        Actividad primeraActividad = crearActividad(primerFlujo, "activo");

        Tramite tramite = Tramite.builder()
                .politicaId(politicaId)
                .clienteId(clienteId)
                .recepcionistaId(recepcionistaId)
                .empresaId(empresaId)
                .estado("proceso")
                .prioridad(prioridad != null ? prioridad : "normal")
                .actividades(new ArrayList<>(List.of(primeraActividad)))
                .fecha(LocalDateTime.now())
                .build();

        tramite = tramiteRepository.save(tramite);

        registrarBitacora(tramite.getId(), recepcionistaId, "INICIAR", "Trámite iniciado", null, "proceso");
        emitirPanelEmpresa(empresaId);

        log.debug("Trámite iniciado: {}", tramite.getId());
        return tramite;
    }

    /** Completa la actividad activa, determina el siguiente flujo y avanza el trámite */
    public Tramite avanzarPaso(String tramiteId, String usuarioId, String observacion, List<Map<String, String>> datosForm) {
        Tramite tramite = obtenerTramite(tramiteId);

        Actividad actividadActiva = tramite.getActividades().stream()
                .filter(a -> "activo".equals(a.getEstado()))
                .findFirst()
                .orElseThrow(() -> new WorkflowException("No hay actividad activa en el trámite"));

        // Guardar datos del formulario en la actividad
        if (datosForm != null) {
            List<DatoForm> datos = new ArrayList<>();
            for (Map<String, String> dato : datosForm) {
                datos.add(DatoForm.builder()
                        .componenteId(dato.get("componenteId"))
                        .etiqueta(dato.get("etiqueta"))
                        .valor(dato.get("valor"))
                        .build());
            }
            if (actividadActiva.getDatosForm() == null) {
                actividadActiva.setDatosForm(DatosClienteForm.builder()
                        .formularioId(obtenerFormularioIdDeFlujo(actividadActiva.getFlujoId()))
                        .estado("completado")
                        .datos(datos)
                        .fecha(LocalDateTime.now())
                        .build());
            } else {
                actividadActiva.getDatosForm().setDatos(datos);
                actividadActiva.getDatosForm().setEstado("completado");
            }
        }

        actividadActiva.setEstado("completado");
        actividadActiva.setFechaFin(LocalDateTime.now());
        actividadActiva.setObservacion(observacion);

        // Determinar siguiente flujo
        Flujo siguienteFlujo = determinarSiguienteFlujo(
                tramite.getPoliticaId(),
                actividadActiva.getFlujoId(),
                actividadActiva.getDatosForm() != null ? actividadActiva.getDatosForm().getDatos() : List.of()
        );

        String estadoAnterior = tramite.getEstado();

        if (siguienteFlujo != null) {
            Actividad nuevaActividad = crearActividad(siguienteFlujo, "activo");
            tramite.getActividades().add(nuevaActividad);
            // Notificar a todos los usuarios del departamento responsable
            if (siguienteFlujo.getDepartamentoId() != null) {
                usuarioRepository.findByDepartamentoId(siguienteFlujo.getDepartamentoId())
                        .forEach(u -> enviarNotificacion(u.getId(), tramiteId, "ASIGNACION",
                                "Se asignó una nueva actividad en el trámite " + tramiteId));
            }
        } else {
            tramite.setEstado("completado");
            tramite.setFechaFin(LocalDateTime.now());
        }

        tramite = tramiteRepository.save(tramite);

        registrarBitacora(tramiteId, usuarioId, "AVANZAR", "Paso avanzado", estadoAnterior, tramite.getEstado());
        emitirEstadoTramite(tramite);
        emitirPanelEmpresa(tramite.getEmpresaId());

        return tramite;
    }

    /** Determina el siguiente flujo según condiciones o por orden secuencial */
    public Flujo determinarSiguienteFlujo(String politicaId, String flujoActualId, List<DatoForm> datosForm) {
        List<Flujo> hijos = flujoRepository.findByFlujoPadreId(flujoActualId);

        if (!hijos.isEmpty()) {
            Flujo incondicional = null;
            for (Flujo hijo : hijos) {
                if (hijo.getCondicionCampo() != null && hijo.getCondicionValor() != null) {
                    // Hijo condicional: evalúa si los datosForm coinciden
                    boolean coincide = datosForm.stream().anyMatch(d ->
                            hijo.getCondicionCampo().equalsIgnoreCase(d.getEtiqueta()) &&
                            hijo.getCondicionValor().equalsIgnoreCase(d.getValor()));
                    if (coincide) return hijo;
                } else if (incondicional == null) {
                    // Hijo sin condición = camino por defecto si ninguno condicional coincide
                    incondicional = hijo;
                }
            }
            if (incondicional != null) return incondicional;
        }

        // Sin hijos o sin coincidencia → siguiente flujo raíz por orden
        Flujo flujoActual = flujoRepository.findById(flujoActualId).orElse(null);
        if (flujoActual == null) return null;

        return flujoRepository
                .findFirstByPoliticaIdAndFlujoPadreIdIsNullAndOrdenGreaterThanOrderByOrden(
                        politicaId, flujoActual.getOrden())
                .orElse(null);
    }

    /** Guarda o actualiza un campo del formulario en la actividad (upsert por componenteId) */
    public DatoForm guardarCampoFormulario(String tramiteId, String actividadId,
                                           String componenteId, String etiqueta, String valor) {
        Tramite tramite = obtenerTramite(tramiteId);

        Actividad actividad = tramite.getActividades().stream()
                .filter(a -> actividadId.equals(a.getId()))
                .findFirst()
                .orElseThrow(() -> new WorkflowException("Actividad no encontrada: " + actividadId));

        if (actividad.getDatosForm() == null) {
            actividad.setDatosForm(DatosClienteForm.builder()
                    .estado("en_proceso")
                    .datos(new ArrayList<>())
                    .fecha(LocalDateTime.now())
                    .build());
        }

        List<DatoForm> datos = actividad.getDatosForm().getDatos();
        Optional<DatoForm> existente = datos.stream()
                .filter(d -> componenteId.equals(d.getComponenteId()))
                .findFirst();

        DatoForm datoActualizado;
        if (existente.isPresent()) {
            existente.get().setValor(valor);
            existente.get().setEtiqueta(etiqueta);
            datoActualizado = existente.get();
        } else {
            datoActualizado = DatoForm.builder()
                    .componenteId(componenteId)
                    .etiqueta(etiqueta)
                    .valor(valor)
                    .build();
            datos.add(datoActualizado);
        }

        tramiteRepository.save(tramite);
        return datoActualizado;
    }

    /** Agrega una observación a un campo del formulario de una actividad */
    public void agregarObservacion(String tramiteId, String actividadId, String componenteId,
                                   String usuarioId, String descripcion, String estado) {
        Tramite tramite = obtenerTramite(tramiteId);

        Actividad actividad = tramite.getActividades().stream()
                .filter(a -> actividadId.equals(a.getId()))
                .findFirst()
                .orElseThrow(() -> new WorkflowException("Actividad no encontrada: " + actividadId));

        if (actividad.getDatosForm() == null) {
            throw new WorkflowException("La actividad no tiene formulario");
        }

        Observacion obs = Observacion.builder()
                .usuarioId(usuarioId)
                .descripcion(descripcion)
                .estado(estado != null ? estado : "observado")
                .fecha(LocalDateTime.now())
                .build();

        actividad.getDatosForm().getObservaciones().add(obs);
        actividad.getDatosForm().setEstado("observado");

        tramiteRepository.save(tramite);
        registrarBitacora(tramiteId, usuarioId, "OBSERVACION", descripcion, null, estado);
    }

    /** Cambia la prioridad de un trámite y emite actualización por WebSocket */
    public Tramite cambiarPrioridad(String tramiteId, String prioridad) {
        Tramite tramite = obtenerTramite(tramiteId);
        tramite.setPrioridad(prioridad);
        tramite = tramiteRepository.save(tramite);
        emitirEstadoTramite(tramite);
        emitirPanelEmpresa(tramite.getEmpresaId());
        return tramite;
    }

    /** Retorna un trámite por su ID */
    public Tramite obtener(String tramiteId) {
        return obtenerTramite(tramiteId);
    }

    /** Retorna trámites activos de una empresa */
    public List<Tramite> obtenerActivos(String empresaId) {
        return tramiteRepository.findByEmpresaIdAndEstadoIn(empresaId, List.of("proceso", "urgente", "pendiente"));
    }

    /** Retorna trámites con prioridad urgente de una empresa */
    public List<Tramite> obtenerUrgentes(String empresaId) {
        return tramiteRepository.findByEmpresaIdAndPrioridad(empresaId, "urgente");
    }

    /** Retorna trámites donde el usuario es cliente o recepcionista */
    public List<Tramite> obtenerMisTramites(String usuarioId) {
        List<Tramite> comoCliente = tramiteRepository.findByClienteId(usuarioId);
        List<Tramite> comoRecepcionista = tramiteRepository.findByRecepcionistaId(usuarioId);
        Set<String> ids = new HashSet<>();
        List<Tramite> todos = new ArrayList<>();
        for (Tramite t : comoCliente) { if (ids.add(t.getId())) todos.add(t); }
        for (Tramite t : comoRecepcionista) { if (ids.add(t.getId())) todos.add(t); }

        // Incluir tramites donde hay una actividad activa asignada al departamento del usuario
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            if (usuario.getDepartamentoId() != null) {
                tramiteRepository.findAll().stream()
                    .filter(t -> t.getActividades() != null && t.getActividades().stream()
                        .anyMatch(a -> "activo".equals(a.getEstado()) &&
                                usuario.getDepartamentoId().equals(a.getDepartamentoId())))
                    .forEach(t -> { if (ids.add(t.getId())) todos.add(t); });
            }
        });

        return todos;
    }

    // ── Métodos privados de soporte ──────────────────────────────────────────

    private Tramite obtenerTramite(String tramiteId) {
        return tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new WorkflowException("Trámite no encontrado: " + tramiteId));
    }

    private Actividad crearActividad(Flujo flujo, String estado) {
        return Actividad.builder()
                .id(UUID.randomUUID().toString())
                .flujoId(flujo.getId())
                .departamentoId(flujo.getDepartamentoId())
                .nombre(flujo.getFormularioId())
                .estado(estado)
                .fechaInicio("activo".equals(estado) ? LocalDateTime.now() : null)
                .datosForm(DatosClienteForm.builder()
                        .formularioId(flujo.getFormularioId())
                        .estado("en_proceso")
                        .datos(new ArrayList<>())
                        .observaciones(new ArrayList<>())
                        .fecha(LocalDateTime.now())
                        .build())
                .build();
    }

    private String obtenerFormularioIdDeFlujo(String flujoId) {
        return flujoRepository.findById(flujoId).map(Flujo::getFormularioId).orElse(null);
    }

    /** Emite el estado actualizado de un trámite al canal WebSocket */
    private void emitirEstadoTramite(Tramite tramite) {
        Actividad actividadActiva = tramite.getActividades().stream()
                .filter(a -> "activo".equals(a.getEstado()))
                .findFirst().orElse(null);

        EstadoTramiteResponse respuesta = EstadoTramiteResponse.builder()
                .id(tramite.getId())
                .estado(tramite.getEstado())
                .prioridad(tramite.getPrioridad())
                .pasoActual((int) tramite.getActividades().stream().filter(a -> !"espera".equals(a.getEstado())).count())
                .actividadActualId(actividadActiva != null ? actividadActiva.getId() : null)
                .actividadActualNombre(actividadActiva != null ? actividadActiva.getNombre() : null)
                .build();

        mensajeria.convertAndSend("/topic/tramite/" + tramite.getId() + "/estado", respuesta);
    }

    /** Emite la lista de trámites activos al panel de la empresa */
    private void emitirPanelEmpresa(String empresaId) {
        if (empresaId == null) return;
        List<Tramite> activos = obtenerActivos(empresaId);
        mensajeria.convertAndSend("/topic/panel/" + empresaId, activos);
    }

    /** Crea y persiste una notificación y la envía por WebSocket */
    private void enviarNotificacion(String usuarioId, String tramiteId, String tipo, String mensaje) {
        if (usuarioId == null) return;
        Notificacion notif = Notificacion.builder()
                .usuarioId(usuarioId)
                .tramiteId(tramiteId)
                .tipo(tipo)
                .mensaje(mensaje)
                .leida(false)
                .build();
        notificacionRepository.save(notif);
        mensajeria.convertAndSend("/queue/notificaciones/" + usuarioId, notif);
    }

    /** Registra una entrada en la bitácora del trámite */
    private void registrarBitacora(String tramiteId, String usuarioId, String accion,
                                   String descripcion, String estadoAnterior, String estadoNuevo) {
        bitacoraRepository.save(Bitacora.builder()
                .tramiteId(tramiteId)
                .usuarioId(usuarioId)
                .accion(accion)
                .descripcion(descripcion)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .build());
    }
}
