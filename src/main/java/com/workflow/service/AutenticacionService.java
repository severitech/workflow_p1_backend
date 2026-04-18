package com.workflow.service;

import com.workflow.document.Rol;
import com.workflow.document.Usuario;
import com.workflow.dto.LoginRequest;
import com.workflow.dto.LoginResponse;
import com.workflow.exception.WorkflowException;
import com.workflow.repository.RolRepository;
import com.workflow.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Gestiona el inicio y cierre de sesión de usuarios */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutenticacionService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder encoderPassword;

    /** Mapa en memoria: token → usuarioId */
    private final Map<String, String> sesionesActivas = new ConcurrentHashMap<>();

    /** Valida credenciales, genera token y retorna datos del usuario */
    public LoginResponse iniciarSesion(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByCorreo(req.getCorreo())
                .orElseThrow(() -> new WorkflowException("Correo o contraseña incorrectos"));

        if (!usuario.isActivo()) {
            throw new WorkflowException("La cuenta está desactivada");
        }

        if (!encoderPassword.matches(req.getPassword(), usuario.getPassword())) {
            throw new WorkflowException("Correo o contraseña incorrectos");
        }

        String nombreRol = rolRepository.findById(usuario.getRolId())
                .map(Rol::getNombre)
                .orElse("sin_rol");

        String token = UUID.randomUUID().toString();
        sesionesActivas.put(token, usuario.getId());

        log.info("[Autenticacion] Sesión iniciada — usuario={} rol={}", usuario.getCorreo(), nombreRol);

        return LoginResponse.builder()
                .token(token)
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correo(usuario.getCorreo())
                .rolId(usuario.getRolId())
                .nombreRol(nombreRol)
                .empresaId(usuario.getEmpresaId())
                .departamentoId(usuario.getDepartamentoId())
                .build();
    }

    /** Invalida el token y cierra la sesión del usuario */
    public void cerrarSesion(String token) {
        String usuarioId = sesionesActivas.remove(token);
        if (usuarioId == null) {
            throw new WorkflowException("Token inválido o sesión ya cerrada");
        }
        log.info("[Autenticacion] Sesión cerrada — usuarioId={}", usuarioId);
    }

    /** Verifica si un token está activo y retorna el usuarioId asociado */
    public String obtenerUsuarioPorToken(String token) {
        String usuarioId = sesionesActivas.get(token);
        if (usuarioId == null) {
            throw new WorkflowException("Sesión no válida o expirada");
        }
        return usuarioId;
    }

    /** Indica si el token existe en las sesiones activas */
    public boolean esSesionValida(String token) {
        return sesionesActivas.containsKey(token);
    }
}
