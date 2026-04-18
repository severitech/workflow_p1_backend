<!-- # CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=MyClassTests

# Run a single test method
./mvnw test -Dtest=MyClassTests#myMethod
```

## Stack

- **Spring Boot 4.0.5**, Java 17
- **Spring Web MVC** — REST controllers
- **Spring WebSocket** — real-time messaging
- **Spring Data MongoDB** — persistence layer

## Architecture

Standard layered Spring Boot application. Base package: `com.example.parcial1`.

`application.properties` only sets the app name — MongoDB URI and any other config must be added there before the app can connect to a database.
 -->
Sos un experto en Spring Boot 3.2, MongoDB y WebSocket. Generame un backend completo y funcional para un sistema de workflow universal. Seguí TODAS las instrucciones al pie de la letra sin omitir ningún archivo, todo los archivos deben estar escrito en español al igual que las variables o llamadas a funcion, ten en cuenta que debe tener comentarios de que hace cada funcion que escribas, pero no un comentario extenso, sino directo .

═══════════════════════════════════════════
STACK TECNOLÓGICO
═══════════════════════════════════════════
- Java 17
- Spring Boot 3.2.4
- Spring Data MongoDB (NO JPA, NO SQL)
- Spring WebSocket con STOMP + SockJS
- Spring Security (deshabilitado para dev, con comentarios para prod)
- Lombok
- Bean Validation (jakarta.validation)
- Maven

Dependencia pom.xml adicional para WebSocket:
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

═══════════════════════════════════════════
ESTRUCTURA DE PAQUETES (obligatoria)
═══════════════════════════════════════════
com.workflow
├── WorkflowApplication.java
├── config/
│   ├── MongoConfig.java
│   ├── WebSocketConfig.java        ← STOMP broker + SockJS
│   ├── SecurityConfig.java
│   └── DataSeeder.java             ← perfil dev
├── document/                       ← @Document MongoDB
│   ├── Empresa.java
│   ├── Departamento.java
│   ├── Rol.java
│   ├── Usuario.java
│   ├── TipoComponente.java
│   ├── Formulario.java             ← embebe Componente[]
│   ├── PoliticaNegocio.java
│   ├── Flujo.java                  ← auto-relación flujoPadreId
│   ├── Tramite.java                ← embebe Actividad[] → embebe DatosClienteForm → embebe DatoForm[] y Observacion[]
│   ├── Bitacora.java
│   └── Notificacion.java
├── repository/                     ← MongoRepository por colección
├── service/
│   ├── WorkflowService.java        ← motor principal
│   ├── FormularioRealtimeService.java ← WebSocket de formularios
│   └── CrudServices.java
├── controller/
│   ├── WorkflowController.java
│   ├── FormularioController.java
│   ├── EmpresaController.java
│   ├── UsuarioController.java
│   ├── PoliticaController.java
│   ├── FlujoController.java
│   └── NotificacionController.java
├── websocket/
│   ├── WorkflowWebSocketController.java  ← @MessageMapping
│   └── WebSocketEventListener.java       ← conectar/desconectar
├── dto/
│   └── [DTOs de request/response por entidad]
└── exception/
    ├── WorkflowException.java
    └── GlobalExceptionHandler.java

═══════════════════════════════════════════
MODELO DE DATOS MONGODB — DECISIONES NOSQL
═══════════════════════════════════════════
REGLA GENERAL: si los datos siempre se leen juntos → embeber. Si se consultan independientemente → colección propia con referencia por String ID.

COLECCIONES PROPIAS (no embeber):
- empresas
- departamentos  (campo: empresaId String)
- roles          (catálogo: "cliente","recepcionista","administrador","tecnico","legal")
- usuarios       (campos: rolId, empresaId — NO hay tablas recepcionista/administrador separadas, el rol diferencia el tipo)
- tipo_componentes (catálogo: "texto","textarea","select","boolean","fecha","archivo","numero")
- formularios    (embebe List<Componente> — los componentes SIEMPRE se leen con su formulario)
- politicas_negocio (campos: nombre, tipo[secuencial|paralelo|iterativo], estado[borrador|activa|archivada], version INT, empresaId, creadoPorId)
- flujos         (campos: politicaId, formularioId, orden, nombre, condicionCampo, condicionValor, esObligatorio, flujoPadreId nullable — auto-relación para flujos condicionales)
- tramites       (embebe List<Actividad>, cada Actividad embebe DatosClienteForm, DatosClienteForm embebe List<DatoForm> y List<Observacion>)
- bitacora
- notificaciones

TRAMITE es el documento central — campos obligatorios:
id, politicaId, clienteId, recepcionistaId, empresaId, estado[pendiente|proceso|urgente|completado|cancelado], semaforo[verde|amarillo|rojo], fecha, fechaFin

ACTIVIDAD (embebida en Tramite) — campos:
id(UUID), flujoId, usuarioId, departamentoId, nombre, estado[espera|activo|completado|rechazado], tiempoLimite(horas), semaforo, fechaInicio, fechaFin, observacion, datosForm(DatosClienteForm)

DATOS_CLIENTE_FORM (embebida en Actividad) — campos:
formularioId, estado[en_proceso|completado|observado], List<DatoForm>, List<Observacion>, fecha

DATO_FORM (embebido en DatosClienteForm) — campos:
componenteId, etiqueta, valor

OBSERVACION (embebida en DatosClienteForm) — campos:
usuarioId, descripcion, estado[observado|aprobado|rechazado], fecha

COMPONENTE (embebido en Formulario) — campos:
id(String UUID), tipoId, etiqueta, placeholder, orden, posicionX, posicionY, requerido

FLUJO — lógica de flujos condicionales:
Un flujo con flujoPadreId=null es raíz. Un flujo con flujoPadreId≠null es hijo condicional que se activa cuando el campo "condicionCampo" del formulario del padre tiene el valor "condicionValor". Ejemplo: flujo padre orden=2 muestra form "tipo_credito". Si el usuario eligió "vivienda", el motor activa el flujo hijo con condicionCampo="tipo_credito" condicionValor="vivienda".

MÉTODO calcularSemaforo() en Tramite:
- horasTranscurridas >= tiempoLimite → "rojo"
- horasTranscurridas >= tiempoLimite * 0.75 → "amarillo"
- else → "verde"

═══════════════════════════════════════════
WEBSOCKET — IMPLEMENTACIÓN OBLIGATORIA
═══════════════════════════════════════════

CONFIG WebSocketConfig.java:
- Registrar endpoint SockJS: /ws
- Habilitar broker simple con prefix /topic y /queue
- Prefix para @MessageMapping: /app
- Habilitar STOMP

CANALES WebSocket que DEBEN existir:

1. AUTOGUARDADO DE FORMULARIO (campo por campo):
   @MessageMapping("/formulario/campo")
   Recibe: { tramiteId, actividadId, componenteId, etiqueta, valor, usuarioId }
   Acción: guarda el dato en MongoDB dentro de Tramite.actividades[actividadId].datosForm.datos (upsert por componenteId)
   Emite a: /topic/tramite/{tramiteId}/formulario → el objeto DatoForm actualizado
   USO: cada vez que el usuario escribe en un campo del formulario, se guarda automáticamente sin botón de guardar

2. ESTADO DEL TRÁMITE EN TIEMPO REAL:
   @MessageMapping("/tramite/estado")
   Recibe: { tramiteId }
   Emite a: /topic/tramite/{tramiteId}/estado → { id, estado, semaforo, pasoActual, actividadActual }
   USO: cuando un recepcionista avanza un paso, TODAS las vistas que muestran ese trámite se actualizan solas

3. NOTIFICACIONES PUSH:
   Canal de emisión desde el servidor: /queue/notificaciones/{usuarioId}
   El servidor emite cuando: se avanza un paso, se asigna actividad, semáforo cambia a rojo
   Estructura: { tramiteId, tipo, mensaje, fecha }

4. PANEL DE RECEPCIONISTA EN VIVO:
   @MessageMapping("/panel/suscribir")
   Recibe: { usuarioId, empresaId }
   Emite a: /topic/panel/{empresaId} → lista actualizada de trámites con semáforo
   USO: el panel de la recepcionista se refresca automáticamente cuando cualquier trámite cambia

5. COLABORACIÓN EN FORMULARIO:
   @MessageMapping("/formulario/presencia")
   Recibe: { tramiteId, actividadId, usuarioId, accion } donde accion = "unirse"|"salir"|"escribiendo"
   Emite a: /topic/tramite/{tramiteId}/presencia → lista de usuarios activos en ese formulario
   USO: mostrar en la UI quién está llenando el formulario en este momento

═══════════════════════════════════════════
MOTOR DE WORKFLOW — WorkflowService.java
═══════════════════════════════════════════
Métodos obligatorios:

1. iniciarTramite(politicaId, clienteId, recepcionistaId, empresaId):
   - Busca la política activa
   - Obtiene el primer flujo (orden=1, flujoPadreId=null)
   - Crea Tramite con primera Actividad en estado "activo"
   - Emite por WebSocket a /topic/panel/{empresaId}
   - Retorna Tramite

2. avanzarPaso(tramiteId, usuarioId, observacion, List<Map<String,String>> datosForm):
   - Completa la actividad activa (estado=completado, fechaFin=now)
   - Guarda los datosForm en la actividad
   - Llama a determinarSiguienteFlujo()
   - Si hay siguiente: crea nueva Actividad embebida
   - Si no hay siguiente: cierra el trámite
   - Emite a /topic/tramite/{tramiteId}/estado
   - Emite a /queue/notificaciones/{usuarioId del siguiente responsable}
   - Emite a /topic/panel/{empresaId}

3. determinarSiguienteFlujo(politicaId, flujoActualId, datosForm):
   - Busca flujos hijos del flujo actual (flujoPadreId=flujoActualId)
   - Si hay hijos: evalúa condicionCampo/condicionValor contra datosForm
   - Si ningún hijo coincide o no hay hijos: busca siguiente flujo raíz por orden
   - Retorna Flujo o null si no hay más pasos

4. guardarCampoFormulario(tramiteId, actividadId, componenteId, etiqueta, valor):
   - Busca el Tramite
   - Encuentra la Actividad por actividadId
   - Hace upsert del DatoForm (si ya existe el componenteId lo actualiza, si no lo agrega)
   - Guarda el Tramite
   - Retorna el DatoForm actualizado
   - (Este método es llamado por el WebSocket controller de autoguardado)

5. agregarObservacion(tramiteId, actividadId, componenteId, usuarioId, descripcion, estado)

6. recalcularSemaforosTodos(empresaId):
   - Itera trámites activos de la empresa
   - Recalcula semáforo de cada uno
   - Si cambió, guarda y emite por WebSocket
   - (Llamar con @Scheduled cada 5 minutos o desde un endpoint manual)

═══════════════════════════════════════════
ENDPOINTS REST COMPLETOS
═══════════════════════════════════════════

POST   /api/empresas
GET    /api/empresas
GET    /api/empresas/{id}
PUT    /api/empresas/{id}
DELETE /api/empresas/{id}

POST   /api/usuarios
GET    /api/usuarios/{id}
GET    /api/usuarios/empresa/{empresaId}
GET    /api/usuarios/empresa/{empresaId}/rol/{rol}
PUT    /api/usuarios/{id}
DELETE /api/usuarios/{id}

POST   /api/formularios
GET    /api/formularios/empresa/{empresaId}
GET    /api/formularios/{id}
PUT    /api/formularios/{id}
DELETE /api/formularios/{id}

POST   /api/politicas
GET    /api/politicas/empresa/{empresaId}
GET    /api/politicas/empresa/{empresaId}/activas
GET    /api/politicas/{id}
PUT    /api/politicas/{id}         ← solo si estado=borrador
PATCH  /api/politicas/{id}/activar
POST   /api/politicas/{id}/nueva-version  ← clona flujos

POST   /api/flujos
GET    /api/flujos/politica/{politicaId}
GET    /api/flujos/politica/{politicaId}/raiz
GET    /api/flujos/{id}/hijos
PUT    /api/flujos/{id}
DELETE /api/flujos/{id}            ← elimina también hijos

POST   /api/workflow/iniciar
PATCH  /api/workflow/{tramiteId}/avanzar
POST   /api/workflow/{tramiteId}/observar
GET    /api/workflow/empresa/{empresaId}/activos
GET    /api/workflow/empresa/{empresaId}/urgentes
GET    /api/workflow/usuario/{usuarioId}/mis-tramites
PATCH  /api/workflow/{tramiteId}/semaforo/recalcular

GET    /api/notificaciones/usuario/{usuarioId}
GET    /api/notificaciones/usuario/{usuarioId}/pendientes
GET    /api/notificaciones/usuario/{usuarioId}/contador
PATCH  /api/notificaciones/{id}/leer
PATCH  /api/notificaciones/usuario/{usuarioId}/leer-todas

═══════════════════════════════════════════
application.properties OBLIGATORIO
═══════════════════════════════════════════
server.port=8080
server.servlet.context-path=/api
spring.data.mongodb.uri=mongodb://localhost:27017/workflow_db
spring.data.mongodb.database=workflow_db
logging.level.com.workflow=DEBUG

application-dev.properties:
spring.data.mongodb.uri=mongodb://localhost:27017/workflow_dev
spring.data.mongodb.database=workflow_dev

═══════════════════════════════════════════
DATA SEEDER (perfil dev)
═══════════════════════════════════════════
Al arrancar con perfil dev, crear automáticamente:
- 1 Empresa: "CRE Bolivia"
- 3 Departamentos: Recepción, Técnico, Legal
- 5 Roles: cliente, recepcionista, administrador, tecnico, legal
- 4 Usuarios: María(recepcionista), Jorge(admin), Carlos(tecnico), Roberto(cliente)
- 3 TipoComponente: texto, select, boolean, archivo
- 3 Formularios con componentes embebidos: "Inicio de trámite", "Evaluación técnica", "Cierre"
- 1 PoliticaNegocio activa: "Solicitud de medidor eléctrico" tipo=secuencial
- 3 Flujos en orden: Recepción(orden=1), Técnico(orden=2), Cierre(orden=3)
- Imprimir en logs los IDs de todos los objetos creados

═══════════════════════════════════════════
REGLAS DE CÓDIGO OBLIGATORIAS
═══════════════════════════════════════════
1. Usar @Data @Builder @NoArgsConstructor @AllArgsConstructor de Lombok en todos los documentos
2. Usar @CreatedDate y @LastModifiedDate con @EnableMongoAuditing en WorkflowApplication
3. Referencias entre colecciones: usar String ID (no @DBRef) para evitar N+1 queries
4. En MongoConfig: quitar el campo "_class" de los documentos con DefaultMongoTypeMapper(null)
5. Índices: @Indexed en campos de búsqueda frecuente (empresaId, usuarioId, estado, semaforo)
6. @CompoundIndex en Tramite: ('empresaId':1,'estado':1) y ('recepcionistaId':1,'semaforo':1)
7. Todos los controladores devuelven ResponseEntity con HttpStatus correcto
8. GlobalExceptionHandler maneja WorkflowException → 400, MethodArgumentNotValidException → 400, Exception → 500
9. Passwords hasheados con BCryptPasswordEncoder, nunca devolver password en respuestas
10. Los servicios son @Service con @RequiredArgsConstructor
11. WebSocket: inyectar SimpMessagingTemplate para emitir mensajes desde servicios
12. El archivo WorkflowWebSocketController.java usa @Controller (no @RestController) con @MessageMapping y @SendTo

═══════════════════════════════════════════
EJEMPLO DE FLUJO CONDICIONAL A IMPLEMENTAR
═══════════════════════════════════════════
El motor debe poder manejar este caso:
- Flujo raíz orden=1: formulario "Datos del cliente"
- Flujo raíz orden=2: formulario "Tipo de crédito" (tiene campo select con opciones: vivienda, empresa, vehiculo)
- Flujo hijo de orden=2 con condicionCampo="Tipo de crédito" condicionValor="vivienda": formulario "Crédito vivienda"
- Flujo hijo de orden=2 con condicionCampo="Tipo de crédito" condicionValor="empresa": formulario "Crédito empresa"
- Flujo hijo de orden=2 con condicionCampo="Tipo de crédito" condicionValor="vehiculo": formulario "Crédito vehículo"

Cuando el usuario llena el campo "Tipo de crédito" con "vivienda" y avanza el paso, el motor debe activar el flujo hijo correspondiente, NO el siguiente flujo raíz por orden.

═══════════════════════════════════════════
ENTREGA ESPERADA
═══════════════════════════════════════════
Generá TODOS los archivos Java completos y funcionales, en este orden:
1. pom.xml
2. application.properties y application-dev.properties
3. WorkflowApplication.java
4. Todos los @Document en document/
5. Todos los Repository en repository/
6. config/MongoConfig.java
7. config/WebSocketConfig.java  ← PRIORITARIO
8. config/SecurityConfig.java
9. service/WorkflowService.java  ← PRIORITARIO con todos los métodos del motor
10. service/FormularioRealtimeService.java  ← PRIORITARIO con autoguardado WebSocket
11. service/CrudServices.java
12. websocket/WorkflowWebSocketController.java  ← PRIORITARIO
13. websocket/WebSocketEventListener.java
14. Todos los DTOs en dto/
15. exception/WorkflowException.java y GlobalExceptionHandler.java
16. Todos los @RestController en controller/
17. config/DataSeeder.java
18. README.md con todos los endpoints REST + canales WebSocket + ejemplo de conexión desde JavaScript frontend

No omitas ningún archivo. Cada archivo debe estar completo con todos sus imports. No uses comentarios placeholder como "// implementar aquí". Todo el código debe ser funcional.


cd "d:\Douglas\UAGRM\SW1\Spring Boot Prueba\Parcial_1"
./gradlew bootRun --args='--spring.profiles.active=dev'
