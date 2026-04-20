package com.workflow.websocket;

import com.workflow.document.Tramite;
import com.workflow.dto.*;
import com.workflow.service.FormularioRealtimeService;
import com.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WorkflowWebSocketController {

    private final WorkflowService workflowService;
    private final FormularioRealtimeService formularioRealtimeService;
    private final SimpMessagingTemplate mensajeria;

    /** Autoguardado campo a campo del formulario */
    @MessageMapping("/formulario/campo")
    public void guardarCampo(CampoFormularioMessage mensaje) {
        formularioRealtimeService.procesarCampo(
                mensaje.getTramiteId(), mensaje.getActividadId(),
                mensaje.getComponenteId(), mensaje.getEtiqueta(),
                mensaje.getValor(), mensaje.getUsuarioId()
        );
    }

    /** Consulta y emite el estado actual de un trámite */
    @MessageMapping("/tramite/estado")
    @SendTo("/topic/tramite/{tramiteId}/estado")
    public EstadoTramiteResponse consultarEstado(EstadoTramiteMessage mensaje) {
        Tramite tramite = workflowService.obtener(mensaje.getTramiteId());
        var actividadActiva = tramite.getActividades().stream()
                .filter(a -> "activo".equals(a.getEstado())).findFirst().orElse(null);
        return EstadoTramiteResponse.builder()
                .id(tramite.getId())
                .estado(tramite.getEstado())
                .prioridad(tramite.getPrioridad())
                .pasoActual((int) tramite.getActividades().stream()
                        .filter(a -> !"espera".equals(a.getEstado())).count())
                .actividadActualId(actividadActiva != null ? actividadActiva.getId() : null)
                .actividadActualNombre(actividadActiva != null ? actividadActiva.getNombre() : null)
                .build();
    }

    /** Suscripción al panel de recepcionista — emite lista de trámites activos de la empresa */
    @MessageMapping("/panel/suscribir")
    @SendTo("/topic/panel/{empresaId}")
    public List<Tramite> suscribirPanel(PanelSuscribirMessage mensaje) {
        return workflowService.obtenerActivos(mensaje.getEmpresaId());
    }

    /** Gestión de presencia en formulario colaborativo */
    @MessageMapping("/formulario/presencia")
    public void gestionarPresencia(PresenciaMessage mensaje) {
        formularioRealtimeService.procesarPresencia(mensaje);
    }

    /**
     * Broadcast de cursor en tiempo real para el editor de flujos.
     * Retransmite la posición + acción a todos los suscriptores del mismo politicaId.
     */
    @MessageMapping("/editor/cursor")
    public void cursorEditor(CursorEditorDto dto) {
        mensajeria.convertAndSend(
            "/topic/editor/" + dto.getPoliticaId() + "/cursores", dto
        );
    }

    /**
     * Broadcast de cursor dentro del editor de componentes de un formulario.
     * Retransmite a todos los que tienen abierto ese formulario.
     */
    @MessageMapping("/editor/cursor-formulario")
    public void cursorFormulario(CursorEditorDto dto) {
        mensajeria.convertAndSend(
            "/topic/editor/" + dto.getFormularioId() + "/cursores-form", dto
        );
    }

    /**
     * Broadcast de cambios estructurales del editor (mover nodo, conectar, desconectar).
     * Todos los editores del mismo politicaId reciben el cambio en tiempo real.
     */
    @MessageMapping("/editor/cambio")
    public void cambioEditor(EditorCambioDto dto) {
        mensajeria.convertAndSend(
            "/topic/editor/" + dto.getPoliticaId() + "/cambios", dto
        );
    }

    /**
     * Broadcast de cambios en componentes de un formulario específico.
     * Solo los usuarios con ese formulario abierto reciben el cambio.
     */
    @MessageMapping("/editor/cambio-formulario")
    public void cambioFormulario(EditorCambioDto dto) {
        mensajeria.convertAndSend(
            "/topic/editor/" + dto.getFlujoId() + "/cambios-form", dto
        );
    }
}
