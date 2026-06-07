# Despliegue del backend (Spring Boot) en AWS con S3

Este backend ya está preparado para usar S3 (ver `application.properties`, claves
`aws.s3.bucket`, `aws.region`, `aws.access-key-id`, `aws.secret-access-key`,
y la propiedad `documentos.almacenamiento` que alterna entre `local` y `s3`).

Con la capa gratuita de AWS (Free Tier) puedes desplegar todo esto sin costo
durante 12 meses (EC2 t2.micro/t3.micro + 5 GB de S3).

---

## 1. Crear el bucket S3 para los documentos

1. Entra a la consola de AWS → **S3** → **Create bucket**.
2. Nombre del bucket: algo único, p. ej. `workflow-documentos-tuempresa`.
3. Región: la misma que usarás en EC2 (p. ej. `us-east-1`).
4. Bloquea el acceso público (Block all public access = ON). El backend
   generará URLs prefirmadas (`generarUrlDescarga`), no necesitas que el
   bucket sea público.
5. Habilita el versionado si quieres histórico de archivos (opcional).
6. Crea el bucket.

### CORS del bucket (necesario porque OnlyOffice y el navegador acceden directo)
En el bucket → **Permissions** → **CORS**, agrega:

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST", "HEAD"],
    "AllowedOrigins": ["https://tu-dominio-frontend.com", "http://localhost:4200"],
    "ExposeHeaders": ["ETag"]
  }
]
```

---

## 2. Crear un usuario IAM con permisos solo sobre ese bucket

1. **IAM** → **Users** → **Create user** (p. ej. `workflow-backend-s3`).
2. No le des acceso a la consola, solo **Programmatic access** (Access key).
3. Asigna una política personalizada (principio de mínimo privilegio), por ejemplo:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["s3:GetObject", "s3:PutObject", "s3:DeleteObject", "s3:ListBucket"],
      "Resource": [
        "arn:aws:s3:::workflow-documentos-tuempresa",
        "arn:aws:s3:::workflow-documentos-tuempresa/*"
      ]
    }
  ]
}
```

4. Genera un **Access key** (Access key ID + Secret access key) y guárdalos:
   los necesitarás como variables de entorno `AWS_ACCESS_KEY_ID` y
   `AWS_SECRET_ACCESS_KEY`. **Nunca los subas al repositorio.**

---

## 3. Levantar la instancia EC2 (Free Tier)

1. **EC2** → **Launch instance**.
2. AMI: Amazon Linux 2023 (o Ubuntu 22.04).
3. Tipo de instancia: `t2.micro` o `t3.micro` (elegible para Free Tier).
4. Crea/selecciona un par de claves (`.pem`) para SSH.
5. Configura el **Security Group**:
   - Puerto 22 (SSH) — solo desde tu IP.
   - Puerto 80/443 (HTTP/HTTPS) — público, para el reverse proxy con SSL.
   - Puerto 8080 — solo si necesitas exponerlo directo (lo normal es ocultarlo
     detrás de Nginx/Caddy y exponer solo 443).
6. Lanza la instancia y asígnale una **Elastic IP** (gratis mientras esté
   asociada a una instancia corriendo) para tener una IP fija.

### Instalar Docker en la instancia
```bash
sudo yum update -y                 # Amazon Linux
sudo yum install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user
# vuelve a iniciar sesión SSH para aplicar el grupo
```

---

## 4. SSL/HTTPS sin pagar nada (Let's Encrypt + Caddy o Nginx + Certbot)

Como tienes la cuenta gratuita, no uses ACM+ALB (tiene costo). La forma más
simple y gratuita de tener HTTPS es:

1. Compra/usa un dominio (o subdominio gratuito tipo DuckDNS/No-IP) que
   apunte a la Elastic IP de tu instancia (registro A).
2. Usa **Caddy** como reverse proxy: obtiene y renueva certificados de
   Let's Encrypt automáticamente, sin configuración manual.

`Caddyfile` de ejemplo (en la instancia EC2):
```
api.tu-dominio.com {
    reverse_proxy localhost:8080
}
```

Levántalo con Docker:
```bash
docker run -d --name caddy -p 80:80 -p 443:443 \
  -v $PWD/Caddyfile:/etc/caddy/Caddyfile \
  -v caddy_data:/data \
  caddy:2
```

Caddy emitirá el certificado SSL automáticamente la primera vez que reciba
tráfico HTTPS para ese dominio (necesita que el puerto 80/443 esté abierto y
el DNS ya apunte a la IP).

> Alternativa: Nginx + Certbot (`certbot --nginx`) si prefieres Nginx.

---

## 5. Variables de entorno para producción

En la instancia EC2, crea un archivo `.env` (no lo subas a git) con:

```
SPRING_PROFILES_ACTIVE=prod
DOCUMENTOS_ALMACENAMIENTO=s3
AWS_S3_BUCKET=workflow-documentos-tuempresa
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=xxxxxxxxxxxxxxxx
AWS_SECRET_ACCESS_KEY=xxxxxxxxxxxxxxxxxxxxxxxxxxxx
SPRING_DATASOURCE_URL=jdbc:postgresql://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
```

Si no quieres pagar una base de datos administrada (RDS tiene Free Tier
limitado a 12 meses), puedes correr Postgres/MySQL en un contenedor Docker
en la misma instancia (recuerda hacer backups a S3 con un cron).

---

## 6. Construir y desplegar con Docker

Ya existe un `Dockerfile` y `docker-compose.yml` en este proyecto. En la
instancia EC2:

```bash
git clone <tu-repo> workflow_backend
cd workflow_backend
# copia tu .env aquí
docker compose --env-file .env up -d --build
```

Verifica que el contenedor esté corriendo y escuchando en `localhost:8080`,
y que Caddy/Nginx esté enrutando `https://api.tu-dominio.com` hacia él.

---

## 7. Notas sobre la IA (otro servicio)

El backend Spring Boot debe ir en AWS, pero el `ia_service` y el frontend
Angular pueden ir en **otros proveedores gratuitos** (Render, Railway, Fly.io,
Vercel, Netlify, Cloudflare Pages) — ver `DESPLIEGUE.md` en cada uno de esos
proyectos. Solo asegúrate de:

- Configurar CORS en el backend para aceptar el dominio del frontend.
- Apuntar `environment.apiUrl` / `environment.wsUrl` del frontend al dominio
  HTTPS del backend en AWS.
- Apuntar la URL del servicio de IA (`ia_service`) desde el backend mediante
  variable de entorno (no hardcodeada).

---

## Resumen de pasos en AWS

1. Crear bucket S3 (privado, con CORS).
2. Crear usuario IAM con permisos solo sobre ese bucket → obtener Access Key.
3. Lanzar EC2 `t2.micro`/`t3.micro` con Elastic IP, abrir puertos 22/80/443.
4. Instalar Docker en la instancia.
5. Apuntar un dominio/subdominio a la Elastic IP.
6. Levantar Caddy (o Nginx+Certbot) para HTTPS automático con Let's Encrypt.
7. Configurar variables de entorno (S3, DB, etc.) y desplegar el backend con
   `docker compose up -d --build`.
8. Probar `https://api.tu-dominio.com/api/...` con SSL válido.
