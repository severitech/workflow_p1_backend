package com.workflow.controller;

import com.workflow.document.Tramite;
import com.workflow.repository.TramiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Expone datos históricos de trámites para el entrenamiento de modelos ML
 * en el ia_service (Feature 5 — control inteligente con ML/TensorFlow).
 */
@RestController
@RequestMapping("/ml")
@RequiredArgsConstructor
public class MlControlador {

    private final TramiteRepository tramiteRepositorio;

    /**
     * Devuelve trámites completados con sus métricas para entrenamiento.
     * Solo incluye trámites en estado "completado" o "cancelado" (histórico).
     */
    @GetMapping("/datos-entrenamiento/{empresaId}")
    public ResponseEntity<List<Map<String, Object>>> datosEntrenamiento(@PathVariable String empresaId) {
        List<Tramite> tramites = tramiteRepositorio.findByEmpresaIdAndEstadoIn(
            empresaId, List.of("completado", "cancelado")
        );

        List<Map<String, Object>> datos = tramites.stream().map(t -> {
            var actividades = t.getActividades();
            long rechazadas = actividades.stream()
                .filter(a -> "rechazado".equals(a.getEstado())).count();
            long completadas = actividades.stream()
                .filter(a -> "completado".equals(a.getEstado())).count();

            return Map.<String, Object>of(
                "tramiteId", t.getId(),
                "politicaId", t.getPoliticaId(),
                "estado", t.getEstado(),
                "semaforoFinal", "urgente".equals(t.getEstado()) ? "rojo"
                               : "urgente".equals(t.getPrioridad()) ? "amarillo" : "verde",
                "totalActividades", actividades.size(),
                "actividadesCompletadas", completadas,
                "actividadesRechazadas", rechazadas,
                "actividades", actividades
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(datos);
    }

    /** Endpoint que devuelve un trámite por ID (usado por el ia_service para predecir) */
    @GetMapping("/tramite/{tramiteId}")
    public ResponseEntity<Tramite> obtenerTramite(@PathVariable String tramiteId) {
        return tramiteRepositorio.findById(tramiteId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
