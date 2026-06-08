# Estado del despliegue — continuar más tarde

Resumen de avance para retomar el despliegue donde quedó. Ver también
`DESPLIEGUE_AWS.md` (pasos generales) y `ia_service/DESPLIEGUE.md`,
`frontend_p1/DESPLIEGUE.md`.

---

## ✅ Completado

### 1. Base de datos — MongoDB Atlas (cluster gratuito)
- Cuenta creada en MongoDB Atlas, organización **"Douglas's Org - 2026..."**, proyecto **workflow**.
- Cluster **`Cluster0`** creado: plan **Free (M0)**, proveedor **AWS**, región **N. Virginia (us-east-1)**.
- Usuario de base de datos creado: `padilladouglas6_db_user`.
- Network Access: configurado para permitir conexión (IP propia + `0.0.0.0/0` para pruebas).
- Connection string final (ya usado en `.env`, `application-seed.properties` y `application-prod.properties`):
  ```
  mongodb+srv://padilladouglas6_db_user:etLbm3AFS9OdPKap@cluster0.djqhv0m.mongodb.net/workflow_db?appName=Cluster0&retryWrites=true&w=majority
  ```
- **Base poblada exitosamente** corriendo el backend con perfil `seed`
  (`.\gradlew.bat bootRun --args="--spring.profiles.active=seed --server.port=8090"`).
  Se cargaron: empresa, 9 usuarios, formularios, flujos, trámites (incluyendo
  históricos para ML y masivos), documentos/permisos/bitácora.
  Credenciales de prueba: password `123456` para todos
  (`admin1@crebolivia.com`, `admin2@crebolivia.com`, `cliente1@crebolivia.com`,
  `recepcion1@crebolivia.com`, etc.)

### 2. Configuración del proyecto backend
- `application.properties`: `storage.tipo` ahora es `${STORAGE_TIPO:local}` (override por env var).
- Nuevo perfil **`application-prod.properties`** creado:
  - `spring.data.mongodb.uri=${MONGODB_URI:<uri-atlas-por-defecto>}`
  - `storage.tipo=s3`
- `application-seed.properties`: actualizado para usar `${MONGO_URI:<uri-atlas-por-defecto>}`
  (antes apuntaba a Azure Cosmos DB — ya migrado a Atlas).

### 3. Seguridad — `.env`
- Se detectó que `.env` (con `MONGO_PASSWORD`, `JWT_SECRET`, etc.) **estaba trackeado en git**.
- Se ejecutó `git rm --cached .env` y se agregó `.env` al `.gitignore`.
- ⚠️ **Pendiente**: como esos secretos estuvieron en el historial de git, conviene
  **rotar credenciales sensibles** (sobre todo si el repo es público o se compartió).
- `.env` actualizado con las nuevas variables `MONGO_URI` / `MONGODB_URI` apuntando a Atlas.

### 4. Servicio de IA (`ia_service`)
- `.env` actualizado con la nueva `MONGO_URI` de Atlas (antes apuntaba a Azure Cosmos).
- `SPRING_API_URL` sigue en `http://localhost:8080/api` — se debe actualizar al
  dominio HTTPS del backend una vez desplegado en AWS.

---

## ⏳ Pendiente (siguiente sesión)

### A. Desplegar el servicio de IA (`ia_service`)
Recomendado: Render / Railway / Fly.io (HTTPS gratis automático). Ver
`ia_service/DESPLIEGUE.md`. Variables de entorno a configurar en la plataforma:
`GROQ_API_KEY`, `MONGO_URI`, `MONGO_DB`, `SPRING_API_URL` (actualizar luego con
la URL real del backend en AWS), `PORT`.

### B. Desplegar el backend Spring Boot en AWS (con S3) — ✅ CASI LISTO
1. ✅ Bucket S3 creado: `workflow-documentos-382284571866-us-east-1-an` (us-east-1,
   privado, CORS configurado para `localhost:4200` y `*.onrender.com`).
2. ✅ Usuario IAM `workflow-backend-s3` creado con política de solo este bucket
   (`workflow-backend-s3-policy`). Access Key generado y guardado en
   `D:\Douglas\UAGRM\SW1\Spring Boot Prueba\csvAWS\workflow-backend-s3_accessKeys.csv`.
3. ✅ Instancia EC2 lanzada: `workflow-backend` (`i-02f66df62eba425b1`,
   Ubuntu 26.04 LTS, t3.micro, Free Tier). Par de claves:
   `D:\Douglas\UAGRM\SW1\Spring Boot Prueba\csvAWS\workflow-backend-key.pem`.
   Grupo de seguridad: SSH abierto a `0.0.0.0/0` (a propósito, para presentación),
   HTTP/HTTPS abiertos a `0.0.0.0/0`.
4. ✅ Elastic IP asignada y asociada: **`52.5.41.137`**.
5. ✅ Docker + docker-compose-plugin instalados en la instancia.
6. ✅ Dominio gratuito creado en No-IP (DuckDNS estaba caído):
   **`workflow-severitech.redirectme.net`** → apunta a `52.5.41.137`
   (cuenta gratis = solo 1 hostname; recordar reconfirmar cada 30 días).
7. ✅ Repo clonado en el servidor (`~/workflow-deploy/backend`, repo puesto en
   público para poder clonar sin token).
8. ✅ Creados en el servidor (`~/workflow-deploy/`):
   - `Caddyfile` — sirve `workflow-severitech.redirectme.net`, enruta
     `/onlyoffice/*` → contenedor OnlyOffice (sin subdominio, por la limitación
     de 1 hostname gratis en No-IP) y el resto → backend:8080. Caddy gestiona
     HTTPS automático con Let's Encrypt.
   - `docker-compose.yml` — 3 servicios: `backend` (build desde `./backend`),
     `onlyoffice` (imagen oficial `onlyoffice/documentserver`), `caddy`
     (proxy + TLS, puertos 80/443).
   - `.env` — variables de producción (perfil `prod`, Mongo Atlas, S3, CORS,
     `ONLYOFFICE_URL_PUBLICA=https://workflow-severitech.redirectme.net/onlyoffice`).
     **Se generó un JWT_SECRET nuevo** (rotado) para no reusar el que estuvo
     expuesto en git. Permisos `chmod 600`.
9. ⏳ EN PROCESO: `docker compose up -d --build` corriendo en background en el
   servidor (compila el backend con Gradle + descarga imagen de OnlyOffice,
   tarda varios minutos).
10. ⏳ Pendiente verificar: `https://workflow-severitech.redirectme.net` responde,
    certificado SSL válido, login funciona, subida de documentos a S3 funciona,
    OnlyOffice carga en `/onlyoffice/`.

### C. Desplegar el frontend Angular
Render/Vercel/Netlify/Cloudflare Pages — ver `frontend_p1/DESPLIEGUE.md`.
Una vez el backend tenga su dominio HTTPS, actualizar `environment.prod.ts`
con `apiUrl` y `wsUrl` apuntando a `https://api.tu-dominio.com/api` /
`wss://api.tu-dominio.com/api/ws`.

### D. Conectar todo
- CORS del backend: agregar dominios finales del frontend e ia_service.
- Actualizar `SPRING_API_URL` en el `.env` de `ia_service` con la URL real del backend.
- Actualizar `IA_SERVICE_URL` (variable de entorno del backend) con la URL del
  servicio de IA ya desplegado.
- Probar flujo completo: login → carga de documentos a S3 → IA consultando datos.

### E. Seguridad final
- Rotar `MONGO_PASSWORD`/contraseña de Atlas y `JWT_SECRET` (estuvieron expuestos en git).
- Restringir el "Network Access" de Atlas a la Elastic IP real del EC2 (quitar `0.0.0.0/0`).
- Hacer commit de los cambios pendientes (`.gitignore`, perfiles `prod`/`seed`, etc.).
