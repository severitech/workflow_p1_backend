package com.workflow.controller;

import com.workflow.document.Formulario;
import com.workflow.dto.FormularioRequest;
import com.workflow.service.CrudServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/formularios")
@RequiredArgsConstructor
public class FormularioController {

    private final CrudServices crudServices;

    @PostMapping
    public ResponseEntity<Formulario> crear(@Valid @RequestBody FormularioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crudServices.crearFormulario(req));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<Formulario>> listarPorEmpresa(@PathVariable String empresaId) {
        return ResponseEntity.ok(crudServices.listarFormulariosPorEmpresa(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Formulario> obtener(@PathVariable String id) {
        return ResponseEntity.ok(crudServices.obtenerFormulario(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Formulario> actualizar(@PathVariable String id, @Valid @RequestBody FormularioRequest req) {
        return ResponseEntity.ok(crudServices.actualizarFormulario(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        crudServices.eliminarFormulario(id);
        return ResponseEntity.noContent().build();
    }
}
