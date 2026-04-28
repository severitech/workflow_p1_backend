package com.workflow.service;

import com.workflow.document.DatoForm;
import com.workflow.dto.PresenciaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormularioRealtimeService {

    private final WorkflowService workflowService;
    private final SimpMessagingTemplate mensajeria;

    /** Mapa de presencia: clave = tramiteId:actividadId → lista de usuarioIds activos */
    private final Map<String, List<String>> presencias = new ConcurrentHashMap<>();

    /** Guarda el campo en MongoDB, emite el dato actualizado y notifica a los involucrados */
    public void procesarCampo(String tramiteId, String actividadId,
                               String componenteId, String etiqueta, String valor, String usuarioId) {
        DatoForm datoActualizado = workflowService.guardarCampoFormulario(
                tramiteId, actividadId, componenteId, etiqueta, valor);

        mensajeria.convertAndSend(
                "/topic/tramite/" + tramiteId + "/formulario",
                datoActualizado
        );

        log.debug("Campo guardado - trámite: {}, componente: {}", tramiteId, componenteId);
    }

    /** Actualiza la lista de usuarios presentes en un formulario y la emite al canal */
    public void procesarPresencia(PresenciaMessage mensaje) {
        String clave = mensaje.getTramiteId() + ":" + mensaje.getActividadId();
        List<String> usuariosActivos = presencias.computeIfAbsent(clave, k -> new ArrayList<>());

        switch (mensaje.getAccion()) {
            case "unirse" -> {
                if (!usuariosActivos.contains(mensaje.getUsuarioId())) {
                    usuariosActivos.add(mensaje.getUsuarioId());
                }
            }
            case "salir" -> usuariosActivos.remove(mensaje.getUsuarioId());
            case "escribiendo" -> {
                // Solo emite el evento sin modificar la lista de presencia
            }
        }

        mensajeria.convertAndSend(
                "/topic/tramite/" + mensaje.getTramiteId() + "/presencia",
                Map.of("usuarios", usuariosActivos, "accion", mensaje.getAccion(),
                       "usuarioId", mensaje.getUsuarioId())
        );
    }
}
