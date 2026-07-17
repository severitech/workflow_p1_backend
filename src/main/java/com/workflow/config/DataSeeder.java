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
@Profile({"dev", "seed"})
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
    private final DocumentoRepositorio documentoRepositorio;
    private final PermisoDocumentoRepositorio permisoDocumentoRepositorio;
    private final BitacoraDocumentoRepositorio bitacoraDocumentoRepositorio;
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
    private static final String TC_TABLA     = "tc_006";
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
    // Flujos — nodos de control UML (inicio/fin) + tareas
    private static final String FLU_INI_MED  = "flu_000";
    private static final String FLU_REC      = "flu_001";
    private static final String FLU_TEC      = "flu_002";
    private static final String FLU_CIERREM  = "flu_003";
    private static final String FLU_FIN_MED  = "flu_004";
    private static final String FLU_INI_CR   = "flu_009";
    private static final String FLU_DATOS    = "flu_010";
    private static final String FLU_TIPO_CR  = "flu_011";
    private static final String FLU_DEC_CR   = "flu_011b";
    private static final String FLU_VIVIEN   = "flu_012";
    private static final String FLU_EMPRESA  = "flu_013";
    private static final String FLU_VEHIC    = "flu_014";
    private static final String FLU_CIERREC  = "flu_015";
    private static final String FLU_FIN_CR   = "flu_016";
    // Trámites
    private static final String TRA1         = "tra_001";
    private static final String TRA2         = "tra_002";
    private static final String TRA3         = "tra_003";
    private static final String TRA4         = "tra_004";
    private static final String TRA5         = "tra_005";

    @Override
    public void run(String... args) {
        log.info("[DataSeeder] Limpiando colecciones existentes...");
        bitacoraDocumentoRepositorio.deleteAll();
        permisoDocumentoRepositorio.deleteAll();
        documentoRepositorio.deleteAll();
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
                tc(TC_TABLA,    "tabla",    "Tabla editable de filas y columnas"),
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
                                cmp(CMP_DOC, TC_TABLA, "Documentos entregados", "", 1, true, null)
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
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_JORGE).build(),
                PoliticaNegocio.builder().id(POL_CREDITO).nombre("Solicitud de crédito")
                        .descripcion("Evaluación con flujos condicionales por tipo de crédito")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_JORGE).build(),
                PoliticaNegocio.builder().id("pol_003").nombre("Conexión de agua potable")
                        .descripcion("Trámite para nueva conexión de agua domiciliaria")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_JORGE).build(),
                PoliticaNegocio.builder().id("pol_004").nombre("Regularización de deuda")
                        .descripcion("Plan de pagos para deudas vencidas con la cooperativa")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_CARLOS).build(),
                PoliticaNegocio.builder().id("pol_005").nombre("Cambio de titularidad")
                        .descripcion("Transferencia de contrato a nuevo titular")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_JORGE).build(),
                PoliticaNegocio.builder().id("pol_006").nombre("Inspección técnica domiciliaria")
                        .descripcion("Visita técnica para verificación de instalaciones")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_CARLOS).build(),
                PoliticaNegocio.builder().id("pol_007").nombre("Ampliación de potencia eléctrica")
                        .descripcion("Aumento de carga contratada para uso industrial")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_JORGE).build(),
                PoliticaNegocio.builder().id("pol_008").nombre("Baja de servicio")
                        .descripcion("Cancelación definitiva del servicio eléctrico")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_JORGE).build(),
                PoliticaNegocio.builder().id("pol_009").nombre("Reposición por corte")
                        .descripcion("Reconexión del servicio tras corte por morosidad")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_CARLOS).build(),
                PoliticaNegocio.builder().id("pol_010").nombre("Crédito hipotecario")
                        .descripcion("Préstamo con garantía inmobiliaria a largo plazo")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_JORGE).build(),
                PoliticaNegocio.builder().id("pol_011").nombre("Crédito vehicular")
                        .descripcion("Financiamiento para compra de vehículo nuevo o usado")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_CARLOS).build(),
                PoliticaNegocio.builder().id("pol_012").nombre("Apertura de cuenta de ahorros")
                        .descripcion("Alta de nueva cuenta de ahorros persona natural")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_JORGE).build(),
                PoliticaNegocio.builder().id("pol_013").nombre("Registro de empresa")
                        .descripcion("Inscripción de nueva empresa en el sistema cooperativo")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_JORGE).build(),
                PoliticaNegocio.builder().id("pol_014").nombre("Subsidio social energético")
                        .descripcion("Solicitud de tarifa subsidiada para hogares vulnerables")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_CARLOS).build(),
                PoliticaNegocio.builder().id("pol_015").nombre("Reclamo por facturación incorrecta")
                        .descripcion("Revisión y corrección de facturas con montos erróneos")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_JORGE).build(),
                PoliticaNegocio.builder().id("pol_016").nombre("Certificado de no adeudo")
                        .descripcion("Emisión de constancia de cuenta al día")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_CARLOS).build(),
                PoliticaNegocio.builder().id("pol_017").nombre("Instalación de panel solar")
                        .descripcion("Conexión de generación fotovoltaica a la red")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_JORGE).build(),
                PoliticaNegocio.builder().id("pol_018").nombre("Traslado de medidor")
                        .descripcion("Reubicación física del medidor eléctrico")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_CARLOS).build(),
                PoliticaNegocio.builder().id("pol_019").nombre("Habilitación comercial")
                        .descripcion("Alta de suministro eléctrico para local comercial")
                        .estado("activa").version(1).empresaId(EMP1).creadoPorId(USR_JORGE).build(),
                PoliticaNegocio.builder().id("pol_020").nombre("Préstamo de emergencia")
                        .descripcion("Crédito rápido para situaciones de emergencia comprobada")
                        .estado("borrador").version(1).empresaId(EMP1).creadoPorId(USR_CARLOS).build()
        ));

        // ── Flujos UML 2.5 — nodos de control + tareas por swimlane (departamento) ──
        flujoRepository.saveAll(List.of(
                // Medidor — nodos de control
                fluControl(FLU_INI_MED, POL_MEDIDOR, "Inicio",  0, "inicio", null),
                fluControl(FLU_FIN_MED, POL_MEDIDOR, "Fin",     4, "fin",    null),
                // Medidor — tareas (cada una en la calle de su departamento)
                flu(FLU_REC,     "Recepción y registro",    POL_MEDIDOR, FORM_MEDIDOR, 1, true, DEP_REC),
                flu(FLU_TEC,     "Evaluación técnica",      POL_MEDIDOR, FORM_EVAL,    2, true, DEP_TEC),
                flu(FLU_CIERREM, "Cierre de trámite",       POL_MEDIDOR, FORM_CIERRE,  3, true, DEP_REC),
                // Crédito — nodos de control
                fluControl(FLU_INI_CR,  POL_CREDITO, "Inicio",    0, "inicio",   null),
                fluControl(FLU_DEC_CR,  POL_CREDITO, "¿Tipo?",    3, "decision", null),
                fluControl(FLU_FIN_CR,  POL_CREDITO, "Fin",       5, "fin",      null),
                // Crédito — tareas
                flu(FLU_DATOS,   "Datos del cliente",  POL_CREDITO, FORM_DATOS,   1, true, DEP_REC),
                flu(FLU_TIPO_CR, "Tipo de crédito",    POL_CREDITO, FORM_CREDITO, 2, true, DEP_REC),
                flu(FLU_VIVIEN,  "Crédito vivienda",   POL_CREDITO, FORM_EVAL,    4, true, DEP_ADM),
                flu(FLU_EMPRESA, "Crédito empresa",    POL_CREDITO, FORM_EVAL,    4, true, DEP_ADM),
                flu(FLU_VEHIC,   "Crédito vehículo",   POL_CREDITO, FORM_EVAL,    4, true, DEP_TEC),
                flu(FLU_CIERREC, "Cierre de crédito",  POL_CREDITO, FORM_CIERRE,  5, true, DEP_REC)
        ));
        log.info("[DataSeeder] Flujos creados: 2 inicio + 2 fin + 1 decision + 9 tareas");

        // ── Flujos para políticas adicionales (3 etapas cada una) ──
        String[] polsExtra = {"pol_003","pol_004","pol_005","pol_006","pol_007","pol_008",
                              "pol_009","pol_010","pol_011","pol_012","pol_013","pol_014",
                              "pol_015","pol_016","pol_017","pol_018","pol_019","pol_020"};
        String[][] etapas = {
            {"Solicitud inicial","Inspección de campo","Aprobación y alta"},
            {"Revisión de deuda","Plan de pago","Confirmación acuerdo"},
            {"Recepción documentos","Verificación legal","Emisión contrato"},
            {"Agendamiento visita","Inspección técnica","Informe de resultados"},
            {"Solicitud de ampliación","Estudio técnico","Instalación y cierre"},
            {"Solicitud de baja","Corte de suministro","Cierre de cuenta"},
            {"Solicitud de reposición","Verificación pago","Reconexión"},
            {"Solicitud hipotecaria","Tasación inmueble","Aprobación crédito"},
            {"Solicitud vehicular","Evaluación crediticia","Desembolso"},
            {"Apertura solicitud","Validación identidad","Alta de cuenta"},
            {"Registro empresa","Verificación fiscal","Activación cuenta"},
            {"Solicitud subsidio","Estudio socioeconómico","Aprobación tarifa"},
            {"Ingreso reclamo","Revisión técnica","Corrección y notificación"},
            {"Solicitud certificado","Verificación saldo","Emisión constancia"},
            {"Solicitud solar","Evaluación técnica","Conexión a red"},
            {"Solicitud traslado","Inspección sitio","Reubicación medidor"},
            {"Solicitud comercial","Estudio carga","Alta suministro"},
            {"Solicitud emergencia","Evaluación situación","Desembolso urgente"},
        };
        String[] deps = {DEP_REC, DEP_TEC, DEP_ADM};
        String[] forms = {FORM_DATOS, FORM_EVAL, FORM_CIERRE};
        List<Flujo> flujoExtra = new ArrayList<>();
        for (int p = 0; p < polsExtra.length; p++) {
            String polId = polsExtra[p];
            flujoExtra.add(fluControl("flu_ini_" + polId, polId, "Inicio", 0, "inicio", null));
            for (int e = 0; e < 3; e++) {
                flujoExtra.add(flu("flu_" + polId + "_" + e, etapas[p][e], polId,
                    forms[e], e + 1, true, deps[e]));
            }
            flujoExtra.add(fluControl("flu_fin_" + polId, polId, "Fin", 4, "fin", null));
        }
        flujoRepository.saveAll(flujoExtra);
        log.info("[DataSeeder] {} flujos adicionales creados", flujoExtra.size());

        // ── Relaciones de flujo ─────────────────────────────────────────────
        flujoRelacionRepository.saveAll(List.of(
                // Medidor — secuencial lineal
                rel("rel_m01", POL_MEDIDOR, FLU_INI_MED, FLU_REC,     null, null, "secuencial"),
                rel("rel_m02", POL_MEDIDOR, FLU_REC,     FLU_TEC,     null, null, "secuencial"),
                rel("rel_m03", POL_MEDIDOR, FLU_TEC,     FLU_CIERREM, null, null, "secuencial"),
                rel("rel_m04", POL_MEDIDOR, FLU_CIERREM, FLU_FIN_MED, null, null, "secuencial"),
                // Crédito — flujo principal
                rel("rel_c01", POL_CREDITO, FLU_INI_CR,  FLU_DATOS,   null, null, "secuencial"),
                rel("rel_c02", POL_CREDITO, FLU_DATOS,   FLU_TIPO_CR, null, null, "secuencial"),
                rel("rel_c03", POL_CREDITO, FLU_TIPO_CR, FLU_DEC_CR,  null, null, "secuencial"),
                // Crédito — ramas desde nodo DECISION según campo del formulario
                rel("rel_c04", POL_CREDITO, FLU_DEC_CR,  FLU_VIVIEN,  "Tipo de crédito", "vivienda", "condicional"),
                rel("rel_c05", POL_CREDITO, FLU_DEC_CR,  FLU_EMPRESA, "Tipo de crédito", "empresa",  "condicional"),
                rel("rel_c06", POL_CREDITO, FLU_DEC_CR,  FLU_VEHIC,   "Tipo de crédito", "vehiculo", "condicional"),
                // Crédito — convergencia de ramas hacia cierre
                rel("rel_c07", POL_CREDITO, FLU_VIVIEN,  FLU_CIERREC, null, null, "secuencial"),
                rel("rel_c08", POL_CREDITO, FLU_EMPRESA, FLU_CIERREC, null, null, "secuencial"),
                rel("rel_c09", POL_CREDITO, FLU_VEHIC,   FLU_CIERREC, null, null, "secuencial"),
                rel("rel_c10", POL_CREDITO, FLU_CIERREC, FLU_FIN_CR,  null, null, "secuencial")
        ));
        log.info("[DataSeeder] Relaciones creadas: 4 secuenciales medidor + 10 crédito");

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

        // ── Trámites históricos para entrenamiento ML (50 registros) ──
        List<Tramite> historicos = new ArrayList<>();
        String[] clientes   = {USR_ROBERTO, USR_LUISA, USR_ANA, USR_SOFIA, USR_ELENA};
        String[] receps     = {USR_MARIA, USR_ANA, USR_SOFIA, USR_ELENA, USR_ROSA};
        String[] politicas  = {POL_MEDIDOR, POL_CREDITO};
        String[] estadosML  = {"completado","completado","completado","completado","cancelado"};
        String[] prioridML  = {"normal","normal","normal","urgente","normal"};
        String[] semafoML   = {"verde","verde","amarillo","amarillo","rojo"};

        for (int i = 0; i < 50; i++) {
            int idx   = i % 5;
            String est = estadosML[idx];
            String pri = prioridML[idx];
            String sem = semafoML[idx];
            LocalDateTime base = ahora.minusDays(5 + (i * 2));
            LocalDateTime fin  = base.plusDays(1 + (i % 4));

            int nAct = 2 + (i % 4);
            List<Actividad> acts = new ArrayList<>();
            for (int j = 0; j < nAct; j++) {
                String actEst = (j < nAct - 1) ? "completado"
                              : ("cancelado".equals(est) && j == nAct - 1) ? "rechazado" : "completado";
                acts.add(act(
                    "act_h" + i + "_" + j,
                    FLU_REC, receps[i % 5], DEP_REC,
                    "Paso " + (j + 1), actEst,
                    base.plusHours(j * 8), base.plusHours(j * 8 + 6 + (i % 3)),
                    actEst.equals("rechazado") ? "Documentación incompleta" : "Completado",
                    datosForm(FORM_MEDIDOR, actEst.equals("rechazado") ? "observado" : "completado",
                        base.plusHours(j * 8 + 6), List.of(
                            dato(CMP_MEDIDOR, "Número de medidor", "MED-H" + (1000 + i)),
                            dato(CMP_UBIC, "Ubicación", "Barrio " + (i % 6) + ", Calle " + i)
                        ), List.of())
                ));
            }

            historicos.add(Tramite.builder()
                .id("tra_h" + String.format("%03d", i))
                .politicaId(politicas[i % 2])
                .clienteId(clientes[i % 5])
                .recepcionistaId(receps[i % 5])
                .empresaId(EMP1)
                .estado(est).prioridad(pri)
                .fecha(base).fechaFin(fin)
                .actividades(acts)
                .build());
        }
        tramiteRepository.saveAll(historicos);
        log.info("[DataSeeder] {} tramites historicos para ML insertados", historicos.size());

        // ── 200 trámites distribuidos (activos + históricos) ──────────────────
        String[] todasPols = {POL_MEDIDOR, POL_CREDITO,
            "pol_003","pol_004","pol_005","pol_006","pol_007","pol_008","pol_009","pol_010",
            "pol_011","pol_012","pol_013","pol_014","pol_015","pol_016","pol_017","pol_018","pol_019","pol_020"};
        String[] todosClientes = {USR_ROBERTO, USR_LUISA, USR_ANA, USR_SOFIA, USR_ELENA};
        String[] todosReceps   = {USR_MARIA, USR_ANA, USR_SOFIA, USR_ELENA, USR_ROSA};

        // Patrones: {estado, prioridad, semaforo}
        String[][] patrones = {
            {"completado","normal","verde"},   {"completado","normal","verde"},
            {"completado","normal","verde"},   {"completado","normal","amarillo"},
            {"completado","urgente","amarillo"},{"cancelado","normal","rojo"},
            {"proceso","normal","verde"},      {"proceso","normal","verde"},
            {"proceso","normal","amarillo"},   {"urgente","urgente","rojo"},
        };

        List<Tramite> masivos = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            String[] pat     = patrones[i % patrones.length];
            String est2      = pat[0];
            String pri2      = pat[1];
            String sem2      = pat[2];
            String polId2    = todasPols[i % todasPols.length];
            String fluBase   = (polId2.equals(POL_MEDIDOR) || polId2.equals(POL_CREDITO))
                               ? (polId2.equals(POL_MEDIDOR) ? FLU_REC : FLU_DATOS)
                               : "flu_" + polId2 + "_0";
            LocalDateTime base2 = ahora.minusDays(1 + (i % 180));
            int nAct2 = 2 + (i % 3);
            boolean esHist   = est2.equals("completado") || est2.equals("cancelado");
            LocalDateTime fin2 = esHist ? base2.plusDays(1 + (i % 5)) : null;

            List<Actividad> actsM = new ArrayList<>();
            for (int j = 0; j < nAct2; j++) {
                boolean esUltimaM = (j == nAct2 - 1);
                String actEstM;
                if (esHist) {
                    actEstM = (est2.equals("cancelado") && esUltimaM) ? "rechazado" : "completado";
                } else {
                    actEstM = esUltimaM ? "activo" : "completado";
                }
                LocalDateTime iniM = base2.plusHours(j * 8L);
                LocalDateTime finM = actEstM.equals("activo") ? null : iniM.plusHours(7);
                actsM.add(act(
                    "act_m" + i + "_" + j,
                    fluBase, todosReceps[i % 5], deps[j % 3],
                    "Etapa " + (j + 1), actEstM,
                    iniM, finM,
                    actEstM.equals("rechazado") ? "Documentación incompleta" : (actEstM.equals("activo") ? null : "OK"),
                    datosForm(forms[j % 3],
                        actEstM.equals("activo") ? "en_proceso" : "completado",
                        iniM.plusHours(6),
                        actEstM.equals("activo") ? List.of() : List.of(
                            dato(CMP_NOMBRE, "Nombre", "Cliente " + i),
                            dato(CMP_CI, "CI", String.valueOf(3000000 + i))
                        ), List.of())
                ));
            }

            masivos.add(Tramite.builder()
                .id("tra_m" + String.format("%03d", i + 1))
                .politicaId(polId2)
                .clienteId(todosClientes[i % 5])
                .recepcionistaId(todosReceps[i % 5])
                .empresaId(EMP1)
                .estado(est2).prioridad(pri2)
                .fecha(base2).fechaFin(fin2)
                .actividades(actsM)
                .build());
        }
        tramiteRepository.saveAll(masivos);
        log.info("[DataSeeder] {} tramites masivos insertados", masivos.size());

        // ── Trámites activos adicionales para el dashboard (20 registros) ──
        List<Tramite> activos = new ArrayList<>();

        // Configuraciones: {estado, prioridad, semaforo, nActividades, tieneRechazo}
        Object[][] cfgs = {
            {"proceso",  "normal",  "verde",    2, false},
            {"proceso",  "normal",  "verde",    3, false},
            {"proceso",  "normal",  "amarillo", 3, false},
            {"proceso",  "urgente", "amarillo", 4, true },
            {"urgente",  "urgente", "rojo",     4, true },
            {"proceso",  "normal",  "verde",    2, false},
            {"proceso",  "normal",  "amarillo", 3, true },
            {"urgente",  "urgente", "rojo",     5, true },
            {"proceso",  "normal",  "verde",    2, false},
            {"proceso",  "normal",  "verde",    3, false},
            {"proceso",  "urgente", "amarillo", 4, true },
            {"urgente",  "urgente", "rojo",     3, true },
            {"proceso",  "normal",  "verde",    2, false},
            {"proceso",  "normal",  "amarillo", 4, false},
            {"proceso",  "normal",  "verde",    2, false},
            {"urgente",  "urgente", "rojo",     5, true },
            {"proceso",  "normal",  "amarillo", 3, true },
            {"proceso",  "normal",  "verde",    2, false},
            {"proceso",  "urgente", "amarillo", 3, true },
            {"urgente",  "urgente", "rojo",     4, true },
        };

        String[] nombresClientes = {"Carlos", "Ana", "Pedro", "Laura", "Miguel",
                                    "Diana",  "Luis", "Sofia", "Marco", "Elena",
                                    "Rosa",   "Juan", "Carla", "Mario", "Paula",
                                    "Tomas",  "Nina", "Felix", "Dora",  "Hugo"};
        String[] apellidos       = {"Roca","Vega","Cruz","Lima","Soto",
                                    "Ríos","Alba","Mora","Páez","Cano",
                                    "Lara","Ruiz","Díaz","Mena","Pino",
                                    "Leiva","Fuen","Nava","Ossa","Bravo"};

        for (int i = 0; i < 20; i++) {
            Object[] c      = cfgs[i];
            String est      = (String) c[0];
            String pri      = (String) c[1];
            String sem      = (String) c[2];
            int nAct        = (int)    c[3];
            boolean rechazo = (boolean)c[4];
            LocalDateTime base = ahora.minusHours(6 + i * 3L);

            List<Actividad> acts = new ArrayList<>();
            for (int j = 0; j < nAct; j++) {
                boolean esUltima  = (j == nAct - 1);
                String actEst     = esUltima ? "activo"
                                  : (rechazo && j == nAct - 2) ? "rechazado" : "completado";
                LocalDateTime ini = base.plusHours(j * 6L);
                LocalDateTime fin2 = actEst.equals("activo") ? null : ini.plusHours(5);
                acts.add(act(
                    "act_a" + i + "_" + j,
                    j % 2 == 0 ? FLU_REC : FLU_TEC,
                    receps[i % 5],
                    j % 2 == 0 ? DEP_REC : DEP_TEC,
                    "Etapa " + (j + 1), actEst,
                    ini, fin2,
                    actEst.equals("rechazado") ? "Documentos faltantes" : (actEst.equals("activo") ? null : "OK"),
                    datosForm(j % 2 == 0 ? FORM_MEDIDOR : FORM_EVAL,
                        actEst.equals("activo") ? "en_proceso" : "completado",
                        ini.plusHours(4),
                        actEst.equals("activo") ? List.of() : List.of(
                            dato(CMP_MEDIDOR, "Número de medidor", "MED-A" + (2000 + i * 10 + j)),
                            dato(CMP_UBIC, "Ubicación", nombresClientes[i] + " " + apellidos[i] + ", zona " + (i % 5))
                        ), List.of())
                ));
            }

            activos.add(Tramite.builder()
                .id("tra_a" + String.format("%02d", i + 1))
                .politicaId(i % 3 == 0 ? POL_CREDITO : POL_MEDIDOR)
                .clienteId(clientes[i % 5])
                .recepcionistaId(receps[i % 5])
                .empresaId(EMP1)
                .estado(est).prioridad(pri)
                .fecha(base)
                .actividades(acts)
                .build());
        }
        tramiteRepository.saveAll(activos);
        log.info("[DataSeeder] {} tramites activos para dashboard insertados", activos.size());

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

        // ── Documentos (Feature 3) ────────────────────────────
        documentoRepositorio.saveAll(List.of(
            Documento.builder()
                .id("doc_001").nombre("Acta de inspección TRA-001.pdf").tipo("pdf")
                .tamanio(204800L).empresaId(EMP1).tramiteId(TRA1).actividadId("act_002")
                .claveAlmacenamiento("uploads/emp_001/tra_001/acta_inspeccion.pdf")
                .version("1.0").creadoPor(USR_CARLOS).fechaCreacion(h6).activo(true)
                .descripcion("Acta de la inspección técnica preliminar").build(),
            Documento.builder()
                .id("doc_002").nombre("Acta de cierre TRA-002.pdf").tipo("pdf")
                .tamanio(153600L).empresaId(EMP1).tramiteId(TRA2).actividadId("act_005")
                .claveAlmacenamiento("uploads/emp_001/tra_002/acta_cierre.pdf")
                .version("1.0").creadoPor(USR_MARIA).fechaCreacion(h12).activo(true)
                .descripcion("Documento de cierre del trámite completado").build()
        ));

        permisoDocumentoRepositorio.saveAll(List.of(
            PermisoDocumento.builder().documentoId("doc_001").usuarioId(USR_MARIA).nivel("editor").otorgadoPor(USR_CARLOS).build(),
            PermisoDocumento.builder().documentoId("doc_001").usuarioId(USR_JORGE).nivel("visualizador").otorgadoPor(USR_CARLOS).build(),
            PermisoDocumento.builder().documentoId("doc_002").usuarioId(USR_MARIA).nivel("editor").otorgadoPor(USR_MARIA).build(),
            PermisoDocumento.builder().documentoId("doc_002").usuarioId(USR_CARLOS).nivel("visualizador").otorgadoPor(USR_MARIA).build()
        ));

        bitacoraDocumentoRepositorio.saveAll(List.of(
            BitacoraDocumento.builder().documentoId("doc_001").usuarioId(USR_CARLOS).accion("subio").detalle("Subió versión 1.0").fecha(h6).build(),
            BitacoraDocumento.builder().documentoId("doc_001").usuarioId(USR_MARIA).accion("visualizo").detalle("Visualizó el documento").fecha(h2).build(),
            BitacoraDocumento.builder().documentoId("doc_002").usuarioId(USR_MARIA).accion("subio").detalle("Subió versión 1.0").fecha(h12).build(),
            BitacoraDocumento.builder().documentoId("doc_002").usuarioId(USR_JORGE).accion("descargo").detalle("Descargó el documento").fecha(h6).build()
        ));
        log.info("[DataSeeder] Documentos, permisos y bitácora documental creados");

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

    /** Distribuye los componentes en 2 columnas dentro del lienzo (0..900px) para que el editor visual arranque con un layout legible */
    private Componente cmp(String id, String tipoId, String etiqueta, String placeholder,
                           int orden, boolean requerido, List<String> opciones) {
        int fila = (orden - 1) / 2;
        int columna = (orden - 1) % 2;
        return Componente.builder().id(id).tipoId(tipoId).etiqueta(etiqueta)
                .placeholder(placeholder).orden(orden)
                .posicionX(columna * 340 + 20).posicionY(fila * 160 + 20)
                .requerido(requerido).opciones(opciones).build();
    }

    /** Crea un nodo de tarea (swimlane = departamento, tipo = "tarea") */
    private Flujo flu(String id, String nombre, String politicaId, String formularioId,
                      int orden, boolean obligatorio, String depId) {
        return Flujo.builder().id(id).nombre(nombre).politicaId(politicaId)
                .formularioId(formularioId).orden(orden).esObligatorio(obligatorio)
                .departamentoId(depId).tipoNodo("tarea").build();
    }

    /** Crea un nodo de control UML (inicio, fin, decision, fork, join) — sin formulario ni departamento obligatorio */
    private Flujo fluControl(String id, String politicaId, String nombre, int orden, String tipoNodo, String depId) {
        return Flujo.builder().id(id).nombre(nombre).politicaId(politicaId)
                .orden(orden).esObligatorio(false).departamentoId(depId).tipoNodo(tipoNodo).build();
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
