package com.workflow.config;

import com.workflow.document.*;
import com.workflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Carga datos sintéticos al iniciar con perfil dev — IDs fijos para referencias consistentes */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoComponenteRepository tipoComponenteRepository;
    private final FormularioRepository formularioRepository;
    private final PoliticaNegocioRepository politicaRepository;
    private final FlujoRepository flujoRepository;
    private final FlujoRelacionRepository flujoRelacionRepository;
    private final TramiteRepository tramiteRepository;
    private final BitacoraRepository bitacoraRepository;
    private final NotificacionRepository notificacionRepository;
    private final PasswordEncoder encoderPassword;

    // ─── IDs fijos ────────────────────────────────────────────
    private static final String EMP1         = "emp_001";
    private static final String DEP_REC      = "dep_001";
    private static final String DEP_TEC      = "dep_002";
    private static final String DEP_ADM      = "dep_003";
    private static final String DEP_REC2     = "dep_004";
    private static final String DEP_REC3     = "dep_005";
    private static final String DEP_REC4     = "dep_006";
    private static final String DEP_REC5     = "dep_007";
    private static final String ROL_CLI      = "rol_001";
    private static final String ROL_REC      = "rol_002";
    private static final String ROL_ADM      = "rol_003";
    private static final String USR_JORGE    = "usr_001";
    private static final String USR_CARLOS   = "usr_002";
    private static final String USR_ROBERTO  = "usr_003";
    private static final String USR_LUISA    = "usr_004";
    private static final String USR_MARIA    = "usr_005";
    private static final String USR_ANA      = "usr_006";
    private static final String USR_SOFIA    = "usr_007";
    private static final String USR_ELENA    = "usr_008";
    private static final String USR_ROSA     = "usr_009";
    private static final String TC_TEXTO     = "tc_001";
    private static final String TC_TEXTAREA  = "tc_002";
    private static final String TC_SELECT    = "tc_003";
    private static final String TC_BOOLEAN   = "tc_004";
    private static final String TC_FECHA     = "tc_005";
    private static final String TC_ARCHIVO   = "tc_006";
    private static final String TC_NUMERO    = "tc_007";
    private static final String FORM_DATOS   = "form_001";
    private static final String FORM_CREDITO = "form_002";
    private static final String FORM_EVAL    = "form_003";
    private static final String FORM_CIERRE  = "form_004";
    private static final String FORM_MEDIDOR = "form_005";
    private static final String CMP_NOMBRE   = "cmp_001";
    private static final String CMP_CI       = "cmp_002";
    private static final String CMP_TEL      = "cmp_003";
    private static final String CMP_DIR      = "cmp_004";
    private static final String CMP_TIPO_CR  = "cmp_005";
    private static final String CMP_MONTO    = "cmp_006";
    private static final String CMP_OBS      = "cmp_007";
    private static final String CMP_APROBADO = "cmp_008";
    private static final String CMP_DOC      = "cmp_009";
    private static final String CMP_MEDIDOR  = "cmp_010";
    private static final String CMP_UBIC     = "cmp_011";
    private static final String POL_MEDIDOR  = "pol_001";
    private static final String POL_CREDITO  = "pol_002";
    // Flujos
    private static final String FLU_REC      = "flu_001";
    private static final String FLU_TEC      = "flu_002";
    private static final String FLU_CIERREM  = "flu_003";
    private static final String FLU_DATOS    = "flu_010";
    private static final String FLU_TIPO_CR  = "flu_011";
    private static final String FLU_VIVIEN   = "flu_012";
    private static final String FLU_EMPRESA  = "flu_013";
    private static final String FLU_VEHIC    = "flu_014";
    private static final String FLU_CIERREC  = "flu_015";
    // Trámites
    private static final String TRA1         = "tra_001";
    private static final String TRA2         = "tra_002";
    private static final String TRA3         = "tra_003";
    private static final String TRA4         = "tra_004";
    private static final String TRA5         = "tra_005";

    @Override
    public void run(String... args) {
        log.info("[DataSeeder] Limpiando colecciones existentes...");
        notificacionRepository.deleteAll();
        bitacoraRepository.deleteAll();
        tramiteRepository.deleteAll();
        flujoRelacionRepository.deleteAll();
        flujoRepository.deleteAll();
        politicaRepository.deleteAll();
        formularioRepository.deleteAll();
        tipoComponenteRepository.deleteAll();
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();
        departamentoRepository.deleteAll();
        empresaRepository.deleteAll();

        log.info("[DataSeeder] ══ Iniciando carga de datos sintéticos ══");

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime h1  = ahora.minusHours(1);
        LocalDateTime h2  = ahora.minusHours(2);
        LocalDateTime h6  = ahora.minusHours(6);
        LocalDateTime h12 = ahora.minusHours(12);
        LocalDateTime h24 = ahora.minusHours(24);
        LocalDateTime h48 = ahora.minusHours(48);

        String passHash = encoderPassword.encode("123456");

        // ── Empresa ───────────────────────────────────────────
        empresaRepository.save(Empresa.builder()
                .id(EMP1).nombre("CRE Bolivia").nit("1234567890")
                .direccion("Av. Cañoto 1234, Santa Cruz de la Sierra")
                .telefono("591-3-3333333").correo("info@crebolivia.com").activo(true).build());
        log.info("[DataSeeder] Empresa creada: {}", EMP1);

        // ── Departamentos ─────────────────────────────────────
        departamentoRepository.saveAll(List.of(
                dep(DEP_REC,  "Recepción 1",   "Recepción — ventanilla 1", EMP1),
                dep(DEP_TEC,  "Técnico",        "Inspecciones y evaluaciones", EMP1),
                dep(DEP_ADM,  "Administración", "Gestión y administración", EMP1),
                dep(DEP_REC2, "Recepción 2",    "Recepción — ventanilla 2", EMP1),
                dep(DEP_REC3, "Recepción 3",    "Recepción — ventanilla 3", EMP1),
                dep(DEP_REC4, "Recepción 4",    "Recepción — ventanilla 4", EMP1),
                dep(DEP_REC5, "Recepción 5",    "Recepción — ventanilla 5", EMP1)
        ));

        // ── Roles ─────────────────────────────────────────────
        rolRepository.saveAll(List.of(
                rol(ROL_CLI, "cliente",       "Ciudadano que solicita un trámite"),
                rol(ROL_REC, "recepcionista", "Recibe y gestiona trámites"),
                rol(ROL_ADM, "administrador", "Administrador con acceso completo")
        ));

        // ── Usuarios ──────────────────────────────────────────
        usuarioRepository.saveAll(List.of(
                usr(USR_JORGE,   "Jorge",   "López",  "admin1@crebolivia.com",     passHash, ROL_ADM, EMP1, DEP_ADM),
                usr(USR_CARLOS,  "Carlos",  "Pérez",  "admin2@crebolivia.com",     passHash, ROL_ADM, EMP1, DEP_ADM),
                usr(USR_ROBERTO, "Roberto", "Flores", "cliente1@crebolivia.com",   passHash, ROL_CLI, EMP1, null),
                usr(USR_LUISA,   "Luisa",   "Méndez", "cliente2@crebolivia.com",   passHash, ROL_CLI, EMP1, null),
                usr(USR_MARIA,   "María",   "García", "recepcion1@crebolivia.com", passHash, ROL_REC, EMP1, DEP_REC),
                usr(USR_ANA,     "Ana",     "Torres", "recepcion2@crebolivia.com", passHash, ROL_REC, EMP1, DEP_REC2),
                usr(USR_SOFIA,   "Sofía",   "Vargas", "recepcion3@crebolivia.com", passHash, ROL_REC, EMP1, DEP_REC3),
                usr(USR_ELENA,   "Elena",   "Rojas",  "recepcion4@crebolivia.com", passHash, ROL_REC, EMP1, DEP_REC4),
                usr(USR_ROSA,    "Rosa",    "Mamani", "recepcion5@crebolivia.com", passHash, ROL_REC, EMP1, DEP_REC5)
        ));
        log.info("[DataSeeder] Usuarios creados: 2 admin, 2 clientes, 5 recepcionistas");

        // ── TipoComponentes ───────────────────────────────────
        tipoComponenteRepository.saveAll(List.of(
                tc(TC_TEXTO,    "texto",    "Campo de texto corto"),
                tc(TC_TEXTAREA, "textarea", "Área de texto largo"),
                tc(TC_SELECT,   "select",   "Lista desplegable"),
                tc(TC_BOOLEAN,  "boolean",  "Casilla verdadero/falso"),
                tc(TC_FECHA,    "fecha",    "Selector de fecha"),
                tc(TC_ARCHIVO,  "archivo",  "Carga de archivo"),
                tc(TC_NUMERO,   "numero",   "Campo numérico")
        ));

        // ── Formularios ───────────────────────────────────────
        formularioRepository.saveAll(List.of(
                Formulario.builder().id(FORM_DATOS).nombre("Datos del cliente")
                        .descripcion("Información básica del solicitante").empresaId(EMP1).activo(true)
                        .componentes(List.of(
                                cmp(CMP_NOMBRE, TC_TEXTO,    "Nombre completo",     "Ej: Juan Pérez",   1, true,  null),
                                cmp(CMP_CI,     TC_NUMERO,   "Cédula de identidad", "Ej: 12345678",     2, true,  null),
                                cmp(CMP_TEL,    TC_TEXTO,    "Teléfono",            "Ej: 70000000",     3, true,  null),
                                cmp(CMP_DIR,    TC_TEXTAREA, "Dirección",           "Calle, barrio...", 4, true,  null)
                        )).build(),
                Formulario.builder().id(FORM_CREDITO).nombre("Tipo de crédito")
                        .descripcion("Selección del tipo de crédito").empresaId(EMP1).activo(true)
                        .componentes(List.of(
                                cmp(CMP_TIPO_CR, TC_SELECT, "Tipo de crédito",       "Seleccione...", 1, true, List.of("vivienda", "empresa", "vehiculo")),
                                cmp(CMP_MONTO,   TC_NUMERO, "Monto solicitado (Bs)", "Ej: 50000",     2, true, null)
                        )).build(),
                Formulario.builder().id(FORM_EVAL).nombre("Evaluación")
                        .descripcion("Resultados de la evaluación").empresaId(EMP1).activo(true)
                        .componentes(List.of(
                                cmp(CMP_OBS,      TC_TEXTAREA, "Observaciones", "Describa hallazgos...", 1, false, null),
                                cmp(CMP_APROBADO, TC_BOOLEAN,  "Aprobado",      "",                      2, true,  null)
                        )).build(),
                Formulario.builder().id(FORM_CIERRE).nombre("Cierre de trámite")
                        .descripcion("Documentación final").empresaId(EMP1).activo(true)
                        .componentes(List.of(
                                cmp(CMP_DOC, TC_ARCHIVO, "Documento de cierre", "", 1, true, null)
                        )).build(),
                Formulario.builder().id(FORM_MEDIDOR).nombre("Solicitud de medidor")
                        .descripcion("Datos para instalación de medidor eléctrico").empresaId(EMP1).activo(true)
                        .componentes(List.of(
                                cmp(CMP_MEDIDOR, TC_TEXTO,    "Número de medidor actual",       "Ej: MED-12345",         1, false, null),
                                cmp(CMP_UBIC,    TC_TEXTAREA, "Ubicación exacta del domicilio", "Barrio, calle, número", 2, true,  null)
                        )).build()
        ));
        log.info("[DataSeeder] Formularios creados");

        // ── Políticas ─────────────────────────────────────────
        politicaRepository.saveAll(List.of(
                PoliticaNegocio.builder().id(POL_MEDIDOR).nombre("Solicitud de medidor eléctrico")
                        .descripcion("Proceso para instalación de medidor residencial")
                        .estado("activa").version(1)
                        .empresaId(EMP1).creadoPorId(USR_JORGE).build(),
                PoliticaNegocio.builder().id(POL_CREDITO).nombre("Solicitud de crédito")
                        .descripcion("Evaluación con flujos condicionales por tipo de crédito")
                        .estado("activa").version(1)
                        .empresaId(EMP1).creadoPorId(USR_JORGE).build()
        ));

        // ── Flujos — sin campos de relación (los flujos son nodos puros) ────
        flujoRepository.saveAll(List.of(
                // Medidor — 3 pasos raíz secuenciales
                flu(FLU_REC,     POL_MEDIDOR, FORM_MEDIDOR, 1, true, DEP_REC),
                flu(FLU_TEC,     POL_MEDIDOR, FORM_EVAL,    2, true, DEP_TEC),
                flu(FLU_CIERREM, POL_MEDIDOR, FORM_CIERRE,  3, true, DEP_REC),
                // Crédito — raíces
                flu(FLU_DATOS,   POL_CREDITO, FORM_DATOS,   1, true, DEP_REC),
                flu(FLU_TIPO_CR, POL_CREDITO, FORM_CREDITO, 2, true, DEP_REC),
                // Hijos condicionales (múltiples padres posibles, múltiples hijos posibles)
                flu(FLU_VIVIEN,  POL_CREDITO, FORM_EVAL,    3, true, DEP_ADM),
                flu(FLU_EMPRESA, POL_CREDITO, FORM_EVAL,    3, true, DEP_ADM),
                flu(FLU_VEHIC,   POL_CREDITO, FORM_EVAL,    3, true, DEP_TEC),
                flu(FLU_CIERREC, POL_CREDITO, FORM_CIERRE,  4, true, DEP_REC)
        ));
        log.info("[DataSeeder] Flujos creados (9 nodos)");

        // ── Relaciones de flujo — muchos-a-muchos via FlujoRelacion ────────
        flujoRelacionRepository.saveAll(List.of(
                // Crédito: FLU_TIPO_CR → hijos condicionales (1 padre con 3 hijos)
                rel("rel_001", POL_CREDITO, FLU_TIPO_CR, FLU_VIVIEN,  "Tipo de crédito", "vivienda", "condicional"),
                rel("rel_002", POL_CREDITO, FLU_TIPO_CR, FLU_EMPRESA, "Tipo de crédito", "empresa",  "condicional"),
                rel("rel_003", POL_CREDITO, FLU_TIPO_CR, FLU_VEHIC,   "Tipo de crédito", "vehiculo", "condicional"),
                // Crédito: hijos convergen en FLU_CIERREC via relación "siguiente"
                // (3 padres distintos apuntan al mismo hijo — muchos padres, 1 hijo)
                rel("rel_004", POL_CREDITO, FLU_VIVIEN,  FLU_CIERREC, null, null, "siguiente"),
                rel("rel_005", POL_CREDITO, FLU_EMPRESA, FLU_CIERREC, null, null, "siguiente"),
                rel("rel_006", POL_CREDITO, FLU_VEHIC,   FLU_CIERREC, null, null, "siguiente")
        ));
        log.info("[DataSeeder] Relaciones creadas: 3 condicionales + 3 convergencias");

        // ── Trámites ──────────────────────────────────────────
        tramiteRepository.saveAll(List.of(

            Tramite.builder().id(TRA1).politicaId(POL_MEDIDOR)
                .clienteId(USR_ROBERTO).recepcionistaId(USR_MARIA).empresaId(EMP1)
                .estado("proceso").prioridad("normal").fecha(h24)
                .actividades(List.of(
                    act("act_001", FLU_REC, USR_MARIA, DEP_REC, "Recepción", "completado", h24, h12,
                        "Documentos recibidos",
                        datosForm(FORM_MEDIDOR, "completado", h12, List.of(
                            dato(CMP_MEDIDOR, "Número de medidor actual", "MED-78432"),
                            dato(CMP_UBIC, "Ubicación exacta del domicilio", "Equipetrol, Calle 3 Este #456")
                        ), List.of())),
                    act("act_002", FLU_TEC, USR_CARLOS, DEP_TEC, "Evaluación técnica", "activo", h12, null,
                        null,
                        datosForm(FORM_EVAL, "en_proceso", h6, List.of(
                            dato(CMP_OBS, "Observaciones", "Instalación requiere cable de 10mm")
                        ), List.of()))
                )).build(),

            Tramite.builder().id(TRA2).politicaId(POL_MEDIDOR)
                .clienteId(USR_LUISA).recepcionistaId(USR_MARIA).empresaId(EMP1)
                .estado("completado").prioridad("normal").fecha(h48).fechaFin(h12)
                .actividades(List.of(
                    act("act_003", FLU_REC, USR_MARIA, DEP_REC, "Recepción", "completado", h48, h48,
                        "Datos completos",
                        datosForm(FORM_MEDIDOR, "completado", h48, List.of(
                            dato(CMP_MEDIDOR, "Número de medidor actual", "MED-11111"),
                            dato(CMP_UBIC, "Ubicación exacta del domicilio", "Urbarí, Calle Las Palmas #123")
                        ), List.of())),
                    act("act_004", FLU_TEC, USR_CARLOS, DEP_TEC, "Evaluación técnica", "completado", h48, h24,
                        "Instalación viable",
                        datosForm(FORM_EVAL, "completado", h24, List.of(
                            dato(CMP_OBS,      "Observaciones", "Sin observaciones"),
                            dato(CMP_APROBADO, "Aprobado",      "true")
                        ), List.of())),
                    act("act_005", FLU_CIERREM, USR_MARIA, DEP_REC, "Cierre", "completado", h24, h12,
                        "Trámite cerrado",
                        datosForm(FORM_CIERRE, "completado", h12, List.of(
                            dato(CMP_DOC, "Documento de cierre", "acta_cierre_tra002.pdf")
                        ), List.of()))
                )).build(),

            Tramite.builder().id(TRA3).politicaId(POL_MEDIDOR)
                .clienteId(USR_ROBERTO).recepcionistaId(USR_ANA).empresaId(EMP1)
                .estado("urgente").prioridad("urgente").fecha(h48)
                .actividades(List.of(
                    act("act_006", FLU_REC, USR_MARIA, DEP_REC, "Recepción", "completado", h48, h48,
                        null,
                        datosForm(FORM_MEDIDOR, "completado", h48, List.of(
                            dato(CMP_MEDIDOR, "Número de medidor actual", "MED-99999"),
                            dato(CMP_UBIC, "Ubicación exacta del domicilio", "Plan Tres Mil, Av. Principal #789")
                        ), List.of())),
                    act("act_007", FLU_TEC, USR_CARLOS, DEP_TEC, "Evaluación técnica", "activo", h48, null,
                        null,
                        datosForm(FORM_EVAL, "en_proceso", h48, List.of(), List.of(
                            obs(USR_MARIA, "Por favor priorizar este trámite", "observado", h6)
                        )))
                )).build(),

            Tramite.builder().id(TRA4).politicaId(POL_CREDITO)
                .clienteId(USR_ROBERTO).recepcionistaId(USR_MARIA).empresaId(EMP1)
                .estado("proceso").prioridad("normal").fecha(h6)
                .actividades(List.of(
                    act("act_008", FLU_DATOS, USR_MARIA, DEP_REC, "Datos del cliente", "completado", h6, h2,
                        null,
                        datosForm(FORM_DATOS, "completado", h2, List.of(
                            dato(CMP_NOMBRE, "Nombre completo",     "Roberto Flores Suárez"),
                            dato(CMP_CI,     "Cédula de identidad", "5678901"),
                            dato(CMP_TEL,    "Teléfono",            "76543210"),
                            dato(CMP_DIR,    "Dirección",           "Av. Brasil #1200, Santa Cruz")
                        ), List.of())),
                    act("act_009", FLU_TIPO_CR, USR_MARIA, DEP_REC, "Tipo de crédito", "activo", h2, null,
                        null,
                        datosForm(FORM_CREDITO, "en_proceso", h2, List.of(), List.of()))
                )).build(),

            Tramite.builder().id(TRA5).politicaId(POL_CREDITO)
                .clienteId(USR_LUISA).recepcionistaId(USR_MARIA).empresaId(EMP1)
                .estado("proceso").prioridad("normal").fecha(h24)
                .actividades(List.of(
                    act("act_010", FLU_DATOS, USR_MARIA, DEP_REC, "Datos del cliente", "completado", h24, h12,
                        null,
                        datosForm(FORM_DATOS, "completado", h12, List.of(
                            dato(CMP_NOMBRE, "Nombre completo",     "Luisa Méndez Torres"),
                            dato(CMP_CI,     "Cédula de identidad", "7654321"),
                            dato(CMP_TEL,    "Teléfono",            "71234567"),
                            dato(CMP_DIR,    "Dirección",           "Barrio Hamacas, Calle 5 #200")
                        ), List.of())),
                    act("act_011", FLU_TIPO_CR, USR_MARIA, DEP_REC, "Tipo de crédito", "completado", h12, h6,
                        null,
                        datosForm(FORM_CREDITO, "completado", h6, List.of(
                            dato(CMP_TIPO_CR, "Tipo de crédito",       "vivienda"),
                            dato(CMP_MONTO,   "Monto solicitado (Bs)", "150000")
                        ), List.of())),
                    act("act_012", FLU_VIVIEN, USR_JORGE, DEP_ADM, "Crédito vivienda", "activo", h6, null,
                        null,
                        datosForm(FORM_EVAL, "en_proceso", h6, List.of(), List.of()))
                )).build()
        ));
        log.info("[DataSeeder] Trámites creados: {}, {}, {}, {}, {}", TRA1, TRA2, TRA3, TRA4, TRA5);

        // ── Bitácora ──────────────────────────────────────────
        bitacoraRepository.saveAll(List.of(
                bitacora(TRA1, USR_MARIA,  "INICIAR",   "Trámite de medidor iniciado",      null,      "proceso",    h24),
                bitacora(TRA1, USR_MARIA,  "AVANZAR",   "Recepción completada",              "proceso", "proceso",    h12),
                bitacora(TRA2, USR_MARIA,  "INICIAR",   "Trámite de medidor iniciado",      null,      "proceso",    h48),
                bitacora(TRA2, USR_CARLOS, "AVANZAR",   "Evaluación técnica completada",     "proceso", "proceso",    h24),
                bitacora(TRA2, USR_MARIA,  "COMPLETAR", "Trámite completado",                "proceso", "completado", h12),
                bitacora(TRA3, USR_MARIA,  "INICIAR",   "Trámite de medidor iniciado",      null,      "proceso",    h48),
                bitacora(TRA3, USR_MARIA,  "URGENTE",   "Semáforo cambió a ROJO",           "proceso", "urgente",    h6),
                bitacora(TRA4, USR_MARIA,  "INICIAR",   "Crédito iniciado",                  null,      "proceso",    h6),
                bitacora(TRA5, USR_MARIA,  "AVANZAR",   "Flujo condicional activado: vivienda", "proceso", "proceso", h6)
        ));

        // ── Notificaciones ────────────────────────────────────
        notificacionRepository.saveAll(List.of(
                notif(USR_CARLOS,  TRA1, "NUEVA_ACTIVIDAD", "Se te asignó la evaluación técnica del trámite TRA-001", false, h12),
                notif(USR_ROBERTO, TRA1, "AVANCE",          "Tu trámite de medidor avanzó a Evaluación técnica",      true,  h12),
                notif(USR_LUISA,   TRA2, "COMPLETADO",      "Tu trámite de medidor fue completado exitosamente",      true,  h12),
                notif(USR_MARIA,   TRA3, "SEMAFORO_ROJO",   "Trámite TRA-003 excedió el tiempo límite — URGENTE",     false, h6),
                notif(USR_CARLOS,  TRA3, "SEMAFORO_ROJO",   "Tu actividad en TRA-003 está vencida",                   false, h6),
                notif(USR_ROBERTO, TRA3, "URGENTE",         "Tu trámite fue marcado como URGENTE por demora",         false, h6),
                notif(USR_MARIA,   TRA4, "NUEVA_ACTIVIDAD", "Nuevo trámite de crédito ingresado",                     true,  h6),
                notif(USR_JORGE,   TRA5, "NUEVA_ACTIVIDAD", "Se te asignó evaluación de crédito vivienda",            false, h1),
                notif(USR_LUISA,   TRA5, "AVANCE",          "Tu solicitud de crédito avanzó a revisión",              false, h1)
        ));

        log.info("[DataSeeder] ══════════════════════════════════════════════════");
        log.info("[DataSeeder]  Seeding completado — workflow_dev");
        log.info("[DataSeeder]  Credenciales (password: 123456)");
        log.info("[DataSeeder]  admin1@crebolivia.com  → administrador");
        log.info("[DataSeeder]  admin2@crebolivia.com  → administrador");
        log.info("[DataSeeder]  cliente1@crebolivia.com  → cliente");
        log.info("[DataSeeder]  recepcion1@crebolivia.com → recepcionista");
        log.info("[DataSeeder] ══════════════════════════════════════════════════");
    }

    // ── Helpers ────────────────────────────────────────────────

    private Departamento dep(String id, String nombre, String desc, String empresaId) {
        return Departamento.builder().id(id).nombre(nombre).descripcion(desc).empresaId(empresaId).activo(true).build();
    }

    private Rol rol(String id, String nombre, String desc) {
        return Rol.builder().id(id).nombre(nombre).descripcion(desc).build();
    }

    private Usuario usr(String id, String nombre, String apellido, String correo,
                        String pass, String rolId, String empresaId, String depId) {
        return Usuario.builder().id(id).nombre(nombre).apellido(apellido).correo(correo)
                .password(pass).rolId(rolId).empresaId(empresaId).departamentoId(depId).activo(true).build();
    }

    private TipoComponente tc(String id, String nombre, String desc) {
        return TipoComponente.builder().id(id).nombre(nombre).descripcion(desc).build();
    }

    private Componente cmp(String id, String tipoId, String etiqueta, String placeholder,
                           int orden, boolean requerido, List<String> opciones) {
        return Componente.builder().id(id).tipoId(tipoId).etiqueta(etiqueta)
                .placeholder(placeholder).orden(orden).posicionX(0).posicionY(orden - 1)
                .requerido(requerido).opciones(opciones).build();
    }

    /** Crea un flujo puro sin campos de relación — las relaciones se crean por separado */
    private Flujo flu(String id, String politicaId, String formularioId,
                      int orden, boolean obligatorio, String depId) {
        return Flujo.builder().id(id).politicaId(politicaId)
                .formularioId(formularioId).orden(orden).esObligatorio(obligatorio)
                .departamentoId(depId).build();
    }

    /** Crea una relación entre dos flujos */
    private FlujoRelacion rel(String id, String politicaId, String padreId, String hijoId,
                              String condCampo, String condValor, String tipo) {
        return FlujoRelacion.builder().id(id).politicaId(politicaId)
                .padreId(padreId).hijoId(hijoId)
                .condicionCampo(condCampo).condicionValor(condValor).tipo(tipo).build();
    }

    private DatoForm dato(String componenteId, String etiqueta, String valor) {
        return DatoForm.builder().componenteId(componenteId).etiqueta(etiqueta).valor(valor).build();
    }

    private Observacion obs(String usuarioId, String descripcion, String estado, LocalDateTime fecha) {
        return Observacion.builder().usuarioId(usuarioId).descripcion(descripcion).estado(estado).fecha(fecha).build();
    }

    private DatosClienteForm datosForm(String formularioId, String estado, LocalDateTime fecha,
                                       List<DatoForm> datos, List<Observacion> observaciones) {
        return DatosClienteForm.builder().formularioId(formularioId).estado(estado)
                .datos(new ArrayList<>(datos)).observaciones(new ArrayList<>(observaciones)).fecha(fecha).build();
    }

    private Actividad act(String id, String flujoId, String usuarioId, String depId, String nombre,
                          String estado, LocalDateTime inicio, LocalDateTime fin, String observacion,
                          DatosClienteForm datosForm) {
        return Actividad.builder().id(id).flujoId(flujoId).usuarioId(usuarioId).departamentoId(depId)
                .nombre(nombre).estado(estado)
                .fechaInicio(inicio).fechaFin(fin).observacion(observacion).datosForm(datosForm).build();
    }

    private Bitacora bitacora(String tramiteId, String usuarioId, String accion,
                              String desc, String estadoAnt, String estadoNuevo, LocalDateTime fecha) {
        return Bitacora.builder().tramiteId(tramiteId).usuarioId(usuarioId).accion(accion)
                .descripcion(desc).estadoAnterior(estadoAnt).estadoNuevo(estadoNuevo).fecha(fecha).build();
    }

    private Notificacion notif(String usuarioId, String tramiteId, String tipo,
                               String mensaje, boolean leida, LocalDateTime fecha) {
        return Notificacion.builder().usuarioId(usuarioId).tramiteId(tramiteId).tipo(tipo)
                .mensaje(mensaje).leida(leida).fecha(fecha).build();
    }
}
