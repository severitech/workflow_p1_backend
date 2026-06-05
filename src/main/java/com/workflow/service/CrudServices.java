package com.workflow.service;

import com.workflow.document.*;
import com.workflow.dto.*;
import com.workflow.exception.WorkflowException;
import com.workflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private final FlujoRelacionRepository relacionRepository;
    private final NotificacionRepository notificacionRepository;
    private final PasswordEncoder encoderPassword;
    private final SimpMessagingTemplate mensajeria;

    // ── Empresas ─────────────────────────────────────────────────────────────

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

    public UsuarioResponse actualizarUsuario(String id, UsuarioRequest req) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Usuario no encontrado: " + id));
        usuario.setNombre(req.getNombre()); usuario.setApellido(req.getApellido());
        usuario.setRolId(req.getRolId()); usuario.setDepartamentoId(req.getDepartamentoId());
        return mapearUsuario(usuarioRepository.save(usuario));
    }

    public void eliminarUsuario(String id) { usuarioRepository.deleteById(id); }

    // ── Formularios ───────────────────────────────────────────────────────────

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

    public PoliticaNegocio crearPolitica(PoliticaRequest req) {
        PoliticaNegocio politica = PoliticaNegocio.builder()
                .nombre(req.getNombre()).descripcion(req.getDescripcion())
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
        return politicaRepository.save(politica);
    }

    public PoliticaNegocio activarPolitica(String id) {
        PoliticaNegocio politica = obtenerPolitica(id);
        politica.setEstado("activa");
        return politicaRepository.save(politica);
    }

    /**
     * Clona una política y sus flujos + relaciones como nueva versión.
     * Construye un mapa de IDs viejos→nuevos para reasignar relaciones correctamente.
     */
    public PoliticaNegocio nuevaVersionPolitica(String id) {
        PoliticaNegocio original = obtenerPolitica(id);
        PoliticaNegocio nueva = PoliticaNegocio.builder()
                .nombre(original.getNombre()).descripcion(original.getDescripcion())
                .estado("borrador").version(original.getVersion() + 1)
                .empresaId(original.getEmpresaId()).creadoPorId(original.getCreadoPorId()).build();
        nueva = politicaRepository.save(nueva);

        final String nuevaPoliticaId = nueva.getId();

        // Clonar flujos y construir mapa oldId -> newId
        List<Flujo> flujos = flujoRepository.findByPoliticaIdOrderByOrden(id);
        Map<String, String> mapaIds = new HashMap<>();
        for (Flujo flujo : flujos) {
            Flujo clonado = flujoRepository.save(Flujo.builder()
                    .politicaId(nuevaPoliticaId)
                    .nombre(flujo.getNombre())
                    .formularioId(flujo.getFormularioId()).orden(flujo.getOrden())
                    .esObligatorio(flujo.isEsObligatorio()).departamentoId(flujo.getDepartamentoId())
                    .tipoNodo(flujo.getTipoNodo() != null ? flujo.getTipoNodo() : "tarea")
                    .build());
            mapaIds.put(flujo.getId(), clonado.getId());
        }

        // Clonar relaciones remapeando padreId e hijoId
        for (FlujoRelacion rel : relacionRepository.findByPoliticaId(id)) {
            String nuevoPadre = mapaIds.get(rel.getPadreId());
            String nuevoHijo  = mapaIds.get(rel.getHijoId());
            if (nuevoPadre != null && nuevoHijo != null) {
                relacionRepository.save(FlujoRelacion.builder()
                        .politicaId(nuevaPoliticaId)
                        .padreId(nuevoPadre).hijoId(nuevoHijo)
                        .condicionCampo(rel.getCondicionCampo()).condicionValor(rel.getCondicionValor())
                        .tipo(rel.getTipo()).build());
            }
        }

        return nueva;
    }

    // ── Flujos ────────────────────────────────────────────────────────────────

    public Flujo crearFlujo(FlujoRequest req) {
        Flujo flujo = Flujo.builder()
                .politicaId(req.getPoliticaId())
                .nombre(req.getNombre())
                .formularioId(req.getFormularioId()).orden(req.getOrden())
                .esObligatorio(req.isEsObligatorio()).departamentoId(req.getDepartamentoId())
                .tipoNodo(req.getTipoNodo() != null ? req.getTipoNodo() : "tarea")
                .build();
        return flujoRepository.save(flujo);
    }

    public List<Flujo> listarFlujosPorPolitica(String politicaId) {
        return flujoRepository.findByPoliticaIdOrderByOrden(politicaId);
    }

    /** Flujos raíz = flujos sin ninguna relación entrante */
    public List<Flujo> listarFlujoRaiz(String politicaId) {
        Set<String> hijoIds = new HashSet<>();
        relacionRepository.findByPoliticaId(politicaId).forEach(r -> hijoIds.add(r.getHijoId()));
        return flujoRepository.findByPoliticaIdOrderByOrden(politicaId).stream()
                .filter(f -> !hijoIds.contains(f.getId())).toList();
    }

    /** Hijos de un flujo = flujos apuntados por cualquier relación saliente de ese padre */
    public List<Flujo> listarHijos(String padreId) {
        return relacionRepository.findByPadreId(padreId).stream()
                .map(r -> flujoRepository.findById(r.getHijoId()).orElse(null))
                .filter(Objects::nonNull).toList();
    }

    public Flujo actualizarFlujo(String id, FlujoRequest req) {
        Flujo flujo = flujoRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Flujo no encontrado: " + id));
        if (req.getNombre() != null) flujo.setNombre(req.getNombre());
        flujo.setFormularioId(req.getFormularioId());
        flujo.setOrden(req.getOrden()); flujo.setEsObligatorio(req.isEsObligatorio());
        flujo.setDepartamentoId(req.getDepartamentoId());
        if (req.getTipoNodo() != null) flujo.setTipoNodo(req.getTipoNodo());
        return flujoRepository.save(flujo);
    }

    /** Elimina el flujo y todas sus relaciones (como padre o como hijo) */
    public void eliminarFlujo(String id) {
        List<FlujoRelacion> relacionadas = relacionRepository.findByPadreIdOrHijoId(id, id);
        relacionRepository.deleteAll(relacionadas);
        flujoRepository.deleteById(id);
    }

    // ── Relaciones de flujo ────────────────────────────────────────────────────

    /** Crea una relación entre dos flujos */
    public FlujoRelacion crearRelacion(FlujoRelacionRequest req) {
        FlujoRelacion relacion = FlujoRelacion.builder()
                .politicaId(req.getPoliticaId())
                .padreId(req.getPadreId()).hijoId(req.getHijoId())
                .condicionCampo(req.getCondicionCampo()).condicionValor(req.getCondicionValor())
                .tipo(req.getTipo() != null ? req.getTipo() : "secuencial")
                .build();
        return relacionRepository.save(relacion);
    }

    public List<FlujoRelacion> listarRelacionesPorPolitica(String politicaId) {
        return relacionRepository.findByPoliticaId(politicaId);
    }

    public List<FlujoRelacion> listarRelacionesPorPadre(String padreId) {
        return relacionRepository.findByPadreId(padreId);
    }

    public FlujoRelacion obtenerRelacion(String id) {
        return relacionRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Relación no encontrada: " + id));
    }

    /** Actualiza la condición de una relación existente */
    public FlujoRelacion actualizarRelacion(String id, FlujoRelacionRequest req) {
        FlujoRelacion relacion = obtenerRelacion(id);
        relacion.setCondicionCampo(req.getCondicionCampo());
        relacion.setCondicionValor(req.getCondicionValor());
        if (req.getTipo() != null) relacion.setTipo(req.getTipo());
        return relacionRepository.save(relacion);
    }

    public void eliminarRelacion(String id) { relacionRepository.deleteById(id); }

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

    public Notificacion marcarLeida(String id) {
        Notificacion notif = notificacionRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Notificación no encontrada: " + id));
        notif.setLeida(true);
        return notificacionRepository.save(notif);
    }

    public void marcarTodasLeidas(String usuarioId) {
        List<Notificacion> pendientes = notificacionRepository
                .findByUsuarioIdAndLeidaFalseOrderByFechaDesc(usuarioId);
        pendientes.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(pendientes);
    }

    /**
     * Crea y envía una notificación por WS a todos los usuarios activos de una empresa.
     * Si rolId no es nulo ni vacío, filtra solo ese rol.
     * Retorna la cantidad de notificaciones enviadas.
     */
    public int broadcast(String empresaId, String rolId, String mensaje) {
        List<Usuario> usuarios = (rolId != null && !rolId.isBlank())
                ? usuarioRepository.findByEmpresaIdAndRolId(empresaId, rolId)
                : usuarioRepository.findByEmpresaIdAndActivoTrue(empresaId);

        List<Notificacion> notificaciones = usuarios.stream().map(u ->
                Notificacion.builder()
                        .usuarioId(u.getId())
                        .tipo("broadcast")
                        .mensaje(mensaje)
                        .leida(false)
                        .build()
        ).toList();

        notificacionRepository.saveAll(notificaciones);

        notificaciones.forEach(n ->
                mensajeria.convertAndSend("/queue/notificaciones/" + n.getUsuarioId(), n)
        );

        return notificaciones.size();
    }

    // ── Util ──────────────────────────────────────────────────────────────────

    private UsuarioResponse mapearUsuario(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId()).nombre(u.getNombre()).apellido(u.getApellido())
                .correo(u.getCorreo()).rolId(u.getRolId()).empresaId(u.getEmpresaId())
                .departamentoId(u.getDepartamentoId()).activo(u.isActivo()).build();
    }
}
