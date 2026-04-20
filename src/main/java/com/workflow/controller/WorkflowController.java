package com.workflow.controller;

import com.workflow.document.Tramite;
import com.workflow.dto.AvanzarPasoRequest;
import com.workflow.dto.IniciarTramiteRequest;
import com.workflow.dto.ObservacionRequest;
import com.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping("/iniciar")
    public ResponseEntity<Tramite> iniciar(@Valid @RequestBody IniciarTramiteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                workflowService.iniciarTramite(req.getPoliticaId(), req.getClienteId(),
                        req.getRecepcionistaId(), req.getEmpresaId(), req.getPrioridad()));
    }

    @PatchMapping("/{tramiteId}/avanzar")
    public ResponseEntity<Tramite> avanzar(@PathVariable String tramiteId,
                                            @Valid @RequestBody AvanzarPasoRequest req) {
        return ResponseEntity.ok(workflowService.avanzarPaso(
                tramiteId, req.getUsuarioId(), req.getObservacion(), req.getDatosForm()));
    }

    @PostMapping("/{tramiteId}/observar")
    public ResponseEntity<Void> observar(@PathVariable String tramiteId,
                                          @Valid @RequestBody ObservacionRequest req) {
        workflowService.agregarObservacion(tramiteId, req.getActividadId(),
                req.getComponenteId(), req.getUsuarioId(), req.getDescripcion(), req.getEstado());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{tramiteId}")
    public ResponseEntity<Tramite> obtener(@PathVariable String tramiteId) {
        return ResponseEntity.ok(workflowService.obtener(tramiteId));
    }

    @GetMapping("/empresa/{empresaId}/activos")
    public ResponseEntity<List<Tramite>> activos(@PathVariable String empresaId) {
        return ResponseEntity.ok(workflowService.obtenerActivos(empresaId));
    }

    @GetMapping("/empresa/{empresaId}/urgentes")
    public ResponseEntity<List<Tramite>> urgentes(@PathVariable String empresaId) {
        return ResponseEntity.ok(workflowService.obtenerUrgentes(empresaId));
    }

    @GetMapping("/usuario/{usuarioId}/mis-tramites")
    public ResponseEntity<List<Tramite>> misTramites(@PathVariable String usuarioId) {
        return ResponseEntity.ok(workflowService.obtenerMisTramites(usuarioId));
    }

    @PatchMapping("/{tramiteId}/prioridad/{prioridad}")
    public ResponseEntity<Tramite> cambiarPrioridad(@PathVariable String tramiteId,
                                                     @PathVariable String prioridad) {
        return ResponseEntity.ok(workflowService.cambiarPrioridad(tramiteId, prioridad));
    }
}
