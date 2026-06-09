package com.workflow.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Servicio de notificaciones push via Firebase Cloud Messaging (FCM).
 * Inicializa Firebase Admin SDK con el service account del classpath.
 */
@Slf4j
@Service
public class FcmServicio {

    private boolean inicializado = false;

    @PostConstruct
    public void inicializar() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = getClass().getClassLoader()
                        .getResourceAsStream("firebase-service-account.json");
                if (serviceAccount == null) {
                    log.warn("[FCM] firebase-service-account.json no encontrado en classpath — push deshabilitado");
                    return;
                }
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
            }
            inicializado = true;
            log.info("[FCM] Firebase Admin SDK inicializado correctamente");
        } catch (Exception e) {
            log.error("[FCM] Error al inicializar Firebase: {}", e.getMessage());
        }
    }

    /**
     * Envía una notificación push al dispositivo con el token dado.
     * Si FCM no está inicializado o el token es nulo, no hace nada.
     */
    public void enviar(String tokenDispositivo, String titulo, String cuerpo) {
        if (!inicializado || tokenDispositivo == null || tokenDispositivo.isBlank()) return;
        try {
            Message mensaje = Message.builder()
                    .setToken(tokenDispositivo)
                    .setNotification(Notification.builder()
                            .setTitle(titulo)
                            .setBody(cuerpo)
                            .build())
                    .build();
            String respuesta = FirebaseMessaging.getInstance().send(mensaje);
            log.debug("[FCM] Mensaje enviado: {}", respuesta);
        } catch (Exception e) {
            log.warn("[FCM] Error al enviar push a token {}: {}", tokenDispositivo, e.getMessage());
        }
    }
}
