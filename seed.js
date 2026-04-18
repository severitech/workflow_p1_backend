// ============================================================
// Script de datos sintéticos para workflow_db
// Ejecutar con: mongosh workflow_db seed.js
// O desde mongosh interactivo: load("seed.js")
// ============================================================

const DB_NAME = "workflow_db";
db = db.getSiblingDB(DB_NAME);

// Limpia todas las colecciones antes de insertar
const colecciones = [
  "empresas","departamentos","roles","usuarios","tipo_componentes",
  "formularios","politicas_negocio","flujos","tramites","bitacora","notificaciones"
];
colecciones.forEach(c => { db[c].drop(); print("Colección limpiada: " + c); });

// ─── IDs fijos para referencias cruzadas ────────────────────
const IDS = {
  // Empresa
  empresa1: "emp_001",

  // Departamentos
  depRecepcion: "dep_001",
  depTecnico:   "dep_002",
  depLegal:     "dep_003",
  depComercial: "dep_004",

  // Roles — solo 3
  rolCliente:       "rol_001",
  rolRecepcionista: "rol_002",
  rolAdmin:         "rol_003",

  // Usuarios
  usrMaria:    "usr_001",  // recepcionista
  usrJorge:    "usr_002",  // administrador
  usrCarlos:   "usr_003",  // administrador (gestiona técnica)
  usrRoberto:  "usr_004",  // cliente
  usrLuisa:    "usr_005",  // cliente
  usrPedro:    "usr_006",  // cliente

  // Tipos de componente
  tcTexto:    "tc_001",
  tcTextarea: "tc_002",
  tcSelect:   "tc_003",
  tcBoolean:  "tc_004",
  tcFecha:    "tc_005",
  tcArchivo:  "tc_006",
  tcNumero:   "tc_007",

  // Formularios
  formDatosCliente:  "form_001",
  formTipoCredito:   "form_002",
  formEvalTecnica:   "form_003",
  formCierre:        "form_004",
  formMedidor:       "form_005",

  // Componentes (embebidos, IDs para referencias en DatoForm)
  compNombre:      "cmp_001",
  compCI:          "cmp_002",
  compTelefono:    "cmp_003",
  compDireccion:   "cmp_004",
  compTipoCredito: "cmp_005",
  compMonto:       "cmp_006",
  compObs:         "cmp_007",
  compAprobado:    "cmp_008",
  compDocCierre:   "cmp_009",
  compNumMedidor:  "cmp_010",
  compUbicacion:   "cmp_011",

  // Políticas
  politicaCredito:  "pol_001",
  politicaMedidor:  "pol_002",

  // Flujos — política medidor
  flujoRecepcion: "flu_001",
  flujoTecnico:   "flu_002",
  flujoCierre:    "flu_003",

  // Flujos — política crédito (con condicionales)
  flujoDatosCliente: "flu_010",
  flujoTipoCredito:  "flu_011",
  flujoCredVivienda: "flu_012",
  flujoCredEmpresa:  "flu_013",
  flujoCredVehiculo: "flu_014",
  flujoCierreCredito:"flu_015",

  // Trámites
  tramite1: "tra_001",
  tramite2: "tra_002",
  tramite3: "tra_003",
  tramite4: "tra_004",
  tramite5: "tra_005",
};

const ahora = new Date();
const hace1h  = new Date(ahora.getTime() - 1 * 3600000);
const hace2h  = new Date(ahora.getTime() - 2 * 3600000);
const hace6h  = new Date(ahora.getTime() - 6 * 3600000);
const hace12h = new Date(ahora.getTime() - 12 * 3600000);
const hace24h = new Date(ahora.getTime() - 24 * 3600000);
const hace48h = new Date(ahora.getTime() - 48 * 3600000);
const en2h    = new Date(ahora.getTime() + 2 * 3600000);
const en24h   = new Date(ahora.getTime() + 24 * 3600000);

// ─── EMPRESAS ───────────────────────────────────────────────
db.empresas.insertMany([
  {
    _id: IDS.empresa1,
    nombre: "CRE Bolivia",
    nit: "1234567890",
    direccion: "Av. Cañoto 1234, Santa Cruz de la Sierra",
    telefono: "591-3-3333333",
    correo: "info@crebolivia.com",
    activo: true,
    fechaCreacion: hace48h,
    fechaModificacion: ahora
  }
]);
print("✓ Empresa insertada");

// ─── DEPARTAMENTOS ──────────────────────────────────────────
db.departamentos.insertMany([
  { _id: IDS.depRecepcion, nombre: "Recepción",  descripcion: "Atención al cliente", empresaId: IDS.empresa1, activo: true, fechaCreacion: hace48h },
  { _id: IDS.depTecnico,   nombre: "Técnico",    descripcion: "Inspecciones y evaluaciones técnicas", empresaId: IDS.empresa1, activo: true, fechaCreacion: hace48h },
  { _id: IDS.depLegal,     nombre: "Legal",      descripcion: "Revisión de contratos y documentos", empresaId: IDS.empresa1, activo: true, fechaCreacion: hace48h },
  { _id: IDS.depComercial, nombre: "Comercial",  descripcion: "Gestión comercial y ventas", empresaId: IDS.empresa1, activo: true, fechaCreacion: hace48h }
]);
print("✓ Departamentos insertados");

// ─── ROLES ──────────────────────────────────────────────────
db.roles.insertMany([
  { _id: IDS.rolCliente,       nombre: "cliente",       descripcion: "Ciudadano que solicita un trámite", fechaCreacion: hace48h },
  { _id: IDS.rolRecepcionista, nombre: "recepcionista", descripcion: "Recibe y gestiona trámites",        fechaCreacion: hace48h },
  { _id: IDS.rolAdmin,         nombre: "administrador", descripcion: "Administrador con acceso completo", fechaCreacion: hace48h }
]);
print("✓ Roles insertados");

// ─── USUARIOS (passwords = BCrypt de "123456") ──────────────
// $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
const passHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
db.usuarios.insertMany([
  { _id: IDS.usrMaria,   nombre: "María",   apellido: "García",  correo: "maria@crebolivia.com",  password: passHash, rolId: IDS.rolRecepcionista, empresaId: IDS.empresa1, departamentoId: "dep_001", activo: true, fechaCreacion: hace48h },
  { _id: IDS.usrJorge,   nombre: "Jorge",   apellido: "López",   correo: "jorge@crebolivia.com",  password: passHash, rolId: IDS.rolAdmin,         empresaId: IDS.empresa1, departamentoId: "dep_003", activo: true, fechaCreacion: hace48h },
  { _id: IDS.usrCarlos,  nombre: "Carlos",  apellido: "Pérez",   correo: "carlos@crebolivia.com", password: passHash, rolId: IDS.rolAdmin,         empresaId: IDS.empresa1, departamentoId: "dep_002", activo: true, fechaCreacion: hace48h },
  { _id: IDS.usrRoberto, nombre: "Roberto", apellido: "Flores",  correo: "roberto@cliente.com",   password: passHash, rolId: IDS.rolCliente,       empresaId: IDS.empresa1, departamentoId: null,      activo: true, fechaCreacion: hace48h },
  { _id: IDS.usrLuisa,   nombre: "Luisa",   apellido: "Méndez",  correo: "luisa@cliente.com",     password: passHash, rolId: IDS.rolCliente,       empresaId: IDS.empresa1, departamentoId: null,      activo: true, fechaCreacion: hace48h },
  { _id: IDS.usrPedro,   nombre: "Pedro",   apellido: "Vásquez", correo: "pedro@cliente.com",     password: passHash, rolId: IDS.rolCliente,       empresaId: IDS.empresa1, departamentoId: null,      activo: true, fechaCreacion: hace48h }
]);
print("✓ Usuarios insertados");

// ─── TIPO COMPONENTES ────────────────────────────────────────
db.tipo_componentes.insertMany([
  { _id: IDS.tcTexto,    nombre: "texto",    descripcion: "Campo de texto corto", fechaCreacion: hace48h },
  { _id: IDS.tcTextarea, nombre: "textarea", descripcion: "Área de texto largo",  fechaCreacion: hace48h },
  { _id: IDS.tcSelect,   nombre: "select",   descripcion: "Lista desplegable",    fechaCreacion: hace48h },
  { _id: IDS.tcBoolean,  nombre: "boolean",  descripcion: "Casilla verdadero/falso", fechaCreacion: hace48h },
  { _id: IDS.tcFecha,    nombre: "fecha",    descripcion: "Selector de fecha",    fechaCreacion: hace48h },
  { _id: IDS.tcArchivo,  nombre: "archivo",  descripcion: "Carga de archivo",     fechaCreacion: hace48h },
  { _id: IDS.tcNumero,   nombre: "numero",   descripcion: "Campo numérico",       fechaCreacion: hace48h }
]);
print("✓ TipoComponentes insertados");

// ─── FORMULARIOS (con componentes embebidos) ─────────────────
db.formularios.insertMany([
  {
    _id: IDS.formDatosCliente,
    nombre: "Datos del cliente",
    descripcion: "Información básica del solicitante",
    empresaId: IDS.empresa1,
    activo: true,
    componentes: [
      { id: IDS.compNombre,   tipoId: IDS.tcTexto,  etiqueta: "Nombre completo", placeholder: "Ej: Juan Pérez", orden: 1, posicionX: 0, posicionY: 0, requerido: true },
      { id: IDS.compCI,       tipoId: IDS.tcNumero, etiqueta: "Cédula de identidad", placeholder: "Ej: 12345678", orden: 2, posicionX: 0, posicionY: 1, requerido: true },
      { id: IDS.compTelefono, tipoId: IDS.tcTexto,  etiqueta: "Teléfono", placeholder: "Ej: 70000000", orden: 3, posicionX: 1, posicionY: 0, requerido: true },
      { id: IDS.compDireccion,tipoId: IDS.tcTextarea,etiqueta: "Dirección", placeholder: "Calle, barrio, ciudad", orden: 4, posicionX: 1, posicionY: 1, requerido: true }
    ],
    fechaCreacion: hace48h
  },
  {
    _id: IDS.formTipoCredito,
    nombre: "Tipo de crédito",
    descripcion: "Selección del tipo de crédito solicitado",
    empresaId: IDS.empresa1,
    activo: true,
    componentes: [
      { id: IDS.compTipoCredito, tipoId: IDS.tcSelect, etiqueta: "Tipo de crédito", placeholder: "Seleccione...", orden: 1, posicionX: 0, posicionY: 0, requerido: true,
        opciones: ["vivienda", "empresa", "vehiculo"] },
      { id: IDS.compMonto, tipoId: IDS.tcNumero, etiqueta: "Monto solicitado (Bs)", placeholder: "Ej: 50000", orden: 2, posicionX: 0, posicionY: 1, requerido: true }
    ],
    fechaCreacion: hace48h
  },
  {
    _id: IDS.formEvalTecnica,
    nombre: "Evaluación técnica",
    descripcion: "Resultados de la inspección técnica",
    empresaId: IDS.empresa1,
    activo: true,
    componentes: [
      { id: IDS.compObs,      tipoId: IDS.tcTextarea, etiqueta: "Observaciones técnicas", placeholder: "Describa los hallazgos...", orden: 1, posicionX: 0, posicionY: 0, requerido: false },
      { id: IDS.compAprobado, tipoId: IDS.tcBoolean,  etiqueta: "Aprobado técnicamente", placeholder: "", orden: 2, posicionX: 0, posicionY: 1, requerido: true }
    ],
    fechaCreacion: hace48h
  },
  {
    _id: IDS.formCierre,
    nombre: "Cierre de trámite",
    descripcion: "Documentación final para cerrar el trámite",
    empresaId: IDS.empresa1,
    activo: true,
    componentes: [
      { id: IDS.compDocCierre, tipoId: IDS.tcArchivo, etiqueta: "Documento de cierre", placeholder: "", orden: 1, posicionX: 0, posicionY: 0, requerido: true }
    ],
    fechaCreacion: hace48h
  },
  {
    _id: IDS.formMedidor,
    nombre: "Solicitud de medidor",
    descripcion: "Datos para instalación de nuevo medidor eléctrico",
    empresaId: IDS.empresa1,
    activo: true,
    componentes: [
      { id: IDS.compNumMedidor, tipoId: IDS.tcTexto, etiqueta: "Número de medidor actual", placeholder: "Ej: MED-12345", orden: 1, posicionX: 0, posicionY: 0, requerido: false },
      { id: IDS.compUbicacion,  tipoId: IDS.tcTextarea, etiqueta: "Ubicación exacta del domicilio", placeholder: "Barrio, calle, número...", orden: 2, posicionX: 0, posicionY: 1, requerido: true }
    ],
    fechaCreacion: hace48h
  }
]);
print("✓ Formularios insertados");

// ─── POLÍTICAS DE NEGOCIO ────────────────────────────────────
db.politicas_negocio.insertMany([
  {
    _id: IDS.politicaMedidor,
    nombre: "Solicitud de medidor eléctrico",
    descripcion: "Proceso completo para la instalación de un nuevo medidor eléctrico residencial",
    tipo: "secuencial",
    estado: "activa",
    version: 1,
    empresaId: IDS.empresa1,
    creadoPorId: IDS.usrJorge,
    fechaCreacion: hace48h,
    fechaModificacion: hace24h
  },
  {
    _id: IDS.politicaCredito,
    nombre: "Solicitud de crédito",
    descripcion: "Evaluación y aprobación de créditos con flujos condicionales por tipo",
    tipo: "secuencial",
    estado: "activa",
    version: 1,
    empresaId: IDS.empresa1,
    creadoPorId: IDS.usrJorge,
    fechaCreacion: hace48h,
    fechaModificacion: hace24h
  }
]);
print("✓ Políticas insertadas");

// ─── FLUJOS ──────────────────────────────────────────────────
db.flujos.insertMany([
  // Política: Medidor eléctrico (3 pasos secuenciales)
  { _id: IDS.flujoRecepcion, nombre: "Recepción",          politicaId: IDS.politicaMedidor, formularioId: IDS.formMedidor,      orden: 1, esObligatorio: true, flujoPadreId: null, condicionCampo: null, condicionValor: null, departamentoId: IDS.depRecepcion, tiempoLimiteHoras: 8,  fechaCreacion: hace48h },
  { _id: IDS.flujoTecnico,   nombre: "Evaluación técnica", politicaId: IDS.politicaMedidor, formularioId: IDS.formEvalTecnica,  orden: 2, esObligatorio: true, flujoPadreId: null, condicionCampo: null, condicionValor: null, departamentoId: IDS.depTecnico,   tiempoLimiteHoras: 24, fechaCreacion: hace48h },
  { _id: IDS.flujoCierre,    nombre: "Cierre",             politicaId: IDS.politicaMedidor, formularioId: IDS.formCierre,       orden: 3, esObligatorio: true, flujoPadreId: null, condicionCampo: null, condicionValor: null, departamentoId: IDS.depRecepcion, tiempoLimiteHoras: 4,  fechaCreacion: hace48h },

  // Política: Crédito (con flujos condicionales)
  { _id: IDS.flujoDatosCliente, nombre: "Datos del cliente", politicaId: IDS.politicaCredito, formularioId: IDS.formDatosCliente, orden: 1, esObligatorio: true, flujoPadreId: null, condicionCampo: null, condicionValor: null, departamentoId: IDS.depRecepcion, tiempoLimiteHoras: 8,  fechaCreacion: hace48h },
  { _id: IDS.flujoTipoCredito,  nombre: "Tipo de crédito",   politicaId: IDS.politicaCredito, formularioId: IDS.formTipoCredito,  orden: 2, esObligatorio: true, flujoPadreId: null, condicionCampo: null, condicionValor: null, departamentoId: IDS.depRecepcion, tiempoLimiteHoras: 8,  fechaCreacion: hace48h },

  // Flujos hijos condicionales del paso "Tipo de crédito"
  { _id: IDS.flujoCredVivienda, nombre: "Crédito vivienda", politicaId: IDS.politicaCredito, formularioId: IDS.formEvalTecnica, orden: 3, esObligatorio: true, flujoPadreId: IDS.flujoTipoCredito, condicionCampo: "Tipo de crédito", condicionValor: "vivienda", departamentoId: IDS.depLegal,     tiempoLimiteHoras: 48, fechaCreacion: hace48h },
  { _id: IDS.flujoCredEmpresa,  nombre: "Crédito empresa",  politicaId: IDS.politicaCredito, formularioId: IDS.formEvalTecnica, orden: 3, esObligatorio: true, flujoPadreId: IDS.flujoTipoCredito, condicionCampo: "Tipo de crédito", condicionValor: "empresa",  departamentoId: IDS.depLegal,     tiempoLimiteHoras: 72, fechaCreacion: hace48h },
  { _id: IDS.flujoCredVehiculo, nombre: "Crédito vehículo", politicaId: IDS.politicaCredito, formularioId: IDS.formEvalTecnica, orden: 3, esObligatorio: true, flujoPadreId: IDS.flujoTipoCredito, condicionCampo: "Tipo de crédito", condicionValor: "vehiculo",  departamentoId: IDS.depTecnico,   tiempoLimiteHoras: 24, fechaCreacion: hace48h },

  { _id: IDS.flujoCierreCredito, nombre: "Cierre crédito", politicaId: IDS.politicaCredito, formularioId: IDS.formCierre, orden: 4, esObligatorio: true, flujoPadreId: null, condicionCampo: null, condicionValor: null, departamentoId: IDS.depRecepcion, tiempoLimiteHoras: 4, fechaCreacion: hace48h }
]);
print("✓ Flujos insertados");

// ─── TRÁMITES (con actividades embebidas) ────────────────────
db.tramites.insertMany([
  // Trámite 1 — Roberto — medidor — EN PROCESO (actividad técnica activa, semáforo amarillo)
  {
    _id: IDS.tramite1,
    politicaId: IDS.politicaMedidor,
    clienteId: IDS.usrRoberto,
    recepcionistaId: IDS.usrMaria,
    empresaId: IDS.empresa1,
    estado: "proceso",
    semaforo: "amarillo",
    actividades: [
      {
        id: "act_001",
        flujoId: IDS.flujoRecepcion,
        usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRecepcion,
        nombre: "Recepción",
        estado: "completado",
        tiempoLimite: 8,
        semaforo: "verde",
        fechaInicio: hace24h,
        fechaFin: hace12h,
        observacion: "Documentos recibidos correctamente",
        datosForm: {
          formularioId: IDS.formMedidor,
          estado: "completado",
          datos: [
            { componenteId: IDS.compNumMedidor, etiqueta: "Número de medidor actual", valor: "MED-78432" },
            { componenteId: IDS.compUbicacion,  etiqueta: "Ubicación exacta del domicilio", valor: "Barrio Equipetrol, Calle 3 Este #456" }
          ],
          observaciones: [],
          fecha: hace12h
        }
      },
      {
        id: "act_002",
        flujoId: IDS.flujoTecnico,
        usuarioId: IDS.usrCarlos,
        departamentoId: IDS.depTecnico,
        nombre: "Evaluación técnica",
        estado: "activo",
        tiempoLimite: 24,
        semaforo: "amarillo",
        fechaInicio: hace12h,
        fechaFin: null,
        observacion: null,
        datosForm: {
          formularioId: IDS.formEvalTecnica,
          estado: "en_proceso",
          datos: [
            { componenteId: IDS.compObs, etiqueta: "Observaciones técnicas", valor: "Instalación requiere cable de 10mm" }
          ],
          observaciones: [],
          fecha: hace6h
        }
      }
    ],
    fecha: hace24h,
    fechaFin: null,
    fechaCreacion: hace24h,
    fechaModificacion: hace6h
  },

  // Trámite 2 — Luisa — medidor — COMPLETADO
  {
    _id: IDS.tramite2,
    politicaId: IDS.politicaMedidor,
    clienteId: IDS.usrLuisa,
    recepcionistaId: IDS.usrMaria,
    empresaId: IDS.empresa1,
    estado: "completado",
    semaforo: "verde",
    actividades: [
      {
        id: "act_003",
        flujoId: IDS.flujoRecepcion,
        usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRecepcion,
        nombre: "Recepción",
        estado: "completado",
        tiempoLimite: 8,
        semaforo: "verde",
        fechaInicio: hace48h,
        fechaFin: hace48h,
        observacion: "Datos completos",
        datosForm: {
          formularioId: IDS.formMedidor,
          estado: "completado",
          datos: [
            { componenteId: IDS.compNumMedidor, etiqueta: "Número de medidor actual", valor: "MED-11111" },
            { componenteId: IDS.compUbicacion,  etiqueta: "Ubicación exacta del domicilio", valor: "Barrio Urbarí, Calle Las Palmas #123" }
          ],
          observaciones: [],
          fecha: hace48h
        }
      },
      {
        id: "act_004",
        flujoId: IDS.flujoTecnico,
        usuarioId: IDS.usrCarlos,
        departamentoId: IDS.depTecnico,
        nombre: "Evaluación técnica",
        estado: "completado",
        tiempoLimite: 24,
        semaforo: "verde",
        fechaInicio: hace48h,
        fechaFin: hace24h,
        observacion: "Instalación viable",
        datosForm: {
          formularioId: IDS.formEvalTecnica,
          estado: "completado",
          datos: [
            { componenteId: IDS.compObs,      etiqueta: "Observaciones técnicas", valor: "Sin observaciones, instalación estándar" },
            { componenteId: IDS.compAprobado, etiqueta: "Aprobado técnicamente",  valor: "true" }
          ],
          observaciones: [],
          fecha: hace24h
        }
      },
      {
        id: "act_005",
        flujoId: IDS.flujoCierre,
        usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRecepcion,
        nombre: "Cierre",
        estado: "completado",
        tiempoLimite: 4,
        semaforo: "verde",
        fechaInicio: hace24h,
        fechaFin: hace12h,
        observacion: "Trámite cerrado correctamente",
        datosForm: {
          formularioId: IDS.formCierre,
          estado: "completado",
          datos: [
            { componenteId: IDS.compDocCierre, etiqueta: "Documento de cierre", valor: "acta_cierre_tra002.pdf" }
          ],
          observaciones: [],
          fecha: hace12h
        }
      }
    ],
    fecha: hace48h,
    fechaFin: hace12h,
    fechaCreacion: hace48h,
    fechaModificacion: hace12h
  },

  // Trámite 3 — Pedro — medidor — URGENTE (semáforo rojo, excedió tiempo)
  {
    _id: IDS.tramite3,
    politicaId: IDS.politicaMedidor,
    clienteId: IDS.usrPedro,
    recepcionistaId: IDS.usrMaria,
    empresaId: IDS.empresa1,
    estado: "urgente",
    semaforo: "rojo",
    actividades: [
      {
        id: "act_006",
        flujoId: IDS.flujoRecepcion,
        usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRecepcion,
        nombre: "Recepción",
        estado: "completado",
        tiempoLimite: 8,
        semaforo: "verde",
        fechaInicio: hace48h,
        fechaFin: hace48h,
        observacion: null,
        datosForm: {
          formularioId: IDS.formMedidor,
          estado: "completado",
          datos: [
            { componenteId: IDS.compNumMedidor, etiqueta: "Número de medidor actual", valor: "MED-99999" },
            { componenteId: IDS.compUbicacion,  etiqueta: "Ubicación exacta del domicilio", valor: "Plan Tres Mil, Av. Principal #789" }
          ],
          observaciones: [],
          fecha: hace48h
        }
      },
      {
        id: "act_007",
        flujoId: IDS.flujoTecnico,
        usuarioId: IDS.usrSofia,
        departamentoId: IDS.depTecnico,
        nombre: "Evaluación técnica",
        estado: "activo",
        tiempoLimite: 24,
        semaforo: "rojo",
        fechaInicio: hace48h,
        fechaFin: null,
        observacion: null,
        datosForm: {
          formularioId: IDS.formEvalTecnica,
          estado: "en_proceso",
          datos: [],
          observaciones: [
            { usuarioId: IDS.usrMaria, descripcion: "Por favor priorizar este trámite", estado: "observado", fecha: hace6h }
          ],
          fecha: hace48h
        }
      }
    ],
    fecha: hace48h,
    fechaFin: null,
    fechaCreacion: hace48h,
    fechaModificacion: hace6h
  },

  // Trámite 4 — Roberto — crédito — EN PROCESO (en paso tipo crédito)
  {
    _id: IDS.tramite4,
    politicaId: IDS.politicaCredito,
    clienteId: IDS.usrRoberto,
    recepcionistaId: IDS.usrMaria,
    empresaId: IDS.empresa1,
    estado: "proceso",
    semaforo: "verde",
    actividades: [
      {
        id: "act_008",
        flujoId: IDS.flujoDatosCliente,
        usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRecepcion,
        nombre: "Datos del cliente",
        estado: "completado",
        tiempoLimite: 8,
        semaforo: "verde",
        fechaInicio: hace6h,
        fechaFin: hace2h,
        observacion: null,
        datosForm: {
          formularioId: IDS.formDatosCliente,
          estado: "completado",
          datos: [
            { componenteId: IDS.compNombre,    etiqueta: "Nombre completo",     valor: "Roberto Flores Suárez" },
            { componenteId: IDS.compCI,        etiqueta: "Cédula de identidad", valor: "5678901" },
            { componenteId: IDS.compTelefono,  etiqueta: "Teléfono",            valor: "76543210" },
            { componenteId: IDS.compDireccion, etiqueta: "Dirección",           valor: "Av. Brasil #1200, Santa Cruz" }
          ],
          observaciones: [],
          fecha: hace2h
        }
      },
      {
        id: "act_009",
        flujoId: IDS.flujoTipoCredito,
        usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRecepcion,
        nombre: "Tipo de crédito",
        estado: "activo",
        tiempoLimite: 8,
        semaforo: "verde",
        fechaInicio: hace2h,
        fechaFin: null,
        observacion: null,
        datosForm: {
          formularioId: IDS.formTipoCredito,
          estado: "en_proceso",
          datos: [],
          observaciones: [],
          fecha: hace2h
        }
      }
    ],
    fecha: hace6h,
    fechaFin: null,
    fechaCreacion: hace6h,
    fechaModificacion: hace1h
  },

  // Trámite 5 — Luisa — crédito — EN PROCESO (en evaluación vivienda, flujo condicional activado)
  {
    _id: IDS.tramite5,
    politicaId: IDS.politicaCredito,
    clienteId: IDS.usrLuisa,
    recepcionistaId: IDS.usrMaria,
    empresaId: IDS.empresa1,
    estado: "proceso",
    semaforo: "verde",
    actividades: [
      {
        id: "act_010",
        flujoId: IDS.flujoDatosCliente,
        usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRecepcion,
        nombre: "Datos del cliente",
        estado: "completado",
        tiempoLimite: 8,
        semaforo: "verde",
        fechaInicio: hace24h,
        fechaFin: hace12h,
        observacion: null,
        datosForm: {
          formularioId: IDS.formDatosCliente,
          estado: "completado",
          datos: [
            { componenteId: IDS.compNombre,    etiqueta: "Nombre completo",     valor: "Luisa Méndez Torres" },
            { componenteId: IDS.compCI,        etiqueta: "Cédula de identidad", valor: "7654321" },
            { componenteId: IDS.compTelefono,  etiqueta: "Teléfono",            valor: "71234567" },
            { componenteId: IDS.compDireccion, etiqueta: "Dirección",           valor: "Barrio Hamacas, Calle 5 #200" }
          ],
          observaciones: [],
          fecha: hace12h
        }
      },
      {
        id: "act_011",
        flujoId: IDS.flujoTipoCredito,
        usuarioId: IDS.usrMaria,
        departamentoId: IDS.depRecepcion,
        nombre: "Tipo de crédito",
        estado: "completado",
        tiempoLimite: 8,
        semaforo: "verde",
        fechaInicio: hace12h,
        fechaFin: hace6h,
        observacion: null,
        datosForm: {
          formularioId: IDS.formTipoCredito,
          estado: "completado",
          datos: [
            { componenteId: IDS.compTipoCredito, etiqueta: "Tipo de crédito",       valor: "vivienda" },
            { componenteId: IDS.compMonto,        etiqueta: "Monto solicitado (Bs)", valor: "150000" }
          ],
          observaciones: [],
          fecha: hace6h
        }
      },
      {
        // Flujo condicional activado por condicionValor="vivienda"
        id: "act_012",
        flujoId: IDS.flujoCredVivienda,
        usuarioId: IDS.usrAna,
        departamentoId: IDS.depLegal,
        nombre: "Crédito vivienda",
        estado: "activo",
        tiempoLimite: 48,
        semaforo: "verde",
        fechaInicio: hace6h,
        fechaFin: null,
        observacion: null,
        datosForm: {
          formularioId: IDS.formEvalTecnica,
          estado: "en_proceso",
          datos: [],
          observaciones: [],
          fecha: hace6h
        }
      }
    ],
    fecha: hace24h,
    fechaFin: null,
    fechaCreacion: hace24h,
    fechaModificacion: hace1h
  }
]);
print("✓ Trámites insertados");

// ─── BITÁCORA ────────────────────────────────────────────────
db.bitacora.insertMany([
  { tramiteId: IDS.tramite1, usuarioId: IDS.usrMaria,  accion: "INICIAR",  descripcion: "Trámite iniciado",            estadoAnterior: null,      estadoNuevo: "proceso",  fecha: hace24h },
  { tramiteId: IDS.tramite1, usuarioId: IDS.usrMaria,  accion: "AVANZAR",  descripcion: "Recepción completada",        estadoAnterior: "proceso", estadoNuevo: "proceso",  fecha: hace12h },
  { tramiteId: IDS.tramite2, usuarioId: IDS.usrMaria,  accion: "INICIAR",  descripcion: "Trámite iniciado",            estadoAnterior: null,      estadoNuevo: "proceso",  fecha: hace48h },
  { tramiteId: IDS.tramite2, usuarioId: IDS.usrMaria,  accion: "AVANZAR",  descripcion: "Recepción completada",        estadoAnterior: "proceso", estadoNuevo: "proceso",  fecha: hace48h },
  { tramiteId: IDS.tramite2, usuarioId: IDS.usrCarlos, accion: "AVANZAR",  descripcion: "Técnica completada",          estadoAnterior: "proceso", estadoNuevo: "proceso",  fecha: hace24h },
  { tramiteId: IDS.tramite2, usuarioId: IDS.usrMaria,  accion: "COMPLETAR",descripcion: "Trámite completado",          estadoAnterior: "proceso", estadoNuevo: "completado",fecha: hace12h },
  { tramiteId: IDS.tramite3, usuarioId: IDS.usrMaria,  accion: "INICIAR",  descripcion: "Trámite iniciado",            estadoAnterior: null,      estadoNuevo: "proceso",  fecha: hace48h },
  { tramiteId: IDS.tramite3, usuarioId: IDS.usrMaria,  accion: "URGENTE",  descripcion: "Semáforo cambió a ROJO",      estadoAnterior: "proceso", estadoNuevo: "urgente",  fecha: hace6h  },
  { tramiteId: IDS.tramite4, usuarioId: IDS.usrMaria,  accion: "INICIAR",  descripcion: "Crédito iniciado",            estadoAnterior: null,      estadoNuevo: "proceso",  fecha: hace6h  },
  { tramiteId: IDS.tramite5, usuarioId: IDS.usrMaria,  accion: "AVANZAR",  descripcion: "Flujo condicional: vivienda", estadoAnterior: "proceso", estadoNuevo: "proceso",  fecha: hace6h  }
]);
print("✓ Bitácora insertada");

// ─── NOTIFICACIONES ──────────────────────────────────────────
db.notificaciones.insertMany([
  { usuarioId: IDS.usrCarlos,  tramiteId: IDS.tramite1, tipo: "NUEVA_ACTIVIDAD", mensaje: "Se te asignó la evaluación técnica del trámite TRA-001", leida: false, fecha: hace12h },
  { usuarioId: IDS.usrRoberto, tramiteId: IDS.tramite1, tipo: "AVANCE",          mensaje: "Tu trámite de medidor avanzó a Evaluación técnica",        leida: true,  fecha: hace12h },
  { usuarioId: IDS.usrLuisa,   tramiteId: IDS.tramite2, tipo: "COMPLETADO",      mensaje: "Tu trámite de medidor fue completado exitosamente",         leida: true,  fecha: hace12h },
  { usuarioId: IDS.usrMaria,   tramiteId: IDS.tramite3, tipo: "SEMAFORO_ROJO",   mensaje: "⚠ Trámite TRA-003 excedió el tiempo límite — URGENTE",      leida: false, fecha: hace6h  },
  { usuarioId: IDS.usrSofia,   tramiteId: IDS.tramite3, tipo: "SEMAFORO_ROJO",   mensaje: "⚠ Tu actividad en TRA-003 está vencida — priorizar",        leida: false, fecha: hace6h  },
  { usuarioId: IDS.usrPedro,   tramiteId: IDS.tramite3, tipo: "URGENTE",         mensaje: "Tu trámite fue marcado como URGENTE por demora",            leida: false, fecha: hace6h  },
  { usuarioId: IDS.usrMaria,   tramiteId: IDS.tramite4, tipo: "NUEVA_ACTIVIDAD", mensaje: "Nuevo trámite de crédito ingresado para Roberto Flores",    leida: true,  fecha: hace6h  },
  { usuarioId: IDS.usrAna,     tramiteId: IDS.tramite5, tipo: "NUEVA_ACTIVIDAD", mensaje: "Se te asignó evaluación de crédito vivienda — TRA-005",     leida: false, fecha: hace1h  },
  { usuarioId: IDS.usrLuisa,   tramiteId: IDS.tramite5, tipo: "AVANCE",          mensaje: "Tu solicitud de crédito avanzó a revisión legal",           leida: false, fecha: hace1h  }
]);
print("✓ Notificaciones insertadas");

// ─── RESUMEN ─────────────────────────────────────────────────
print("\n════════════════════════════════════════");
print("  Seeding completado — workflow_db");
print("════════════════════════════════════════");
print("  empresas:          " + db.empresas.countDocuments());
print("  departamentos:     " + db.departamentos.countDocuments());
print("  roles:             " + db.roles.countDocuments());
print("  usuarios:          " + db.usuarios.countDocuments());
print("  tipo_componentes:  " + db.tipo_componentes.countDocuments());
print("  formularios:       " + db.formularios.countDocuments());
print("  politicas_negocio: " + db.politicas_negocio.countDocuments());
print("  flujos:            " + db.flujos.countDocuments());
print("  tramites:          " + db.tramites.countDocuments());
print("  bitacora:          " + db.bitacora.countDocuments());
print("  notificaciones:    " + db.notificaciones.countDocuments());
print("════════════════════════════════════════");
print("\n  Credenciales de prueba (password: 123456)");
print("  maria@crebolivia.com  → recepcionista");
print("  jorge@crebolivia.com  → administrador");
print("  carlos@crebolivia.com → tecnico");
print("  ana@crebolivia.com    → legal");
print("  roberto@cliente.com   → cliente");
print("════════════════════════════════════════\n");
