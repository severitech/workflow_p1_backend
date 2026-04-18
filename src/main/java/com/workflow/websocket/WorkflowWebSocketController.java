package com.workflow.websocket;

import com.workflow.document.Tramite;
import com.workflow.dto.*;
import com.workflow.service.FormularioRealtimeService;
import com.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WorkflowWebSocketController {

    private final WorkflowService workflowService;
    private final FormularioRealtimeService formularioRealtimeService;

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
        Tramite tramite = workflowService.recalcularSemaforo(mensaje.getTramiteId());
        var actividadActiva = tramite.getActividades().stream()
                .filter(a -> "activo".equals(a.getEstado())).findFirst().orElse(null);
        return EstadoTramiteResponse.builder()
                .id(tramite.getId())
                .estado(tramite.getEstado())
                .semaforo(tramite.getSemaforo())
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
}
