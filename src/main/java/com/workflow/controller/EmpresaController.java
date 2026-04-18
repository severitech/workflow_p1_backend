package com.workflow.controller;

import com.workflow.document.Empresa;
import com.workflow.dto.EmpresaRequest;
import com.workflow.service.CrudServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final CrudServices crudServices;

    @PostMapping
    public ResponseEntity<Empresa> crear(@Valid @RequestBody EmpresaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crudServices.crearEmpresa(req));
    }

    @GetMapping
    public ResponseEntity<List<Empresa>> listar() {
        return ResponseEntity.ok(crudServices.listarEmpresas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> obtener(@PathVariable String id) {
        return ResponseEntity.ok(crudServices.obtenerEmpresa(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empresa> actualizar(@PathVariable String id, @Valid @RequestBody EmpresaRequest req) {
        return ResponseEntity.ok(crudServices.actualizarEmpresa(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        crudServices.eliminarEmpresa(id);
        return ResponseEntity.noContent().build();
    }
}
