package com.workflow.controller;

import com.workflow.document.Notificacion;
import com.workflow.service.CrudServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final CrudServices crudServices;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Notificacion>> listar(@PathVariable String usuarioId) {
        return ResponseEntity.ok(crudServices.listarNotificaciones(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/pendientes")
    public ResponseEntity<List<Notificacion>> pendientes(@PathVariable String usuarioId) {
        return ResponseEntity.ok(crudServices.listarPendientes(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/contador")
    public ResponseEntity<Map<String, Long>> contador(@PathVariable String usuarioId) {
        return ResponseEntity.ok(Map.of("pendientes", crudServices.contarPendientes(usuarioId)));
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<Notificacion> marcarLeida(@PathVariable String id) {
        return ResponseEntity.ok(crudServices.marcarLeida(id));
    }

    @PatchMapping("/usuario/{usuarioId}/leer-todas")
    public ResponseEntity<Void> marcarTodasLeidas(@PathVariable String usuarioId) {
        crudServices.marcarTodasLeidas(usuarioId);
        return ResponseEntity.ok().build();
    }
}
