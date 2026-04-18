package com.workflow.controller;

import com.workflow.document.PoliticaNegocio;
import com.workflow.dto.PoliticaRequest;
import com.workflow.service.CrudServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/politicas")
@RequiredArgsConstructor
public class PoliticaController {

    private final CrudServices crudServices;

    @PostMapping
    public ResponseEntity<PoliticaNegocio> crear(@Valid @RequestBody PoliticaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crudServices.crearPolitica(req));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<PoliticaNegocio>> listarPorEmpresa(@PathVariable String empresaId) {
        return ResponseEntity.ok(crudServices.listarPoliticasPorEmpresa(empresaId));
    }

    @GetMapping("/empresa/{empresaId}/activas")
    public ResponseEntity<List<PoliticaNegocio>> listarActivas(@PathVariable String empresaId) {
        return ResponseEntity.ok(crudServices.listarPoliticasActivas(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PoliticaNegocio> obtener(@PathVariable String id) {
        return ResponseEntity.ok(crudServices.obtenerPolitica(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PoliticaNegocio> actualizar(@PathVariable String id, @Valid @RequestBody PoliticaRequest req) {
        return ResponseEntity.ok(crudServices.actualizarPolitica(id, req));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<PoliticaNegocio> activar(@PathVariable String id) {
        return ResponseEntity.ok(crudServices.activarPolitica(id));
    }

    @PostMapping("/{id}/nueva-version")
    public ResponseEntity<PoliticaNegocio> nuevaVersion(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crudServices.nuevaVersionPolitica(id));
    }
}
