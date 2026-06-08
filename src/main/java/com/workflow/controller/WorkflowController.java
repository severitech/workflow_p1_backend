package com.workflow.controller;

import com.workflow.document.DatoForm;
import com.workflow.document.Tramite;
import com.workflow.dto.AvanzarPasoRequest;
import com.workflow.dto.IniciarTramiteRequest;
import com.workflow.dto.ObservacionRequest;
import com.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                tramiteId, req.getActividadId(), req.getUsuarioId(), req.getObservacion(), req.getDatosForm()));
    }

    /**
     * Sube un archivo (cualquier tipo: pdf, imagen, word, etc.) para un campo de tipo
     * "archivo" del formulario de una actividad. Se guarda en S3 dentro de la carpeta
     * "tramites/{tramiteId}/", y la clave resultante queda como valor del campo.
     */
    @PostMapping(value = "/{tramiteId}/actividad/{actividadId}/archivo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> subirArchivoCampo(
            @PathVariable String tramiteId,
            @PathVariable String actividadId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("componenteId") String componenteId,
            @RequestParam("etiqueta") String etiqueta) {
        DatoForm dato = workflowService.subirArchivoCampoFormulario(
                tramiteId, actividadId, componenteId, etiqueta, archivo);
        return ResponseEntity.ok(Map.of(
                "dato", dato,
                "urlDescarga", workflowService.generarUrlArchivo(dato.getValor())
        ));
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
    public ResponseEntity<List<Tramite>> activos(
            @PathVariable String empresaId,
            @RequestParam(defaultValue = "50") int limite) {
        List<Tramite> todos = workflowService.obtenerActivos(empresaId);
        List<Tramite> resultado = todos.stream().limit(limite).collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/empresa/{empresaId}/todos")
    public ResponseEntity<List<Tramite>> todos(@PathVariable String empresaId) {
        return ResponseEntity.ok(workflowService.obtenerTodos(empresaId));
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
