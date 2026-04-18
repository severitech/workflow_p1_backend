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
    private final TramiteRepository tramiteRepository;
    private final BitacoraRepository bitacoraRepository;
    private final NotificacionRepository notificacionRepository;
    private final PasswordEncoder encoderPassword;

    // ─── IDs fijos ────────────────────────────────────────────
    private static final String EMP1         = "emp_001";
    private static final String DEP_REC      = "dep_001";
    private static final String DEP_TEC      = "dep_002";
    private static final String DEP_ADM      = "dep_003";
    // Roles — solo 3
    private static final String ROL_CLI      = "rol_001";
    private static final String ROL_REC      = "rol_002";
    private static final String ROL_ADM      = "rol_003";
    // Usuarios
    private static final String USR_MARIA    = "usr_001";  // recepcionista
    private static final String USR_JORGE    = "usr_002";  // administrador
    private static final String USR_CARLOS   = "usr_003";  // administrador (gestiona técnica)
    private static final String USR_ROBERTO  = "usr_004";  // cliente
    private static final String USR_LUISA    = "usr_005";  // cliente
    private static final String USR_PEDRO    = "usr_006";  // cliente
    // Tipos de componente
    private static final String TC_TEXTO     = "tc_001";
    private static final String TC_TEXTAREA  = "tc_002";
    private static final String TC_SELECT    = "tc_003";
    private static final String TC_BOOLEAN   = "tc_004";
    private static final String TC_FECHA     = "tc_005";
    private static final String TC_ARCHIVO   = "tc_006";
    private static final String TC_NUMERO    = "tc_007";
    // Formularios
    private static final String FORM_DATOS   = "form_001";
    private static final String FORM_CREDITO = "form_002";
    private static final String FORM_EVAL    = "form_003";
    private static final String FORM_CIERRE  = "form_004";
    private static final String FORM_MEDIDOR = "form_005";
    // Componentes
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
    // Políticas
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
        // Limpia todas las colecciones para garantizar datos frescos en cada arranque dev
        log.info("[DataSeeder] Limpiando colecciones existentes...");
        notificacionRepository.deleteAll();
        bitacoraRepository.deleteAll();
        tramiteRepository.deleteAll();
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
        Empresa empresa = empresaRepository.save(Empresa.builder()
                .id(EMP1).nombre("CRE Bolivia").nit("1234567890")
                .direccion("Av. Cañoto 1234, Santa Cruz de la Sierra")
                .telefono("591-3-3333333").correo("info@crebolivia.com").activo(true).build());
        log.info("[DataSeeder] Empresa: id={}", empresa.getId());

        // ── Departamentos ─────────────────────────────────────
        departamentoRepository.saveAll(List.of(
                dep(DEP_REC, "Recepción",     "Atención al cliente",        EMP1),
                dep(DEP_TEC, "Técnico",       "Inspecciones y evaluaciones", EMP1),
                dep(DEP_ADM, "Administración","Gestión y administración",    EMP1)
        ));
        log.info("[DataSeeder] Departamentos: {}, {}, {}", DEP_REC, DEP_TEC, DEP_ADM);

        // ── Roles — solo 3 ────────────────────────────────────
        rolRepository.saveAll(List.of(
                rol(ROL_CLI, "cliente",       "Ciudadano que solicita un trámite"),
                rol(ROL_REC, "recepcionista", "Recibe y gestiona trámites"),
                rol(ROL_ADM, "administrador", "Administrador con acceso completo")
        ));
        log.info("[DataSeeder] Roles: cliente={}, recepcionista={}, administrador={}", ROL_CLI, ROL_REC, ROL_ADM);

        // ── Usuarios ──────────────────────────────────────────
        usuarioRepository.saveAll(List.of(
                usr(USR_MARIA,   "María",   "García",  "maria@crebolivia.com",  passHash, ROL_REC, EMP1, DEP_REC),
                usr(USR_JORGE,   "Jorge",   "López",   "jorge@crebolivia.com",  passHash, ROL_ADM, EMP1, DEP_ADM),
                usr(USR_CARLOS,  "Carlos",  "Pérez",   "carlos@crebolivia.com", passHash, ROL_ADM, EMP1, DEP_TEC),
                usr(USR_ROBERTO, "Roberto", "Flores",  "roberto@cliente.com",   passHash, ROL_CLI, EMP1, null),
                usr(USR_LUISA,   "Luisa",   "Méndez",  "luisa@cliente.com",     passHash, ROL_CLI, EMP1, null),
                usr(USR_PEDRO,   "Pedro",   "Vásquez", "pedro@cliente.com",     passHash, ROL_CLI, EMP1, null)
        ));
        log.info("[DataSeeder] Usuarios creados: maria(recepcionista), jorge(admin), carlos(admin), 3 clientes");

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
                                cmp(CMP_NOMBRE, TC_TEXTO,    "Nombre completo",       "Ej: Juan Pérez",    1, true,  null),
                                cmp(CMP_CI,     TC_NUMERO,   "Cédula de identidad",   "Ej: 12345678",      2, true,  null),
                                cmp(CMP_TEL,    TC_TEXTO,    "Teléfono",              "Ej: 70000000",      3, true,  null),
                                cmp(CMP_DIR,    TC_TEXTAREA, "Dirección",             "Calle, barrio...",  4, true,  null)
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
                                cmp(CMP_OBS,      TC_TEXTAREA, "Observaciones",       "Describa hallazgos...", 1, false, null),
                                cmp(CMP_APROBADO, TC_BOOLEAN,  "Aprobado",            "",                      2, true,  null)
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
                        .tipo("secuencial").estado("activa").version(1)
                        .empresaId(EMP1).creadoPorId(USR_JORGE).build(),
                PoliticaNegocio.builder().id(POL_CREDITO).nombre("Solicitud de crédito")
                        .descripcion("Evaluación con flujos condicionales por tipo de crédito")
                        .tipo("secuencial").estado("activa").version(1)
                        .empresaId(EMP1).creadoPorId(USR_JORGE).build()
        ));
        log.info("[DataSeeder] Políticas creadas: {}, {}", POL_MEDIDOR, POL_CREDITO);

        // ── Flujos ────────────────────────────────────────────
        flujoRepository.saveAll(List.of(
                // Medidor — 3 pasos secuenciales
                flu(FLU_REC,     "Recepción",          POL_MEDIDOR, FORM_MEDIDOR,  1, true, null,       null,               null,       DEP_REC, 8),
                flu(FLU_TEC,     "Evaluación técnica", POL_MEDIDOR, FORM_EVAL,     2, true, null,       null,               null,       DEP_TEC, 24),
                flu(FLU_CIERREM, "Cierre",             POL_MEDIDOR, FORM_CIERRE,   3, true, null,       null,               null,       DEP_REC, 4),
                // Crédito — raíces
                flu(FLU_DATOS,   "Datos del cliente",  POL_CREDITO, FORM_DATOS,    1, true, null,       null,               null,       DEP_REC, 8),
                flu(FLU_TIPO_CR, "Tipo de crédito",    POL_CREDITO, FORM_CREDITO,  2, true, null,       null,               null,       DEP_REC, 8),
                // Hijos condicionales
                flu(FLU_VIVIEN,  "Crédito vivienda",   POL_CREDITO, FORM_EVAL,     3, true, FLU_TIPO_CR,"Tipo de crédito",  "vivienda", DEP_ADM, 48),
                flu(FLU_EMPRESA, "Crédito empresa",    POL_CREDITO, FORM_EVAL,     3, true, FLU_TIPO_CR,"Tipo de crédito",  "empresa",  DEP_ADM, 72),
                flu(FLU_VEHIC,   "Crédito vehículo",   POL_CREDITO, FORM_EVAL,     3, true, FLU_TIPO_CR,"Tipo de crédito",  "vehiculo", DEP_TEC, 24),
                flu(FLU_CIERREC, "Cierre crédito",     POL_CREDITO, FORM_CIERRE,   4, true, null,       null,               null,       DEP_REC, 4)
        ));
        log.info("[DataSeeder] Flujos creados (incluye 3 flujos condicionales)");

        // ── Trámites ──────────────────────────────────────────
        tramiteRepository.saveAll(List.of(

            // TRA-001: Roberto — medidor — EN PROCESO (evaluación activa, semáforo amarillo)
            Tramite.builder().id(TRA1).politicaId(POL_MEDIDOR)
                .clienteId(USR_ROBERTO).recepcionistaId(USR_MARIA).empresaId(EMP1)
                .estado("proceso").semaforo("amarillo").fecha(h24)
                .actividades(List.of(
                    act("act_001", FLU_REC, USR_MARIA, DEP_REC, "Recepción", "completado", 8, "verde", h24, h12,
                        "Documentos recibidos correctamente",
                        datosForm(FORM_MEDIDOR, "completado", h12, List.of(
                            dato(CMP_MEDIDOR, "Número de medidor actual", "MED-78432"),
                            dato(CMP_UBIC, "Ubicación exacta del domicilio", "Equipetrol, Calle 3 Este #456")
                        ), List.of())),
                    act("act_002", FLU_TEC, USR_CARLOS, DEP_TEC, "Evaluación técnica", "activo", 24, "amarillo", h12, null,
                        null,
                        datosForm(FORM_EVAL, "en_proceso", h6, List.of(
                            dato(CMP_OBS, "Observaciones", "Instalación requiere cable de 10mm")
                        ), List.of()))
                )).build(),

            // TRA-002: Luisa — medidor — COMPLETADO
            Tramite.builder().id(TRA2).politicaId(POL_MEDIDOR)
                .clienteId(USR_LUISA).recepcionistaId(USR_MARIA).empresaId(EMP1)
                .estado("completado").semaforo("verde").fecha(h48).fechaFin(h12)
                .actividades(List.of(
                    act("act_003", FLU_REC, USR_MARIA, DEP_REC, "Recepción", "completado", 8, "verde", h48, h48,
                        "Datos completos",
                        datosForm(FORM_MEDIDOR, "completado", h48, List.of(
                            dato(CMP_MEDIDOR, "Número de medidor actual", "MED-11111"),
                            dato(CMP_UBIC, "Ubicación exacta del domicilio", "Urbarí, Calle Las Palmas #123")
                        ), List.of())),
                    act("act_004", FLU_TEC, USR_CARLOS, DEP_TEC, "Evaluación técnica", "completado", 24, "verde", h48, h24,
                        "Instalación viable",
                        datosForm(FORM_EVAL, "completado", h24, List.of(
                            dato(CMP_OBS,      "Observaciones", "Sin observaciones, instalación estándar"),
                            dato(CMP_APROBADO, "Aprobado",      "true")
                        ), List.of())),
                    act("act_005", FLU_CIERREM, USR_MARIA, DEP_REC, "Cierre", "completado", 4, "verde", h24, h12,
                        "Trámite cerrado correctamente",
                        datosForm(FORM_CIERRE, "completado", h12, List.of(
                            dato(CMP_DOC, "Documento de cierre", "acta_cierre_tra002.pdf")
                        ), List.of()))
                )).build(),

            // TRA-003: Pedro — medidor — URGENTE (semáforo rojo, sin completar en 48h)
            Tramite.builder().id(TRA3).politicaId(POL_MEDIDOR)
                .clienteId(USR_PEDRO).recepcionistaId(USR_MARIA).empresaId(EMP1)
                .estado("urgente").semaforo("rojo").fecha(h48)
                .actividades(List.of(
                    act("act_006", FLU_REC, USR_MARIA, DEP_REC, "Recepción", "completado", 8, "verde", h48, h48,
                        null,
                        datosForm(FORM_MEDIDOR, "completado", h48, List.of(
                            dato(CMP_MEDIDOR, "Número de medidor actual", "MED-99999"),
                            dato(CMP_UBIC, "Ubicación exacta del domicilio", "Plan Tres Mil, Av. Principal #789")
                        ), List.of())),
                    act("act_007", FLU_TEC, USR_CARLOS, DEP_TEC, "Evaluación técnica", "activo", 24, "rojo", h48, null,
                        null,
                        datosForm(FORM_EVAL, "en_proceso", h48, List.of(), List.of(
                            obs(USR_MARIA, "Por favor priorizar este trámite", "observado", h6)
                        )))
                )).build(),

            // TRA-004: Roberto — crédito — EN PROCESO (en paso tipo crédito)
            Tramite.builder().id(TRA4).politicaId(POL_CREDITO)
                .clienteId(USR_ROBERTO).recepcionistaId(USR_MARIA).empresaId(EMP1)
                .estado("proceso").semaforo("verde").fecha(h6)
                .actividades(List.of(
                    act("act_008", FLU_DATOS, USR_MARIA, DEP_REC, "Datos del cliente", "completado", 8, "verde", h6, h2,
                        null,
                        datosForm(FORM_DATOS, "completado", h2, List.of(
                            dato(CMP_NOMBRE, "Nombre completo",     "Roberto Flores Suárez"),
                            dato(CMP_CI,     "Cédula de identidad", "5678901"),
                            dato(CMP_TEL,    "Teléfono",            "76543210"),
                            dato(CMP_DIR,    "Dirección",           "Av. Brasil #1200, Santa Cruz")
                        ), List.of())),
                    act("act_009", FLU_TIPO_CR, USR_MARIA, DEP_REC, "Tipo de crédito", "activo", 8, "verde", h2, null,
                        null,
                        datosForm(FORM_CREDITO, "en_proceso", h2, List.of(), List.of()))
                )).build(),

            // TRA-005: Luisa — crédito — flujo condicional "vivienda" activado
            Tramite.builder().id(TRA5).politicaId(POL_CREDITO)
                .clienteId(USR_LUISA).recepcionistaId(USR_MARIA).empresaId(EMP1)
                .estado("proceso").semaforo("verde").fecha(h24)
                .actividades(List.of(
                    act("act_010", FLU_DATOS, USR_MARIA, DEP_REC, "Datos del cliente", "completado", 8, "verde", h24, h12,
                        null,
                        datosForm(FORM_DATOS, "completado", h12, List.of(
                            dato(CMP_NOMBRE, "Nombre completo",     "Luisa Méndez Torres"),
                            dato(CMP_CI,     "Cédula de identidad", "7654321"),
                            dato(CMP_TEL,    "Teléfono",            "71234567"),
                            dato(CMP_DIR,    "Dirección",           "Barrio Hamacas, Calle 5 #200")
                        ), List.of())),
                    act("act_011", FLU_TIPO_CR, USR_MARIA, DEP_REC, "Tipo de crédito", "completado", 8, "verde", h12, h6,
                        null,
                        datosForm(FORM_CREDITO, "completado", h6, List.of(
                            dato(CMP_TIPO_CR, "Tipo de crédito",       "vivienda"),
                            dato(CMP_MONTO,   "Monto solicitado (Bs)", "150000")
                        ), List.of())),
                    // Flujo condicional activado por condicionValor="vivienda"
                    act("act_012", FLU_VIVIEN, USR_JORGE, DEP_ADM, "Crédito vivienda", "activo", 48, "verde", h6, null,
                        null,
                        datosForm(FORM_EVAL, "en_proceso", h6, List.of(), List.of()))
                )).build()
        ));
        log.info("[DataSeeder] Trámites creados: {}, {}, {}, {}, {}", TRA1, TRA2, TRA3, TRA4, TRA5);

        // ── Bitácora ──────────────────────────────────────────
        bitacoraRepository.saveAll(List.of(
                bitacora(TRA1, USR_MARIA,  "INICIAR",   "Trámite de medidor iniciado",               null,      "proceso",    h24),
                bitacora(TRA1, USR_MARIA,  "AVANZAR",   "Recepción completada",                      "proceso", "proceso",    h12),
                bitacora(TRA2, USR_MARIA,  "INICIAR",   "Trámite de medidor iniciado",               null,      "proceso",    h48),
                bitacora(TRA2, USR_CARLOS, "AVANZAR",   "Evaluación técnica completada",             "proceso", "proceso",    h24),
                bitacora(TRA2, USR_MARIA,  "COMPLETAR", "Trámite completado",                        "proceso", "completado", h12),
                bitacora(TRA3, USR_MARIA,  "INICIAR",   "Trámite de medidor iniciado",               null,      "proceso",    h48),
                bitacora(TRA3, USR_MARIA,  "URGENTE",   "Semáforo cambió a ROJO por tiempo vencido", "proceso", "urgente",    h6),
                bitacora(TRA4, USR_MARIA,  "INICIAR",   "Crédito iniciado",                          null,      "proceso",    h6),
                bitacora(TRA5, USR_MARIA,  "AVANZAR",   "Flujo condicional activado: vivienda",      "proceso", "proceso",    h6)
        ));

        // ── Notificaciones ────────────────────────────────────
        notificacionRepository.saveAll(List.of(
                notif(USR_CARLOS,  TRA1, "NUEVA_ACTIVIDAD", "Se te asignó la evaluación técnica del trámite TRA-001", false, h12),
                notif(USR_ROBERTO, TRA1, "AVANCE",          "Tu trámite de medidor avanzó a Evaluación técnica",      true,  h12),
                notif(USR_LUISA,   TRA2, "COMPLETADO",      "Tu trámite de medidor fue completado exitosamente",      true,  h12),
                notif(USR_MARIA,   TRA3, "SEMAFORO_ROJO",   "Trámite TRA-003 excedió el tiempo límite — URGENTE",     false, h6),
                notif(USR_CARLOS,  TRA3, "SEMAFORO_ROJO",   "Tu actividad en TRA-003 está vencida — priorizar",       false, h6),
                notif(USR_PEDRO,   TRA3, "URGENTE",         "Tu trámite fue marcado como URGENTE por demora",         false, h6),
                notif(USR_MARIA,   TRA4, "NUEVA_ACTIVIDAD", "Nuevo trámite de crédito ingresado para Roberto Flores", true,  h6),
                notif(USR_JORGE,   TRA5, "NUEVA_ACTIVIDAD", "Se te asignó evaluación de crédito vivienda — TRA-005", false, h1),
                notif(USR_LUISA,   TRA5, "AVANCE",          "Tu solicitud de crédito avanzó a revisión — TRA-005",    false, h1)
        ));

        log.info("[DataSeeder] ══════════════════════════════════════════════════");
        log.info("[DataSeeder]  Seeding completado — workflow_dev");
        log.info("[DataSeeder]  Roles disponibles: cliente, recepcionista, administrador");
        log.info("[DataSeeder]  Credenciales de prueba (password: 123456)");
        log.info("[DataSeeder]  maria@crebolivia.com  → recepcionista");
        log.info("[DataSeeder]  jorge@crebolivia.com  → administrador");
        log.info("[DataSeeder]  carlos@crebolivia.com → administrador");
        log.info("[DataSeeder]  roberto@cliente.com   → cliente");
        log.info("[DataSeeder]  luisa@cliente.com     → cliente");
        log.info("[DataSeeder]  pedro@cliente.com     → cliente");
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

    private Flujo flu(String id, String nombre, String politicaId, String formularioId,
                      int orden, boolean obligatorio, String padreId,
                      String condCampo, String condValor, String depId, int horas) {
        return Flujo.builder().id(id).nombre(nombre).politicaId(politicaId)
                .formularioId(formularioId).orden(orden).esObligatorio(obligatorio)
                .flujoPadreId(padreId).condicionCampo(condCampo).condicionValor(condValor)
                .departamentoId(depId).tiempoLimiteHoras(horas).build();
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
                          String estado, int tiempoLimite, String semaforo,
                          LocalDateTime inicio, LocalDateTime fin, String observacion,
                          DatosClienteForm datosForm) {
        return Actividad.builder().id(id).flujoId(flujoId).usuarioId(usuarioId).departamentoId(depId)
                .nombre(nombre).estado(estado).tiempoLimite(tiempoLimite).semaforo(semaforo)
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
