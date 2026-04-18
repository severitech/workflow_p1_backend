package com.workflow.controller;

import com.workflow.dto.CerrarSesionRequest;
import com.workflow.dto.LoginRequest;
import com.workflow.dto.LoginResponse;
import com.workflow.service.AutenticacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/autenticacion")
@RequiredArgsConstructor
public class AutenticacionController {

    private final AutenticacionService autenticacionService;

    /**
     * POST /api/autenticacion/iniciar-sesion
     * Body: { "correo": "...", "password": "..." }
     * Retorna token + datos del usuario
     */
    @PostMapping("/iniciar-sesion")
    public ResponseEntity<LoginResponse> iniciarSesion(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(autenticacionService.iniciarSesion(req));
    }

    /**
     * POST /api/autenticacion/cerrar-sesion
     * Body: { "token": "..." }
     * Invalida la sesión activa del usuario
     */
    @PostMapping("/cerrar-sesion")
    public ResponseEntity<Map<String, String>> cerrarSesion(@Valid @RequestBody CerrarSesionRequest req) {
        autenticacionService.cerrarSesion(req.getToken());
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
    }

    /**
     * GET /api/autenticacion/verificar?token=...
     * Verifica si una sesión es válida (útil para el frontend)
     */
    @GetMapping("/verificar")
    public ResponseEntity<Map<String, Object>> verificarSesion(@RequestParam String token) {
        boolean valida = autenticacionService.esSesionValida(token);
        return ResponseEntity.ok(Map.of(
                "valida", valida,
                "mensaje", valida ? "Sesión activa" : "Sesión inválida o expirada"
        ));
    }
}
