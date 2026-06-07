package com.workflow.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.Duration;
import java.util.UUID;

/**
 * Servicio de almacenamiento de archivos.
 * Soporta almacenamiento local (dev) y AWS S3 (producción).
 * Configurable con la propiedad storage.tipo=local|s3
 */
@Slf4j
@Service
public class AlmacenamientoServicio {

    @Value("${storage.tipo:local}")
    private String tipoStorage;

    @Value("${storage.local.directorio:uploads}")
    private String directorioLocal;

    @Value("${aws.s3.bucket:workflow-documentos}")
    private String bucket;

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.access-key-id:}")
    private String accessKey;

    @Value("${aws.secret-access-key:}")
    private String secretKey;

    private S3Client s3Client;
    private S3Presigner presigner;

    @PostConstruct
    public void inicializar() {
        if ("s3".equals(tipoStorage)) {
            inicializarS3();
        } else {
            inicializarAlmacenamientoLocal();
        }
    }

    private void inicializarS3() {
        try {
            var credenciales = AwsBasicCredentials.create(accessKey, secretKey);
            s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credenciales))
                    .build();
            presigner = S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credenciales))
                    .build();
            log.info("[Almacenamiento] Modo S3 — bucket: {}", bucket);
        } catch (Exception e) {
            log.error("[Almacenamiento] Error al inicializar S3, cambiando a local: {}", e.getMessage());
            tipoStorage = "local";
            inicializarAlmacenamientoLocal();
        }
    }

    private void inicializarAlmacenamientoLocal() {
        try {
            Files.createDirectories(Paths.get(directorioLocal));
            log.info("[Almacenamiento] Modo local — directorio: {}", directorioLocal);
        } catch (IOException e) {
            log.error("[Almacenamiento] No se pudo crear el directorio local: {}", e.getMessage());
        }
    }

    /**
     * Guarda el archivo y devuelve la clave de almacenamiento (path local o S3 key).
     * La clave incluye un UUID para evitar colisiones.
     */
    public String guardar(MultipartFile archivo, String prefijo) throws IOException {
        String extension = obtenerExtension(archivo.getOriginalFilename());
        String clave = prefijo + "/" + UUID.randomUUID() + "." + extension;

        if ("s3".equals(tipoStorage)) {
            guardarEnS3(archivo, clave);
        } else {
            guardarEnLocal(archivo, clave);
        }
        return clave;
    }

    /** Guarda bytes crudos (para archivos generados programáticamente) */
    public String guardarBytes(byte[] bytes, String prefijo, String extension) throws IOException {
        String clave = prefijo + "/" + UUID.randomUUID() + "." + extension;
        if ("s3".equals(tipoStorage)) {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(clave).build(),
                    RequestBody.fromBytes(bytes)
            );
        } else {
            Path destino = Paths.get(directorioLocal, clave);
            Files.createDirectories(destino.getParent());
            Files.write(destino, bytes);
        }
        return clave;
    }

    /** Lee y devuelve el contenido completo de un archivo como bytes */
    public byte[] leerBytes(String clave) throws IOException {
        if ("s3".equals(tipoStorage)) {
            return s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(clave).build()
            ).asByteArray();
        } else {
            return Files.readAllBytes(Paths.get(directorioLocal, clave));
        }
    }

    private void guardarEnS3(MultipartFile archivo, String clave) throws IOException {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(clave)
                        .contentType(archivo.getContentType())
                        .build(),
                RequestBody.fromBytes(archivo.getBytes())
        );
    }

    private void guardarEnLocal(MultipartFile archivo, String clave) throws IOException {
        Path destino = Paths.get(directorioLocal, clave);
        Files.createDirectories(destino.getParent());
        Files.write(destino, archivo.getBytes());
    }

    /**
     * Genera URL de descarga.
     * - S3: URL prefirmada con expiración de 15 minutos
     * - Local: ruta relativa que el controlador sirve como stream
     */
    public String generarUrlDescarga(String clave) {
        if ("s3".equals(tipoStorage)) {
            return generarUrlPresignadaS3(clave);
        }
        // Para local, el controlador expone GET /api/documentos/archivo/{clave}
        return "/api/documentos/archivo/" + clave;
    }

    private String generarUrlPresignadaS3(String clave) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(clave)
                        .build())
                .build();
        return presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * Obtiene el archivo como Resource (solo para almacenamiento local).
     */
    public Resource obtenerComoResource(String clave) throws MalformedURLException {
        Path ruta = Paths.get(directorioLocal, clave);
        return new UrlResource(ruta.toUri());
    }

    /** Elimina el archivo del almacenamiento (usado al eliminar versiones antiguas). */
    public void eliminar(String clave) {
        try {
            if ("s3".equals(tipoStorage)) {
                s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(clave).build());
            } else {
                Files.deleteIfExists(Paths.get(directorioLocal, clave));
            }
        } catch (Exception e) {
            log.warn("[Almacenamiento] No se pudo eliminar la clave {}: {}", clave, e.getMessage());
        }
    }

    public boolean esAlmacenamientoLocal() {
        return !"s3".equals(tipoStorage);
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) return "bin";
        return nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1).toLowerCase();
    }
}
