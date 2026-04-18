package com.workflow.controller;

import com.workflow.document.Flujo;
import com.workflow.dto.FlujoRequest;
import com.workflow.service.CrudServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flujos")
@RequiredArgsConstructor
public class FlujoController {

    private final CrudServices crudServices;

    @PostMapping
    public ResponseEntity<Flujo> crear(@Valid @RequestBody FlujoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crudServices.crearFlujo(req));
    }

    @GetMapping("/politica/{politicaId}")
    public ResponseEntity<List<Flujo>> listarPorPolitica(@PathVariable String politicaId) {
        return ResponseEntity.ok(crudServices.listarFlujosPorPolitica(politicaId));
    }

    @GetMapping("/politica/{politicaId}/raiz")
    public ResponseEntity<List<Flujo>> listarRaiz(@PathVariable String politicaId) {
        return ResponseEntity.ok(crudServices.listarFlujoRaiz(politicaId));
    }

    @GetMapping("/{id}/hijos")
    public ResponseEntity<List<Flujo>> listarHijos(@PathVariable String id) {
        return ResponseEntity.ok(crudServices.listarHijos(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Flujo> actualizar(@PathVariable String id, @Valid @RequestBody FlujoRequest req) {
        return ResponseEntity.ok(crudServices.actualizarFlujo(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        crudServices.eliminarFlujo(id);
        return ResponseEntity.noContent().build();
    }
}
