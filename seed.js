// ============================================================
// Script de datos sintéticos para workflow_dev
// Ejecutar con: mongosh workflow_dev seed.js
// ============================================================

const DB_NAME = "workflow_dev";
db = db.getSiblingDB(DB_NAME);

// Limpia todas las colecciones antes de insertar
const colecciones = [
  "empresas", "departamentos", "roles", "usuarios", "tipo_componentes",
  "formularios", "politicas_negocio", "flujos", "flujo_relaciones",
  "tramites", "bitacora", "notificaciones",
  "documentos", "permisos_documentos", "bitacora_documentos"
];
colecciones.forEach(c => { try { db[c].drop(); } catch(e) {} print("Colección limpiada: " + c); });

// ─── IDs fijos — deben coincidir con DataSeeder.java ────────
const IDS = {
  empresa1: "emp_001",

  // 7 departamentos
  depRec1:  "dep_001",
  depTec:   "dep_002",
  depAdm:   "dep_003",
  depRec2:  "dep_004",
  depRec3:  "dep_005",
  depRec4:  "dep_006",
  depRec5:  "dep_007",

  rolCliente:       "rol_001",
  rolRecepcionista: "rol_002",
  rolAdmin:         "rol_003",

  // 9 usuarios: 2 admin + 2 clientes + 5 recepcionistas
  usrJorge:  "usr_001",   // admin1@crebolivia.com
  usrCarlos: "usr_002",   // admin2@crebolivia.com
  usrRoberto:"usr_003",   // cliente1@crebolivia.com
  usrLuisa:  "usr_004",   // cliente2@crebolivia.com
  usrMaria:  "usr_005",   // recepcion1@crebolivia.com
  usrAna:    "usr_006",   // recepcion2@crebolivia.com
  usrSofia:  "usr_007",   // recepcion3@crebolivia.com
  usrElena:  "usr_008",   // recepcion4@crebolivia.com
  usrRosa:   "usr_009",   // recepcion5@crebolivia.com

  tcTexto:    "tc_001",
  tcTextarea: "tc_002",
  tcSelect:   "tc_003",
  tcBoolean:  "tc_004",
  tcFecha:    "tc_005",
  tcArchivo:  "tc_006",
  tcNumero:   "tc_007",

  formDatos:   "form_001",
  formCredito: "form_002",
  formEval:    "form_003",
  formCierre:  "form_004",
  formMedidor: "form_005",

  cmpNombre:   "cmp_001",
  cmpCI:       "cmp_002",
  cmpTel:      "cmp_003",
  cmpDir:      "cmp_004",
  cmpTipoCr:   "cmp_005",
  cmpMonto:    "cmp_006",
  cmpObs:      "cmp_007",
  cmpAprobado: "cmp_008",
  cmpDoc:      "cmp_009",
  cmpMedidor:  "cmp_010",
  cmpUbic:     "cmp_011",

  polMedidor: "pol_001",
  polCredito: "pol_002",

  // Flujos — nodos UML 2.5
  fluIniMed:  "flu_000",   // inicio medidor
  fluRec:     "flu_001",   // tarea recepción
  fluTec:     "flu_002",   // tarea técnico
  fluCierreM: "flu_003",   // tarea cierre medidor
  fluFinMed:  "flu_004",   // fin medidor
  fluIniCr:   "flu_009",   // inicio crédito
  fluDatos:   "flu_010",   // tarea datos cliente
  fluTipoCr:  "flu_011",   // tarea tipo crédito
  fluDecCr:   "flu_011b",  // nodo decision crédito
  fluVivien:  "flu_012",   // tarea crédito vivienda
  fluEmpresa: "flu_013",   // tarea crédito empresa
  fluVehic:   "flu_014",   // tarea crédito vehículo
  fluCierreC: "flu_015",   // tarea cierre crédito
  fluFinCr:   "flu_016",   // fin crédito

  tramite1: "tra_001",
  tramite2: "tra_002",
  tramite3: "tra_003",
  tramite4: "tra_004",
  tramite5: "tra_005",

  doc1: "doc_001",
  doc2: "doc_002",
};

const ahora = new Date();
const h1  = new Date(ahora - 1  * 3600000);
const h2  = new Date(ahora - 2  * 3600000);
const h6  = new Date(ahora - 6  * 3600000);
const h12 = new Date(ahora - 12 * 3600000);
const h24 = new Date(ahora - 24 * 3600000);
const h48 = new Date(ahora - 48 * 3600000);

// ─── EMPRESA ────────────────────────────────────────────────
db.empresas.insertOne({
  _id: IDS.empresa1,
  nombre: "CRE Bolivia",
  nit: "1234567890",
  direccion: "Av. Cañoto 1234, Santa Cruz de la Sierra",
  telefono: "591-3-3333333",
  correo: "info@crebolivia.com",
  activo: true
});
print("✓ Empresa");

// ─── DEPARTAMENTOS ──────────────────────────────────────────
db.departamentos.insertMany([
  { _id: IDS.depRec1,  nombre: "Recepción 1",   descripcion: "Recepción — ventanilla 1",  empresaId: IDS.empresa1, activo: true },
  { _id: IDS.depTec,   nombre: "Técnico",        descripcion: "Inspecciones y evaluaciones", empresaId: IDS.empresa1, activo: true },
  { _id: IDS.depAdm,   nombre: "Administración", descripcion: "Gestión y administración",  empresaId: IDS.empresa1, activo: true },
  { _id: IDS.depRec2,  nombre: "Recepción 2",   descripcion: "Recepción — ventanilla 2",  empresaId: IDS.empresa1, activo: true },
  { _id: IDS.depRec3,  nombre: "Recepción 3",   descripcion: "Recepción — ventanilla 3",  empresaId: IDS.empresa1, activo: true },
  { _id: IDS.depRec4,  nombre: "Recepción 4",   descripcion: "Recepción — ventanilla 4",  empresaId: IDS.empresa1, activo: true },
  { _id: IDS.depRec5,  nombre: "Recepción 5",   descripcion: "Recepción — ventanilla 5",  empresaId: IDS.empresa1, activo: true },
]);
print("✓ Departamentos (7)");

// ─── ROLES ──────────────────────────────────────────────────
db.roles.insertMany([
  { _id: IDS.rolCliente,       nombre: "cliente",       descripcion: "Ciudadano que solicita un trámite" },
  { _id: IDS.rolRecepcionista, nombre: "recepcionista", descripcion: "Recibe y gestiona trámites" },
  { _id: IDS.rolAdmin,         nombre: "administrador", descripcion: "Administrador con acceso completo" },
]);
print("✓ Roles");

// ─── USUARIOS (password = BCrypt de "123456") ────────────────
const passHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
db.usuarios.insertMany([
  // 2 administradores
  { _id: IDS.usrJorge,  nombre: "Jorge",    apellido: "López",   correo: "admin1@crebolivia.com",     password: passHash, rolId: IDS.rolAdmin,         empresaId: IDS.empresa1, departamentoId: IDS.depAdm,  activo: true },
  { _id: IDS.usrCarlos, nombre: "Carlos",   apellido: "Pérez",   correo: "admin2@crebolivia.com",     password: passHash, rolId: IDS.rolAdmin,         empresaId: IDS.empresa1, departamentoId: IDS.depAdm,  activo: true },
  // 2 clientes
  { _id: IDS.usrRoberto,nombre: "Roberto",  apellido: "Flores",  correo: "cliente1@crebolivia.com",   password: passHash, rolId: IDS.rolCliente,       empresaId: IDS.empresa1, departamentoId: null,        activo: true },
  { _id: IDS.usrLuisa,  nombre: "Luisa",    apellido: "Méndez",  correo: "cliente2@crebolivia.com",   password: passHash, rolId: IDS.rolCliente,       empresaId: IDS.empresa1, departamentoId: null,        activo: true },
  // 5 recepcionistas
  { _id: IDS.usrMaria,  nombre: "María",    apellido: "García",  correo: "recepcion1@crebolivia.com", password: passHash, rolId: IDS.rolRecepcionista, empresaId: IDS.empresa1, departamentoId: IDS.depRec1, activo: true },
  { _id: IDS.usrAna,    nombre: "Ana",      apellido: "Torres",  correo: "recepcion2@crebolivia.com", password: passHash, rolId: IDS.rolRecepcionista, empresaId: IDS.empresa1, departamentoId: IDS.depRec2, activo: true },
  { _id: IDS.usrSofia,  nombre: "Sofía",    apellido: "Vargas",  correo: "recepcion3@crebolivia.com", password: passHash, rolId: IDS.rolRecepcionista, empresaId: IDS.empresa1, departamentoId: IDS.depRec3, activo: true },
  { _id: IDS.usrElena,  nombre: "Elena",    apellido: "Rojas",   correo: "recepcion4@crebolivia.com", password: passHash, rolId: IDS.rolRecepcionista, empresaId: IDS.empresa1, departamentoId: IDS.depRec4, activo: true },
  { _id: IDS.usrRosa,   nombre: "Rosa",     apellido: "Mamani",  correo: "recepcion5@crebolivia.com", password: passHash, rolId: IDS.rolRecepcionista, empresaId: IDS.empresa1, departamentoId: IDS.depRec5, activo: true },
]);
print("✓ Usuarios (9: 2 admin + 2 clientes + 5 recepcionistas)");

// ─── TIPO COMPONENTES ────────────────────────────────────────
db.tipo_componentes.insertMany([
  { _id: IDS.tcTexto,    nombre: "texto",    descripcion: "Campo de texto corto" },
  { _id: IDS.tcTextarea, nombre: "textarea", descripcion: "Área de texto largo" },
  { _id: IDS.tcSelect,   nombre: "select",   descripcion: "Lista desplegable" },
  { _id: IDS.tcBoolean,  nombre: "boolean",  descripcion: "Casilla verdadero/falso" },
  { _id: IDS.tcFecha,    nombre: "fecha",    descripcion: "Selector de fecha" },
  { _id: IDS.tcArchivo,  nombre: "archivo",  descripcion: "Carga de archivo" },
  { _id: IDS.tcNumero,   nombre: "numero",   descripcion: "Campo numérico" },
]);
print("✓ TipoComponentes");

// ─── FORMULARIOS ─────────────────────────────────────────────
db.formularios.insertMany([
  {
    _id: IDS.formDatos, nombre: "Datos del cliente",
    descripcion: "Información básica del solicitante", empresaId: IDS.empresa1, activo: true,
    componentes: [
      { id: IDS.cmpNombre,  tipoId: IDS.tcTexto,    etiqueta: "Nombre completo",     placeholder: "Ej: Juan Pérez",        orden: 1, posicionX: 0, posicionY: 0, requerido: true,  opciones: null },
      { id: IDS.cmpCI,      tipoId: IDS.tcNumero,   etiqueta: "Cédula de identidad", placeholder: "Ej: 12345678",          orden: 2, posicionX: 0, posicionY: 1, requerido: true,  opciones: null },
      { id: IDS.cmpTel,     tipoId: IDS.tcTexto,    etiqueta: "Teléfono",            placeholder: "Ej: 70000000",          orden: 3, posicionX: 0, posicionY: 2, requerido: true,  opciones: null },
      { id: IDS.cmpDir,     tipoId: IDS.tcTextarea, etiqueta: "Dirección",           placeholder: "Calle, barrio, ciudad", orden: 4, posicionX: 0, posicionY: 3, requerido: true,  opciones: null },
    ]
  },
  {
    _id: IDS.formCredito, nombre: "Tipo de crédito",
    descripcion: "Selección del tipo de crédito solicitado", empresaId: IDS.empresa1, activo: true,
    componentes: [
      { id: IDS.cmpTipoCr, tipoId: IDS.tcSelect, etiqueta: "Tipo de crédito",       placeholder: "Seleccione...", orden: 1, posicionX: 0, posicionY: 0, requerido: true, opciones: ["vivienda","empresa","vehiculo"] },
      { id: IDS.cmpMonto,  tipoId: IDS.tcNumero, etiqueta: "Monto solicitado (Bs)", placeholder: "Ej: 50000",    orden: 2, posicionX: 0, posicionY: 1, requerido: true, opciones: null },
    ]
  },
  {
    _id: IDS.formEval, nombre: "Evaluación",
    descripcion: "Resultados de la evaluación", empresaId: IDS.empresa1, activo: true,
    componentes: [
      { id: IDS.cmpObs,      tipoId: IDS.tcTextarea, etiqueta: "Observaciones", placeholder: "Describa los hallazgos...", orden: 1, posicionX: 0, posicionY: 0, requerido: false, opciones: null },
      { id: IDS.cmpAprobado, tipoId: IDS.tcBoolean,  etiqueta: "Aprobado",      placeholder: "",                          orden: 2, posicionX: 0, posicionY: 1, requerido: true,  opciones: null },
    ]
  },
  {
    _id: IDS.formCierre, nombre: "Cierre de trámite",
    descripcion: "Documentación final para cerrar el trámite", empresaId: IDS.empresa1, activo: true,
    componentes: [
      { id: IDS.cmpDoc, tipoId: IDS.tcArchivo, etiqueta: "Documento de cierre", placeholder: "", orden: 1, posicionX: 0, posicionY: 0, requerido: true, opciones: null },
    ]
  },
  {
    _id: IDS.formMedidor, nombre: "Solicitud de medidor",
    descripcion: "Datos para instalación de medidor eléctrico", empresaId: IDS.empresa1, activo: true,
    componentes: [
      { id: IDS.cmpMedidor, tipoId: IDS.tcTexto,    etiqueta: "Número de medidor actual",       placeholder: "Ej: MED-12345",           orden: 1, posicionX: 0, posicionY: 0, requerido: false, opciones: null },
      { id: IDS.cmpUbic,    tipoId: IDS.tcTextarea, etiqueta: "Ubicación exacta del domicilio", placeholder: "Barrio, calle, número...", orden: 2, posicionX: 0, posicionY: 1, requerido: true,  opciones: null },
    ]
  },
]);
print("✓ Formularios (5)");

// ─── POLÍTICAS ───────────────────────────────────────────────
db.politicas_negocio.insertMany([
  {
    _id: IDS.polMedidor, nombre: "Solicitud de medidor eléctrico",
    descripcion: "Proceso para instalación de medidor residencial",
    tipo: "secuencial", estado: "activa", version: 1,
    empresaId: IDS.empresa1, creadoPorId: IDS.usrJorge
  },
  {
    _id: IDS.polCredito, nombre: "Solicitud de crédito",
    descripcion: "Evaluación con flujos condicionales por tipo de crédito",
    tipo: "secuencial", estado: "activa", version: 1,
    empresaId: IDS.empresa1, creadoPorId: IDS.usrJorge
  },
]);
print("✓ Políticas (2)");

// ─── FLUJOS — nodos UML 2.5 (inicio/fin/decision) + tareas ──
db.flujos.insertMany([
  // Medidor — nodos de control
  { _id: IDS.fluIniMed,  nombre: "Inicio",               politicaId: IDS.polMedidor, formularioId: null,          orden: 0, esObligatorio: false, departamentoId: null,       tipoNodo: "inicio"   },
  { _id: IDS.fluFinMed,  nombre: "Fin",                  politicaId: IDS.polMedidor, formularioId: null,          orden: 4, esObligatorio: false, departamentoId: null,       tipoNodo: "fin"      },
  // Medidor — tareas
  { _id: IDS.fluRec,     nombre: "Recepción y registro", politicaId: IDS.polMedidor, formularioId: IDS.formMedidor, orden: 1, esObligatorio: true, departamentoId: IDS.depRec1, tipoNodo: "tarea" },
  { _id: IDS.fluTec,     nombre: "Evaluación técnica",   politicaId: IDS.polMedidor, formularioId: IDS.formEval,    orden: 2, esObligatorio: true, departamentoId: IDS.depTec,  tipoNodo: "tarea" },
  { _id: IDS.fluCierreM, nombre: "Cierre de trámite",    politicaId: IDS.polMedidor, formularioId: IDS.formCierre,  orden: 3, esObligatorio: true, departamentoId: IDS.depRec1, tipoNodo: "tarea" },

  // Crédito — nodos de control
  { _id: IDS.fluIniCr,  nombre: "Inicio",  politicaId: IDS.polCredito, formularioId: null, orden: 0, esObligatorio: false, departamentoId: null, tipoNodo: "inicio"   },
  { _id: IDS.fluDecCr,  nombre: "¿Tipo?",  politicaId: IDS.polCredito, formularioId: null, orden: 3, esObligatorio: false, departamentoId: null, tipoNodo: "decision" },
  { _id: IDS.fluFinCr,  nombre: "Fin",     politicaId: IDS.polCredito, formularioId: null, orden: 5, esObligatorio: false, departamentoId: null, tipoNodo: "fin"      },
  // Crédito — tareas
  { _id: IDS.fluDatos,   nombre: "Datos del cliente", politicaId: IDS.polCredito, formularioId: IDS.formDatos,   orden: 1, esObligatorio: true, departamentoId: IDS.depRec1, tipoNodo: "tarea" },
  { _id: IDS.fluTipoCr,  nombre: "Tipo de crédito",  politicaId: IDS.polCredito, formularioId: IDS.formCredito, orden: 2, esObligatorio: true, departamentoId: IDS.depRec1, tipoNodo: "tarea" },
  { _id: IDS.fluVivien,  nombre: "Crédito vivienda",  politicaId: IDS.polCredito, formularioId: IDS.formEval,    orden: 4, esObligatorio: true, departamentoId: IDS.depAdm,  tipoNodo: "tarea" },
  { _id: IDS.fluEmpresa, nombre: "Crédito empresa",   politicaId: IDS.polCredito, formularioId: IDS.formEval,    orden: 4, esObligatorio: true, departamentoId: IDS.depAdm,  tipoNodo: "tarea" },
  { _id: IDS.fluVehic,   nombre: "Crédito vehículo",  politicaId: IDS.polCredito, formularioId: IDS.formEval,    orden: 4, esObligatorio: true, departamentoId: IDS.depTec,  tipoNodo: "tarea" },
  { _id: IDS.fluCierreC, nombre: "Cierre de crédito", politicaId: IDS.polCredito, formularioId: IDS.formCierre,  orden: 5, esObligatorio: true, departamentoId: IDS.depRec1, tipoNodo: "tarea" },
]);
print("✓ Flujos (14: 2 inicio + 2 fin + 1 decision + 9 tareas)");

// ─── RELACIONES ENTRE FLUJOS ─────────────────────────────────
db.flujo_relaciones.insertMany([
  // Medidor — secuencial lineal
  { _id: "rel_m01", politicaId: IDS.polMedidor, padreId: IDS.fluIniMed,  hijoId: IDS.fluRec,     condicionCampo: null, condicionValor: null, tipo: "secuencial" },
  { _id: "rel_m02", politicaId: IDS.polMedidor, padreId: IDS.fluRec,     hijoId: IDS.fluTec,     condicionCampo: null, condicionValor: null, tipo: "secuencial" },
  { _id: "rel_m03", politicaId: IDS.polMedidor, padreId: IDS.fluTec,     hijoId: IDS.fluCierreM, condicionCampo: null, condicionValor: null, tipo: "secuencial" },
  { _id: "rel_m04", politicaId: IDS.polMedidor, padreId: IDS.fluCierreM, hijoId: IDS.fluFinMed,  condicionCampo: null, condicionValor: null, tipo: "secuencial" },

  // Crédito — flujo principal hasta nodo decision
  { _id: "rel_c01", politicaId: IDS.polCredito, padreId: IDS.fluIniCr,  hijoId: IDS.fluDatos,   condicionCampo: null, condicionValor: null, tipo: "secuencial" },
  { _id: "rel_c02", politicaId: IDS.polCredito, padreId: IDS.fluDatos,   hijoId: IDS.fluTipoCr,  condicionCampo: null, condicionValor: null, tipo: "secuencial" },
  { _id: "rel_c03", politicaId: IDS.polCredito, padreId: IDS.fluTipoCr,  hijoId: IDS.fluDecCr,   condicionCampo: null, condicionValor: null, tipo: "secuencial" },
  // Crédito — ramas condicionales desde nodo decision
  { _id: "rel_c04", politicaId: IDS.polCredito, padreId: IDS.fluDecCr,   hijoId: IDS.fluVivien,  condicionCampo: "Tipo de crédito", condicionValor: "vivienda", tipo: "condicional" },
  { _id: "rel_c05", politicaId: IDS.polCredito, padreId: IDS.fluDecCr,   hijoId: IDS.fluEmpresa, condicionCampo: "Tipo de crédito", condicionValor: "empresa",  tipo: "condicional" },
  { _id: "rel_c06", politicaId: IDS.polCredito, padreId: IDS.fluDecCr,   hijoId: IDS.fluVehic,   condicionCampo: "Tipo de crédito", condicionValor: "vehiculo", tipo: "condicional" },
  // Crédito — convergencia de ramas al cierre
  { _id: "rel_c07", politicaId: IDS.polCredito, padreId: IDS.fluVivien,  hijoId: IDS.fluCierreC, condicionCampo: null, condicionValor: null, tipo: "secuencial" },
  { _id: "rel_c08", politicaId: IDS.polCredito, padreId: IDS.fluEmpresa, hijoId: IDS.fluCierreC, condicionCampo: null, condicionValor: null, tipo: "secuencial" },
  { _id: "rel_c09", politicaId: IDS.polCredito, padreId: IDS.fluVehic,   hijoId: IDS.fluCierreC, condicionCampo: null, condicionValor: null, tipo: "secuencial" },
  { _id: "rel_c10", politicaId: IDS.polCredito, padreId: IDS.fluCierreC, hijoId: IDS.fluFinCr,   condicionCampo: null, condicionValor: null, tipo: "secuencial" },
]);
print("✓ Relaciones (14: 4 medidor + 10 crédito)");

// ─── TRÁMITES ────────────────────────────────────────────────
db.tramites.insertMany([

  // TRA-001: Roberto — medidor — EN PROCESO
  {
    _id: IDS.tramite1,
    politicaId: IDS.polMedidor, clienteId: IDS.usrRoberto,
    recepcionistaId: IDS.usrMaria, empresaId: IDS.empresa1,
    estado: "proceso", prioridad: "normal",
    actividades: [
      {
        id: "act_001", flujoId: IDS.fluRec, usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRec1, nombre: "Recepción y registro",
        estado: "completado", fechaInicio: h24, fechaFin: h12,
        observacion: "Documentos recibidos correctamente",
        datosForm: {
          formularioId: IDS.formMedidor, estado: "completado", fecha: h12,
          datos: [
            { componenteId: IDS.cmpMedidor, etiqueta: "Número de medidor actual",       valor: "MED-78432" },
            { componenteId: IDS.cmpUbic,    etiqueta: "Ubicación exacta del domicilio", valor: "Equipetrol, Calle 3 Este #456" },
          ],
          observaciones: []
        }
      },
      {
        id: "act_002", flujoId: IDS.fluTec, usuarioId: IDS.usrCarlos,
        departamentoId: IDS.depTec, nombre: "Evaluación técnica",
        estado: "activo", fechaInicio: h12, fechaFin: null, observacion: null,
        datosForm: {
          formularioId: IDS.formEval, estado: "en_proceso", fecha: h6,
          datos: [
            { componenteId: IDS.cmpObs, etiqueta: "Observaciones", valor: "Instalación requiere cable de 10mm" },
          ],
          observaciones: []
        }
      }
    ],
    fecha: h24, fechaFin: null
  },

  // TRA-002: Luisa — medidor — COMPLETADO
  {
    _id: IDS.tramite2,
    politicaId: IDS.polMedidor, clienteId: IDS.usrLuisa,
    recepcionistaId: IDS.usrMaria, empresaId: IDS.empresa1,
    estado: "completado", prioridad: "normal",
    actividades: [
      {
        id: "act_003", flujoId: IDS.fluRec, usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRec1, nombre: "Recepción y registro",
        estado: "completado", fechaInicio: h48, fechaFin: h48,
        observacion: "Datos completos",
        datosForm: {
          formularioId: IDS.formMedidor, estado: "completado", fecha: h48,
          datos: [
            { componenteId: IDS.cmpMedidor, etiqueta: "Número de medidor actual",       valor: "MED-11111" },
            { componenteId: IDS.cmpUbic,    etiqueta: "Ubicación exacta del domicilio", valor: "Urbarí, Calle Las Palmas #123" },
          ],
          observaciones: []
        }
      },
      {
        id: "act_004", flujoId: IDS.fluTec, usuarioId: IDS.usrCarlos,
        departamentoId: IDS.depTec, nombre: "Evaluación técnica",
        estado: "completado", fechaInicio: h48, fechaFin: h24,
        observacion: "Instalación viable",
        datosForm: {
          formularioId: IDS.formEval, estado: "completado", fecha: h24,
          datos: [
            { componenteId: IDS.cmpObs,      etiqueta: "Observaciones", valor: "Sin observaciones, instalación estándar" },
            { componenteId: IDS.cmpAprobado, etiqueta: "Aprobado",      valor: "true" },
          ],
          observaciones: []
        }
      },
      {
        id: "act_005", flujoId: IDS.fluCierreM, usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRec1, nombre: "Cierre de trámite",
        estado: "completado", fechaInicio: h24, fechaFin: h12,
        observacion: "Trámite cerrado correctamente",
        datosForm: {
          formularioId: IDS.formCierre, estado: "completado", fecha: h12,
          datos: [
            { componenteId: IDS.cmpDoc, etiqueta: "Documento de cierre", valor: "acta_cierre_tra002.pdf" },
          ],
          observaciones: []
        }
      }
    ],
    fecha: h48, fechaFin: h12
  },

  // TRA-003: Roberto — medidor — URGENTE (recepcionista Ana)
  {
    _id: IDS.tramite3,
    politicaId: IDS.polMedidor, clienteId: IDS.usrRoberto,
    recepcionistaId: IDS.usrAna, empresaId: IDS.empresa1,
    estado: "urgente", prioridad: "urgente",
    actividades: [
      {
        id: "act_006", flujoId: IDS.fluRec, usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRec1, nombre: "Recepción y registro",
        estado: "completado", fechaInicio: h48, fechaFin: h48, observacion: null,
        datosForm: {
          formularioId: IDS.formMedidor, estado: "completado", fecha: h48,
          datos: [
            { componenteId: IDS.cmpMedidor, etiqueta: "Número de medidor actual",       valor: "MED-99999" },
            { componenteId: IDS.cmpUbic,    etiqueta: "Ubicación exacta del domicilio", valor: "Plan Tres Mil, Av. Principal #789" },
          ],
          observaciones: []
        }
      },
      {
        id: "act_007", flujoId: IDS.fluTec, usuarioId: IDS.usrCarlos,
        departamentoId: IDS.depTec, nombre: "Evaluación técnica",
        estado: "activo", fechaInicio: h48, fechaFin: null, observacion: null,
        datosForm: {
          formularioId: IDS.formEval, estado: "en_proceso", fecha: h48,
          datos: [],
          observaciones: [
            { usuarioId: IDS.usrMaria, descripcion: "Por favor priorizar este trámite", estado: "observado", fecha: h6 }
          ]
        }
      }
    ],
    fecha: h48, fechaFin: null
  },

  // TRA-004: Roberto — crédito — EN PROCESO
  {
    _id: IDS.tramite4,
    politicaId: IDS.polCredito, clienteId: IDS.usrRoberto,
    recepcionistaId: IDS.usrMaria, empresaId: IDS.empresa1,
    estado: "proceso", prioridad: "normal",
    actividades: [
      {
        id: "act_008", flujoId: IDS.fluDatos, usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRec1, nombre: "Datos del cliente",
        estado: "completado", fechaInicio: h6, fechaFin: h2, observacion: null,
        datosForm: {
          formularioId: IDS.formDatos, estado: "completado", fecha: h2,
          datos: [
            { componenteId: IDS.cmpNombre, etiqueta: "Nombre completo",     valor: "Roberto Flores Suárez" },
            { componenteId: IDS.cmpCI,     etiqueta: "Cédula de identidad", valor: "5678901" },
            { componenteId: IDS.cmpTel,    etiqueta: "Teléfono",            valor: "76543210" },
            { componenteId: IDS.cmpDir,    etiqueta: "Dirección",           valor: "Av. Brasil #1200, Santa Cruz" },
          ],
          observaciones: []
        }
      },
      {
        id: "act_009", flujoId: IDS.fluTipoCr, usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRec1, nombre: "Tipo de crédito",
        estado: "activo", fechaInicio: h2, fechaFin: null, observacion: null,
        datosForm: { formularioId: IDS.formCredito, estado: "en_proceso", fecha: h2, datos: [], observaciones: [] }
      }
    ],
    fecha: h6, fechaFin: null
  },

  // TRA-005: Luisa — crédito — rama condicional "vivienda" activa
  {
    _id: IDS.tramite5,
    politicaId: IDS.polCredito, clienteId: IDS.usrLuisa,
    recepcionistaId: IDS.usrMaria, empresaId: IDS.empresa1,
    estado: "proceso", prioridad: "normal",
    actividades: [
      {
        id: "act_010", flujoId: IDS.fluDatos, usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRec1, nombre: "Datos del cliente",
        estado: "completado", fechaInicio: h24, fechaFin: h12, observacion: null,
        datosForm: {
          formularioId: IDS.formDatos, estado: "completado", fecha: h12,
          datos: [
            { componenteId: IDS.cmpNombre, etiqueta: "Nombre completo",     valor: "Luisa Méndez Torres" },
            { componenteId: IDS.cmpCI,     etiqueta: "Cédula de identidad", valor: "7654321" },
            { componenteId: IDS.cmpTel,    etiqueta: "Teléfono",            valor: "71234567" },
            { componenteId: IDS.cmpDir,    etiqueta: "Dirección",           valor: "Barrio Hamacas, Calle 5 #200" },
          ],
          observaciones: []
        }
      },
      {
        id: "act_011", flujoId: IDS.fluTipoCr, usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRec1, nombre: "Tipo de crédito",
        estado: "completado", fechaInicio: h12, fechaFin: h6, observacion: null,
        datosForm: {
          formularioId: IDS.formCredito, estado: "completado", fecha: h6,
          datos: [
            { componenteId: IDS.cmpTipoCr, etiqueta: "Tipo de crédito",       valor: "vivienda" },
            { componenteId: IDS.cmpMonto,  etiqueta: "Monto solicitado (Bs)", valor: "150000" },
          ],
          observaciones: []
        }
      },
      {
        id: "act_012", flujoId: IDS.fluVivien, usuarioId: IDS.usrJorge,
        departamentoId: IDS.depAdm, nombre: "Crédito vivienda",
        estado: "activo", fechaInicio: h6, fechaFin: null, observacion: null,
        datosForm: { formularioId: IDS.formEval, estado: "en_proceso", fecha: h6, datos: [], observaciones: [] }
      }
    ],
    fecha: h24, fechaFin: null
  }
]);
print("✓ Trámites (5)");

// ─── BITÁCORA ────────────────────────────────────────────────
db.bitacora.insertMany([
  { tramiteId: IDS.tramite1, usuarioId: IDS.usrMaria,  accion: "INICIAR",   descripcion: "Trámite de medidor iniciado",         estadoAnterior: null,      estadoNuevo: "proceso",    fecha: h24 },
  { tramiteId: IDS.tramite1, usuarioId: IDS.usrMaria,  accion: "AVANZAR",   descripcion: "Recepción completada",                estadoAnterior: "proceso", estadoNuevo: "proceso",    fecha: h12 },
  { tramiteId: IDS.tramite2, usuarioId: IDS.usrMaria,  accion: "INICIAR",   descripcion: "Trámite de medidor iniciado",         estadoAnterior: null,      estadoNuevo: "proceso",    fecha: h48 },
  { tramiteId: IDS.tramite2, usuarioId: IDS.usrCarlos, accion: "AVANZAR",   descripcion: "Evaluación técnica completada",       estadoAnterior: "proceso", estadoNuevo: "proceso",    fecha: h24 },
  { tramiteId: IDS.tramite2, usuarioId: IDS.usrMaria,  accion: "COMPLETAR", descripcion: "Trámite completado",                  estadoAnterior: "proceso", estadoNuevo: "completado", fecha: h12 },
  { tramiteId: IDS.tramite3, usuarioId: IDS.usrMaria,  accion: "INICIAR",   descripcion: "Trámite de medidor iniciado",         estadoAnterior: null,      estadoNuevo: "proceso",    fecha: h48 },
  { tramiteId: IDS.tramite3, usuarioId: IDS.usrMaria,  accion: "URGENTE",   descripcion: "Semáforo cambió a ROJO",              estadoAnterior: "proceso", estadoNuevo: "urgente",    fecha: h6  },
  { tramiteId: IDS.tramite4, usuarioId: IDS.usrMaria,  accion: "INICIAR",   descripcion: "Crédito iniciado",                    estadoAnterior: null,      estadoNuevo: "proceso",    fecha: h6  },
  { tramiteId: IDS.tramite5, usuarioId: IDS.usrMaria,  accion: "AVANZAR",   descripcion: "Flujo condicional activado: vivienda",estadoAnterior: "proceso", estadoNuevo: "proceso",    fecha: h6  },
]);
print("✓ Bitácora (9)");

// ─── NOTIFICACIONES ──────────────────────────────────────────
db.notificaciones.insertMany([
  { usuarioId: IDS.usrCarlos,  tramiteId: IDS.tramite1, tipo: "NUEVA_ACTIVIDAD", mensaje: "Se te asignó la evaluación técnica del trámite TRA-001", leida: false, fecha: h12 },
  { usuarioId: IDS.usrRoberto, tramiteId: IDS.tramite1, tipo: "AVANCE",          mensaje: "Tu trámite de medidor avanzó a Evaluación técnica",      leida: true,  fecha: h12 },
  { usuarioId: IDS.usrLuisa,   tramiteId: IDS.tramite2, tipo: "COMPLETADO",      mensaje: "Tu trámite de medidor fue completado exitosamente",       leida: true,  fecha: h12 },
  { usuarioId: IDS.usrMaria,   tramiteId: IDS.tramite3, tipo: "SEMAFORO_ROJO",   mensaje: "Trámite TRA-003 excedió el tiempo límite — URGENTE",      leida: false, fecha: h6  },
  { usuarioId: IDS.usrCarlos,  tramiteId: IDS.tramite3, tipo: "SEMAFORO_ROJO",   mensaje: "Tu actividad en TRA-003 está vencida",                   leida: false, fecha: h6  },
  { usuarioId: IDS.usrRoberto, tramiteId: IDS.tramite3, tipo: "URGENTE",         mensaje: "Tu trámite fue marcado como URGENTE por demora",          leida: false, fecha: h6  },
  { usuarioId: IDS.usrMaria,   tramiteId: IDS.tramite4, tipo: "NUEVA_ACTIVIDAD", mensaje: "Nuevo trámite de crédito ingresado",                      leida: true,  fecha: h6  },
  { usuarioId: IDS.usrJorge,   tramiteId: IDS.tramite5, tipo: "NUEVA_ACTIVIDAD", mensaje: "Se te asignó evaluación de crédito vivienda — TRA-005",   leida: false, fecha: h1  },
  { usuarioId: IDS.usrLuisa,   tramiteId: IDS.tramite5, tipo: "AVANCE",          mensaje: "Tu solicitud de crédito avanzó a revisión — TRA-005",     leida: false, fecha: h1  },
]);
print("✓ Notificaciones (9)");

// ─── DOCUMENTOS (Feature 3 — Gestión Documental) ────────────
db.documentos.insertMany([
  {
    _id: IDS.doc1,
    nombre: "Acta de inspección TRA-001.pdf",
    tipo: "pdf",
    tamanio: 204800,
    empresaId: IDS.empresa1,
    tramiteId: IDS.tramite1,
    actividadId: "act_002",
    claveAlmacenamiento: "uploads/emp_001/tra_001/acta_inspeccion.pdf",
    version: "1.0",
    versionesAnteriores: [],
    creadoPor: IDS.usrCarlos,
    fechaCreacion: h6,
    activo: true,
    descripcion: "Acta de la inspección técnica preliminar"
  },
  {
    _id: IDS.doc2,
    nombre: "Acta de cierre TRA-002.pdf",
    tipo: "pdf",
    tamanio: 153600,
    empresaId: IDS.empresa1,
    tramiteId: IDS.tramite2,
    actividadId: "act_005",
    claveAlmacenamiento: "uploads/emp_001/tra_002/acta_cierre.pdf",
    version: "1.0",
    versionesAnteriores: [],
    creadoPor: IDS.usrMaria,
    fechaCreacion: h12,
    activo: true,
    descripcion: "Documento de cierre del trámite completado"
  }
]);
print("✓ Documentos (2)");

// ─── PERMISOS DE DOCUMENTOS ──────────────────────────────────
db.permisos_documentos.insertMany([
  { documentoId: IDS.doc1, usuarioId: IDS.usrMaria,  nivel: "editor",       otorgadoPor: IDS.usrCarlos },
  { documentoId: IDS.doc1, usuarioId: IDS.usrJorge,  nivel: "visualizador", otorgadoPor: IDS.usrCarlos },
  { documentoId: IDS.doc2, usuarioId: IDS.usrMaria,  nivel: "editor",       otorgadoPor: IDS.usrMaria  },
  { documentoId: IDS.doc2, usuarioId: IDS.usrCarlos, nivel: "visualizador", otorgadoPor: IDS.usrMaria  },
]);
print("✓ Permisos de documentos (4)");

// ─── BITÁCORA DE DOCUMENTOS ──────────────────────────────────
db.bitacora_documentos.insertMany([
  { documentoId: IDS.doc1, usuarioId: IDS.usrCarlos, accion: "subio",    detalle: "Subió versión 1.0", fecha: h6  },
  { documentoId: IDS.doc1, usuarioId: IDS.usrMaria,  accion: "visualizo", detalle: "Visualizó el documento", fecha: h2 },
  { documentoId: IDS.doc2, usuarioId: IDS.usrMaria,  accion: "subio",    detalle: "Subió versión 1.0", fecha: h12 },
  { documentoId: IDS.doc2, usuarioId: IDS.usrJorge,  accion: "descargo", detalle: "Descargó el documento",   fecha: h6  },
]);
print("✓ Bitácora de documentos (4)");

// ─── RESUMEN ─────────────────────────────────────────────────
print("\n════════════════════════════════════════════");
print("  Seeding completado — workflow_dev");
print("════════════════════════════════════════════");
print("  empresas:            " + db.empresas.countDocuments());
print("  departamentos:       " + db.departamentos.countDocuments());
print("  roles:               " + db.roles.countDocuments());
print("  usuarios:            " + db.usuarios.countDocuments());
print("  tipo_componentes:    " + db.tipo_componentes.countDocuments());
print("  formularios:         " + db.formularios.countDocuments());
print("  politicas_negocio:   " + db.politicas_negocio.countDocuments());
print("  flujos:              " + db.flujos.countDocuments());
print("  flujo_relaciones:    " + db.flujo_relaciones.countDocuments());
print("  tramites:            " + db.tramites.countDocuments());
print("  bitacora:            " + db.bitacora.countDocuments());
print("  notificaciones:      " + db.notificaciones.countDocuments());
print("  documentos:          " + db.documentos.countDocuments());
print("  permisos_documentos: " + db.permisos_documentos.countDocuments());
print("  bitacora_documentos: " + db.bitacora_documentos.countDocuments());
print("════════════════════════════════════════════");
print("\n  Credenciales de prueba (password: 123456)");
print("  admin1@crebolivia.com     → administrador");
print("  admin2@crebolivia.com     → administrador");
print("  cliente1@crebolivia.com   → cliente");
print("  cliente2@crebolivia.com   → cliente");
print("  recepcion1@crebolivia.com → recepcionista");
print("  recepcion2@crebolivia.com → recepcionista");
print("  recepcion3@crebolivia.com → recepcionista");
print("  recepcion4@crebolivia.com → recepcionista");
print("  recepcion5@crebolivia.com → recepcionista");
print("════════════════════════════════════════════\n");
