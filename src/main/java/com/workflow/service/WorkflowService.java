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
    private final FlujoRelacionRepository relacionRepository;
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

        // Flujos raíz = flujos sin relaciones que los apunten como hijos
        Set<String> tienenRelacionEntrante = obtenerFlujosConRelacionEntrante(politicaId);
        List<Flujo> primerosFlujosOrdenados = flujoRepository.findByPoliticaIdOrderByOrden(politicaId).stream()
                .filter(f -> !tienenRelacionEntrante.contains(f.getId()))
                .toList();

        if (primerosFlujosOrdenados.isEmpty()) {
            throw new WorkflowException("La política no tiene flujos configurados");
        }

        // El primer orden puede ser paralelo si hay varios flujos con el mismo orden mínimo
        int primerOrden = primerosFlujosOrdenados.get(0).getOrden();
        List<Flujo> primerosParalelos = primerosFlujosOrdenados.stream()
                .filter(f -> f.getOrden() == primerOrden).toList();

        List<Actividad> actividades = new ArrayList<>();
        for (Flujo f : primerosParalelos) actividades.add(crearActividad(f, "activo"));

        Tramite tramite = Tramite.builder()
                .politicaId(politicaId).clienteId(clienteId).recepcionistaId(recepcionistaId)
                .empresaId(empresaId).estado("proceso")
                .prioridad(prioridad != null ? prioridad : "normal")
                .actividades(actividades).fecha(LocalDateTime.now()).build();

        tramite = tramiteRepository.save(tramite);
        registrarBitacora(tramite.getId(), recepcionistaId, "INICIAR", "Trámite iniciado", null, "proceso");
        emitirPanelEmpresa(empresaId);
        log.debug("Trámite iniciado: {}", tramite.getId());
        return tramite;
    }

    /**
     * Completa una actividad específica y avanza el trámite.
     * En flujos paralelos, espera a que todas las actividades activas del mismo grupo se completen.
     */
    public Tramite avanzarPaso(String tramiteId, String actividadId, String usuarioId,
                                String observacion, List<Map<String, String>> datosForm) {
        Tramite tramite = obtenerTramite(tramiteId);

        // Seleccionar la actividad a completar (por ID si se especifica, sino la primera activa)
        Actividad actividadActiva = (actividadId != null)
                ? tramite.getActividades().stream()
                        .filter(a -> actividadId.equals(a.getId()) && "activo".equals(a.getEstado()))
                        .findFirst()
                        .orElseThrow(() -> new WorkflowException("Actividad activa no encontrada: " + actividadId))
                : tramite.getActividades().stream()
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
                        .valor(dato.get("valor")).build());
            }
            if (actividadActiva.getDatosForm() == null) {
                actividadActiva.setDatosForm(DatosClienteForm.builder()
                        .formularioId(obtenerFormularioIdDeFlujo(actividadActiva.getFlujoId()))
                        .estado("completado").datos(datos).fecha(LocalDateTime.now()).build());
            } else {
                actividadActiva.getDatosForm().setDatos(datos);
                actividadActiva.getDatosForm().setEstado("completado");
            }
        }

        actividadActiva.setEstado("completado");
        actividadActiva.setFechaFin(LocalDateTime.now());
        actividadActiva.setObservacion(observacion);

        // Notificar al recepcionista que el formulario fue completado
        String nombrePaso = actividadActiva.getNombre() != null ? actividadActiva.getNombre() : "un paso";
        String msgCompletado = "Formulario \"" + nombrePaso + "\" completado en el trámite " + tramiteId.substring(0, Math.min(8, tramiteId.length()));
        if (tramite.getRecepcionistaId() != null && !tramite.getRecepcionistaId().equals(usuarioId)) {
            enviarNotificacion(tramite.getRecepcionistaId(), tramiteId, actividadActiva.getId(), "FORMULARIO_COMPLETADO", msgCompletado, null);
        }

        // Verificar si aún quedan otras actividades "activo" (flujos paralelos)
        boolean hayOtrasActivas = tramite.getActividades().stream()
                .anyMatch(a -> "activo".equals(a.getEstado()));

        String estadoAnterior = tramite.getEstado();

        if (!hayOtrasActivas) {
            // Todas las actividades paralelas terminaron — determinar siguiente paso
            List<DatoForm> datosCompletos = actividadActiva.getDatosForm() != null
                    ? actividadActiva.getDatosForm().getDatos() : List.of();

            List<Flujo> siguientesFlujoss = determinarSiguientesFlujoss(
                    tramite.getPoliticaId(), actividadActiva.getFlujoId(), datosCompletos);

            // Evitar duplicar actividades ya activas (convergencia de múltiples ramas)
            Set<String> flujoIdsYaActivos = tramite.getActividades().stream()
                    .filter(a -> "activo".equals(a.getEstado()))
                    .map(Actividad::getFlujoId)
                    .collect(java.util.stream.Collectors.toSet());

            if (!siguientesFlujoss.isEmpty()) {
                for (Flujo sig : siguientesFlujoss) {
                    if (flujoIdsYaActivos.contains(sig.getId())) continue;
                    Actividad nueva = crearActividad(sig, "activo");
                    tramite.getActividades().add(nueva);
                    String nomActividad = nueva.getNombre() != null ? nueva.getNombre() : "nueva actividad";
                    String sufijo = tramiteId.substring(0, Math.min(8, tramiteId.length()));
                    if (sig.getDepartamentoId() != null) {
                        String msgAsig = "Nueva actividad asignada: \"" + nomActividad + "\" en trámite " + sufijo;
                        String rutaAsig = "/formulario-tramite/" + tramiteId + "/" + nueva.getId();
                        usuarioRepository.findByDepartamentoId(sig.getDepartamentoId())
                                .forEach(u -> enviarNotificacion(u.getId(), tramiteId, nueva.getId(), "ASIGNACION", msgAsig, rutaAsig));
                    }
                    // Notificar al cliente que su trámite avanzó al siguiente paso
                    if (tramite.getClienteId() != null) {
                        String msgCliente = "Tu trámite " + sufijo + " avanzó al paso: \"" + nomActividad + "\"";
                        enviarNotificacion(tramite.getClienteId(), tramiteId, nueva.getId(), "TRAMITE_AVANZADO", msgCliente, "/mis-tramites");
                    }
                }
            } else {
                tramite.setEstado("completado");
                tramite.setFechaFin(LocalDateTime.now());
                // Notificar al cliente y recepcionista que el trámite está completado
                String msgFin = "El trámite " + tramiteId.substring(0, Math.min(8, tramiteId.length())) + " fue completado exitosamente";
                enviarNotificacion(tramite.getClienteId(), tramiteId, "TRAMITE_COMPLETADO", msgFin, "/mis-tramites");
                if (tramite.getRecepcionistaId() != null) {
                    enviarNotificacion(tramite.getRecepcionistaId(), tramiteId, "TRAMITE_COMPLETADO", msgFin, "/panel-recepcionista");
                }
            }
        }

        tramite = tramiteRepository.save(tramite);
        registrarBitacora(tramiteId, usuarioId, "AVANZAR", "Paso avanzado", estadoAnterior, tramite.getEstado());
        emitirEstadoTramite(tramite);
        emitirPanelEmpresa(tramite.getEmpresaId());
        return tramite;
    }

    /**
     * Determina los siguientes flujos a activar soportando:
     * 1. iterativo — bucle condicional (condición met → loop, no met → continuar)
     * 2. paralelo — fork simultáneo (todos los flujos paralelos se activan a la vez)
     * 3. condicional — rama única según datos del formulario
     * 4. siguiente — convergencia explícita
     * 5. fallback — siguiente flujo raíz por orden
     */
    public List<Flujo> determinarSiguientesFlujoss(String politicaId, String flujoActualId, List<DatoForm> datosForm) {
        List<FlujoRelacion> salientes = relacionRepository.findByPadreId(flujoActualId);

        // 1. Iterativo — evaluar condición de bucle
        for (FlujoRelacion rel : salientes) {
            if ("iterativo".equals(rel.getTipo())) {
                boolean condicionMet = rel.getCondicionCampo() == null
                        || datosForm.stream().anyMatch(d ->
                                rel.getCondicionCampo().equalsIgnoreCase(d.getEtiqueta()) &&
                                rel.getCondicionValor() != null &&
                                rel.getCondicionValor().equalsIgnoreCase(d.getValor()));
                if (condicionMet) {
                    // Condición cumplida → repetir (loop back al flujo destino)
                    return flujoRepository.findById(rel.getHijoId())
                            .map(List::of).orElse(List.of());
                }
                // Condición no cumplida → salir del bucle, continuar con flujo normal
            }
        }

        // 2. Paralelo — fork: devolver TODOS los flujos hijos paralelos simultáneamente
        List<Flujo> paralelos = salientes.stream()
                .filter(r -> "paralelo".equals(r.getTipo()))
                .map(r -> flujoRepository.findById(r.getHijoId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        if (!paralelos.isEmpty()) return paralelos;

        // 3. Condicional — evaluar condiciones, priorizar coincidencia exacta
        FlujoRelacion sinCondicion = null;
        for (FlujoRelacion rel : salientes) {
            if ("condicional".equals(rel.getTipo())) {
                if (rel.getCondicionCampo() != null && rel.getCondicionValor() != null) {
                    boolean coincide = datosForm.stream().anyMatch(d ->
                            rel.getCondicionCampo().equalsIgnoreCase(d.getEtiqueta()) &&
                            rel.getCondicionValor().equalsIgnoreCase(d.getValor()));
                    if (coincide) return flujoRepository.findById(rel.getHijoId())
                            .map(List::of).orElse(List.of());
                } else if (sinCondicion == null) {
                    sinCondicion = rel;
                }
            }
        }
        if (sinCondicion != null) {
            return flujoRepository.findById(sinCondicion.getHijoId())
                    .map(List::of).orElse(List.of());
        }

        // 4. Siguiente — si hay varios del mismo padre se tratan como fork paralelo
        List<Flujo> siguientes = salientes.stream()
                .filter(r -> "siguiente".equals(r.getTipo()))
                .map(r -> flujoRepository.findById(r.getHijoId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        if (!siguientes.isEmpty()) return siguientes;

        // 5. Fallback: siguiente flujo raíz por orden
        Flujo flujoActual = flujoRepository.findById(flujoActualId).orElse(null);
        if (flujoActual == null) return List.of();

        Set<String> conRelacionEntrante = obtenerFlujosConRelacionEntrante(politicaId);
        return flujoRepository.findByPoliticaIdOrderByOrden(politicaId).stream()
                .filter(f -> !conRelacionEntrante.contains(f.getId()))
                .filter(f -> f.getOrden() > flujoActual.getOrden())
                .findFirst()
                .map(List::of).orElse(List.of());
    }

    /** @deprecated Usar determinarSiguientesFlujoss */
    public Flujo determinarSiguienteFlujo(String politicaId, String flujoActualId, List<DatoForm> datosForm) {
        List<Flujo> lista = determinarSiguientesFlujoss(politicaId, flujoActualId, datosForm);
        return lista.isEmpty() ? null : lista.get(0);
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

    public Tramite obtener(String tramiteId) { return obtenerTramite(tramiteId); }

    public List<Tramite> obtenerActivos(String empresaId) {
        return tramiteRepository.findByEmpresaIdAndEstadoIn(empresaId, List.of("proceso", "urgente", "pendiente"));
    }

    public List<Tramite> obtenerTodos(String empresaId) {
        return tramiteRepository.findByEmpresaId(empresaId);
    }

    public List<Tramite> obtenerUrgentes(String empresaId) {
        return tramiteRepository.findByEmpresaIdAndPrioridad(empresaId, "urgente");
    }

    /** Retorna trámites donde el usuario es cliente, recepcionista o tiene actividad activa en su departamento */
    public List<Tramite> obtenerMisTramites(String usuarioId) {
        List<Tramite> comoCliente = tramiteRepository.findByClienteId(usuarioId);
        List<Tramite> comoRecepcionista = tramiteRepository.findByRecepcionistaId(usuarioId);
        Set<String> ids = new HashSet<>();
        List<Tramite> todos = new ArrayList<>();
        for (Tramite t : comoCliente) { if (ids.add(t.getId())) todos.add(t); }
        for (Tramite t : comoRecepcionista) { if (ids.add(t.getId())) todos.add(t); }

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

    // ── Métodos privados ──────────────────────────────────────────────────────

    /** Retorna IDs de flujos que tienen al menos una relación entrante (no son raíz) */
    private Set<String> obtenerFlujosConRelacionEntrante(String politicaId) {
        Set<String> ids = new HashSet<>();
        relacionRepository.findByPoliticaId(politicaId).forEach(r -> ids.add(r.getHijoId()));
        return ids;
    }

    private Tramite obtenerTramite(String tramiteId) {
        return tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new WorkflowException("Trámite no encontrado: " + tramiteId));
    }

    private Actividad crearActividad(Flujo flujo, String estado) {
        return Actividad.builder()
                .id(UUID.randomUUID().toString())
                .flujoId(flujo.getId())
                .departamentoId(flujo.getDepartamentoId())
                .nombre(flujo.getNombre() != null ? flujo.getNombre() : flujo.getFormularioId())
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

    private void emitirEstadoTramite(Tramite tramite) {
        List<Actividad> activas = tramite.getActividades().stream()
                .filter(a -> "activo".equals(a.getEstado())).toList();
        Actividad actividadActiva = activas.isEmpty() ? null : activas.get(0);

        EstadoTramiteResponse respuesta = EstadoTramiteResponse.builder()
                .id(tramite.getId())
                .estado(tramite.getEstado())
                .prioridad(tramite.getPrioridad())
                .pasoActual((int) tramite.getActividades().stream().filter(a -> !"espera".equals(a.getEstado())).count())
                .actividadActualId(actividadActiva != null ? actividadActiva.getId() : null)
                .actividadActualNombre(activas.size() > 1
                        ? activas.stream().map(Actividad::getNombre).reduce((a, b) -> a + " / " + b).orElse("")
                        : (actividadActiva != null ? actividadActiva.getNombre() : null))
                .build();

        mensajeria.convertAndSend("/topic/tramite/" + tramite.getId() + "/estado", respuesta);
    }

    private void emitirPanelEmpresa(String empresaId) {
        if (empresaId == null) return;
        mensajeria.convertAndSend("/topic/panel/" + empresaId, obtenerActivos(empresaId));
    }

    private void enviarNotificacion(String usuarioId, String tramiteId, String tipo, String mensaje) {
        enviarNotificacion(usuarioId, tramiteId, null, tipo, mensaje, null);
    }

    private void enviarNotificacion(String usuarioId, String tramiteId, String tipo, String mensaje, String ruta) {
        enviarNotificacion(usuarioId, tramiteId, null, tipo, mensaje, ruta);
    }

    private void enviarNotificacion(String usuarioId, String tramiteId, String actividadId, String tipo, String mensaje, String ruta) {
        if (usuarioId == null) return;
        Notificacion notif = Notificacion.builder()
                .usuarioId(usuarioId).tramiteId(tramiteId).actividadId(actividadId)
                .tipo(tipo).mensaje(mensaje).leida(false).ruta(ruta).build();
        notificacionRepository.save(notif);
        mensajeria.convertAndSend("/queue/notificaciones/" + usuarioId, notif);
    }

    /** Notifica al recepcionista y cliente que un campo fue guardado en el formulario */
    public void notificarCampoGuardado(String tramiteId, String actividadId, String etiqueta, String usuarioQueGuardaId) {
        try {
            Tramite tramite = obtenerTramite(tramiteId);
            String mensaje = "Campo \"" + etiqueta + "\" guardado en trámite " + tramiteId.substring(0, Math.min(8, tramiteId.length()));
            // Notificar al recepcionista si es diferente al que guardó
            if (tramite.getRecepcionistaId() != null && !tramite.getRecepcionistaId().equals(usuarioQueGuardaId)) {
                enviarNotificacion(tramite.getRecepcionistaId(), tramiteId, actividadId, "CAMPO_GUARDADO", mensaje, null);
            }
            // Notificar al cliente si es diferente al que guardó
            if (tramite.getClienteId() != null && !tramite.getClienteId().equals(usuarioQueGuardaId)) {
                enviarNotificacion(tramite.getClienteId(), tramiteId, actividadId, "CAMPO_GUARDADO", mensaje, null);
            }
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación de campo guardado: {}", e.getMessage());
        }
    }

    private void registrarBitacora(String tramiteId, String usuarioId, String accion,
                                   String descripcion, String estadoAnterior, String estadoNuevo) {
        bitacoraRepository.save(Bitacora.builder()
                .tramiteId(tramiteId).usuarioId(usuarioId).accion(accion)
                .descripcion(descripcion).estadoAnterior(estadoAnterior).estadoNuevo(estadoNuevo)
                .build());
    }
}
