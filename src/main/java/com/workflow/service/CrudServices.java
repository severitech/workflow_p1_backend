package com.workflow.service;

import com.workflow.document.*;
import com.workflow.dto.*;
import com.workflow.exception.WorkflowException;
import com.workflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CrudServices {

    private final EmpresaRepository empresaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final FormularioRepository formularioRepository;
    private final PoliticaNegocioRepository politicaRepository;
    private final FlujoRepository flujoRepository;
    private final NotificacionRepository notificacionRepository;
    private final PasswordEncoder encoderPassword;

    // ── Empresas ─────────────────────────────────────────────────────────────

    /** Crea una empresa nueva */
    public Empresa crearEmpresa(EmpresaRequest req) {
        Empresa empresa = Empresa.builder()
                .nombre(req.getNombre()).nit(req.getNit()).direccion(req.getDireccion())
                .telefono(req.getTelefono()).correo(req.getCorreo()).activo(true).build();
        return empresaRepository.save(empresa);
    }

    public List<Empresa> listarEmpresas() { return empresaRepository.findAll(); }

    public Empresa obtenerEmpresa(String id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Empresa no encontrada: " + id));
    }

    /** Actualiza una empresa existente */
    public Empresa actualizarEmpresa(String id, EmpresaRequest req) {
        Empresa empresa = obtenerEmpresa(id);
        empresa.setNombre(req.getNombre()); empresa.setNit(req.getNit());
        empresa.setDireccion(req.getDireccion()); empresa.setTelefono(req.getTelefono());
        empresa.setCorreo(req.getCorreo());
        return empresaRepository.save(empresa);
    }

    public void eliminarEmpresa(String id) { empresaRepository.deleteById(id); }

    // ── Usuarios ─────────────────────────────────────────────────────────────

    /** Crea un usuario hasheando su contraseña */
    public UsuarioResponse crearUsuario(UsuarioRequest req) {
        if (usuarioRepository.findByCorreo(req.getCorreo()).isPresent()) {
            throw new WorkflowException("Ya existe un usuario con ese correo");
        }
        Usuario usuario = Usuario.builder()
                .nombre(req.getNombre()).apellido(req.getApellido()).correo(req.getCorreo())
                .password(encoderPassword.encode(req.getPassword()))
                .rolId(req.getRolId()).empresaId(req.getEmpresaId())
                .departamentoId(req.getDepartamentoId()).activo(true).build();
        return mapearUsuario(usuarioRepository.save(usuario));
    }

    public UsuarioResponse obtenerUsuario(String id) {
        return mapearUsuario(usuarioRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Usuario no encontrado: " + id)));
    }

    public List<UsuarioResponse> listarUsuariosPorEmpresa(String empresaId) {
        return usuarioRepository.findByEmpresaId(empresaId).stream().map(this::mapearUsuario).toList();
    }

    /** Busca usuarios de una empresa filtrando por nombre de rol */
    public List<UsuarioResponse> listarUsuariosPorEmpresaYRol(String empresaId, String nombreRol) {
        String rolId = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new WorkflowException("Rol no encontrado: " + nombreRol)).getId();
        return usuarioRepository.findByEmpresaIdAndRolId(empresaId, rolId)
                .stream().map(this::mapearUsuario).toList();
    }

    /** Actualiza usuario sin modificar la contraseña */
    public UsuarioResponse actualizarUsuario(String id, UsuarioRequest req) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Usuario no encontrado: " + id));
        usuario.setNombre(req.getNombre()); usuario.setApellido(req.getApellido());
        usuario.setRolId(req.getRolId()); usuario.setDepartamentoId(req.getDepartamentoId());
        return mapearUsuario(usuarioRepository.save(usuario));
    }

    public void eliminarUsuario(String id) { usuarioRepository.deleteById(id); }

    // ── Formularios ───────────────────────────────────────────────────────────

    /** Crea un formulario con sus componentes embebidos */
    public Formulario crearFormulario(FormularioRequest req) {
        Formulario form = Formulario.builder()
                .nombre(req.getNombre()).descripcion(req.getDescripcion())
                .empresaId(req.getEmpresaId()).componentes(req.getComponentes()).activo(true).build();
        return formularioRepository.save(form);
    }

    public List<Formulario> listarFormulariosPorEmpresa(String empresaId) {
        return formularioRepository.findByEmpresaId(empresaId);
    }

    public Formulario obtenerFormulario(String id) {
        return formularioRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Formulario no encontrado: " + id));
    }

    public Formulario actualizarFormulario(String id, FormularioRequest req) {
        Formulario form = obtenerFormulario(id);
        form.setNombre(req.getNombre()); form.setDescripcion(req.getDescripcion());
        form.setComponentes(req.getComponentes());
        return formularioRepository.save(form);
    }

    public void eliminarFormulario(String id) { formularioRepository.deleteById(id); }

    // ── Políticas ─────────────────────────────────────────────────────────────

    /** Crea una política en estado borrador */
    public PoliticaNegocio crearPolitica(PoliticaRequest req) {
        PoliticaNegocio politica = PoliticaNegocio.builder()
                .nombre(req.getNombre()).descripcion(req.getDescripcion()).tipo(req.getTipo())
                .estado("borrador").version(1).empresaId(req.getEmpresaId())
                .creadoPorId(req.getCreadoPorId()).build();
        return politicaRepository.save(politica);
    }

    public List<PoliticaNegocio> listarPoliticasPorEmpresa(String empresaId) {
        return politicaRepository.findByEmpresaId(empresaId);
    }

    public List<PoliticaNegocio> listarPoliticasActivas(String empresaId) {
        return politicaRepository.findByEmpresaIdAndEstado(empresaId, "activa");
    }

    public PoliticaNegocio obtenerPolitica(String id) {
        return politicaRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Política no encontrada: " + id));
    }

    /** Actualiza una política solo si está en estado borrador */
    public PoliticaNegocio actualizarPolitica(String id, PoliticaRequest req) {
        PoliticaNegocio politica = obtenerPolitica(id);
        if (!"borrador".equals(politica.getEstado())) {
            throw new WorkflowException("Solo se pueden editar políticas en estado borrador");
        }
        politica.setNombre(req.getNombre()); politica.setDescripcion(req.getDescripcion());
        politica.setTipo(req.getTipo());
        return politicaRepository.save(politica);
    }

    /** Cambia el estado de una política a activa */
    public PoliticaNegocio activarPolitica(String id) {
        PoliticaNegocio politica = obtenerPolitica(id);
        politica.setEstado("activa");
        return politicaRepository.save(politica);
    }

    /** Clona una política y sus flujos como nueva versión */
    public PoliticaNegocio nuevaVersionPolitica(String id) {
        PoliticaNegocio original = obtenerPolitica(id);
        PoliticaNegocio nueva = PoliticaNegocio.builder()
                .nombre(original.getNombre()).descripcion(original.getDescripcion())
                .tipo(original.getTipo()).estado("borrador")
                .version(original.getVersion() + 1)
                .empresaId(original.getEmpresaId()).creadoPorId(original.getCreadoPorId()).build();
        nueva = politicaRepository.save(nueva);

        // Clonar flujos
        final String nuevaPoliticaId = nueva.getId();
        List<Flujo> flujos = flujoRepository.findByPoliticaIdOrderByOrden(id);
        for (Flujo flujo : flujos) {
            flujoRepository.save(Flujo.builder()
                    .politicaId(nuevaPoliticaId)
                    .formularioId(flujo.getFormularioId()).orden(flujo.getOrden())
                    .esObligatorio(flujo.isEsObligatorio()).flujoPadreId(flujo.getFlujoPadreId())
                    .condicionCampo(flujo.getCondicionCampo()).condicionValor(flujo.getCondicionValor())
                    .departamentoId(flujo.getDepartamentoId())
                    .build());
        }
        return nueva;
    }

    // ── Flujos ────────────────────────────────────────────────────────────────

    public Flujo crearFlujo(FlujoRequest req) {
        Flujo flujo = Flujo.builder()
                .politicaId(req.getPoliticaId())
                .formularioId(req.getFormularioId()).orden(req.getOrden())
                .esObligatorio(req.isEsObligatorio()).flujoPadreId(req.getFlujoPadreId())
                .condicionCampo(req.getCondicionCampo()).condicionValor(req.getCondicionValor())
                .departamentoId(req.getDepartamentoId())
                .build();
        return flujoRepository.save(flujo);
    }

    public List<Flujo> listarFlujosPorPolitica(String politicaId) {
        return flujoRepository.findByPoliticaIdOrderByOrden(politicaId);
    }

    public List<Flujo> listarFlujoRaiz(String politicaId) {
        return flujoRepository.findByPoliticaIdAndFlujoPadreIdIsNullOrderByOrden(politicaId);
    }

    public List<Flujo> listarHijos(String flujoId) {
        return flujoRepository.findByFlujoPadreId(flujoId);
    }

    public Flujo actualizarFlujo(String id, FlujoRequest req) {
        Flujo flujo = flujoRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Flujo no encontrado: " + id));
        flujo.setFormularioId(req.getFormularioId());
        flujo.setOrden(req.getOrden()); flujo.setEsObligatorio(req.isEsObligatorio());
        flujo.setFlujoPadreId(req.getFlujoPadreId()); flujo.setCondicionCampo(req.getCondicionCampo());
        flujo.setCondicionValor(req.getCondicionValor()); flujo.setDepartamentoId(req.getDepartamentoId());
        if (req.getPosicionX() != null) flujo.setPosicionX(req.getPosicionX());
        if (req.getPosicionY() != null) flujo.setPosicionY(req.getPosicionY());
        return flujoRepository.save(flujo);
    }

    /** Elimina el flujo y todos sus flujos hijos */
    public void eliminarFlujo(String id) {
        flujoRepository.findByFlujoPadreId(id).forEach(hijo -> flujoRepository.deleteById(hijo.getId()));
        flujoRepository.deleteById(id);
    }

    // ── Notificaciones ────────────────────────────────────────────────────────

    public List<Notificacion> listarNotificaciones(String usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    public List<Notificacion> listarPendientes(String usuarioId) {
        return notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaDesc(usuarioId);
    }

    public long contarPendientes(String usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    /** Marca una notificación como leída */
    public Notificacion marcarLeida(String id) {
        Notificacion notif = notificacionRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Notificación no encontrada: " + id));
        notif.setLeida(true);
        return notificacionRepository.save(notif);
    }

    /** Marca todas las notificaciones de un usuario como leídas */
    public void marcarTodasLeidas(String usuarioId) {
        List<Notificacion> pendientes = notificacionRepository
                .findByUsuarioIdAndLeidaFalseOrderByFechaDesc(usuarioId);
        pendientes.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(pendientes);
    }

    // ── Util ──────────────────────────────────────────────────────────────────

    private UsuarioResponse mapearUsuario(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId()).nombre(u.getNombre()).apellido(u.getApellido())
                .correo(u.getCorreo()).rolId(u.getRolId()).empresaId(u.getEmpresaId())
                .departamentoId(u.getDepartamentoId()).activo(u.isActivo()).build();
    }
}
