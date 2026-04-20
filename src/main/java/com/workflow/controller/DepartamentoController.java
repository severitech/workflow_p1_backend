package com.workflow.controller;

import com.workflow.document.Departamento;
import com.workflow.dto.DepartamentoRequest;
import com.workflow.exception.WorkflowException;
import com.workflow.repository.DepartamentoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departamentos")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoRepository departamentoRepository;

    /** Crea un nuevo departamento */
    @PostMapping
    public ResponseEntity<Departamento> crear(@Valid @RequestBody DepartamentoRequest req) {
        Departamento dept = Departamento.builder()
                .nombre(req.getNombre())
                .descripcion(req.getDescripcion())
                .empresaId(req.getEmpresaId())
                .activo(true)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(departamentoRepository.save(dept));
    }

    /** Lista departamentos activos de una empresa */
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<Departamento>> listarPorEmpresa(@PathVariable String empresaId) {
        return ResponseEntity.ok(departamentoRepository.findByEmpresaIdAndActivoTrue(empresaId));
    }

    /** Lista todos los departamentos de una empresa (incluye inactivos) */
    @GetMapping("/empresa/{empresaId}/todos")
    public ResponseEntity<List<Departamento>> listarTodosPorEmpresa(@PathVariable String empresaId) {
        return ResponseEntity.ok(departamentoRepository.findByEmpresaId(empresaId));
    }

    /** Obtiene un departamento por ID */
    @GetMapping("/{id}")
    public ResponseEntity<Departamento> obtener(@PathVariable String id) {
        return ResponseEntity.ok(departamentoRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Departamento no encontrado: " + id)));
    }

    /** Actualiza un departamento */
    @PutMapping("/{id}")
    public ResponseEntity<Departamento> actualizar(@PathVariable String id,
                                                    @Valid @RequestBody DepartamentoRequest req) {
        Departamento dept = departamentoRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Departamento no encontrado: " + id));
        dept.setNombre(req.getNombre());
        dept.setDescripcion(req.getDescripcion());
        return ResponseEntity.ok(departamentoRepository.save(dept));
    }

    /** Activa o desactiva un departamento */
    @PatchMapping("/{id}/activo")
    public ResponseEntity<Departamento> toggleActivo(@PathVariable String id) {
        Departamento dept = departamentoRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Departamento no encontrado: " + id));
        dept.setActivo(!dept.isActivo());
        return ResponseEntity.ok(departamentoRepository.save(dept));
    }

    /** Elimina un departamento */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        departamentoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
