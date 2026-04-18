package com.workflow.controller;

import com.workflow.dto.UsuarioRequest;
import com.workflow.dto.UsuarioResponse;
import com.workflow.service.CrudServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final CrudServices crudServices;

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crudServices.crearUsuario(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable String id) {
        return ResponseEntity.ok(crudServices.obtenerUsuario(id));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<UsuarioResponse>> listarPorEmpresa(@PathVariable String empresaId) {
        return ResponseEntity.ok(crudServices.listarUsuariosPorEmpresa(empresaId));
    }

    @GetMapping("/empresa/{empresaId}/rol/{rol}")
    public ResponseEntity<List<UsuarioResponse>> listarPorEmpresaYRol(
            @PathVariable String empresaId, @PathVariable String rol) {
        return ResponseEntity.ok(crudServices.listarUsuariosPorEmpresaYRol(empresaId, rol));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable String id, @Valid @RequestBody UsuarioRequest req) {
        return ResponseEntity.ok(crudServices.actualizarUsuario(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        crudServices.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
