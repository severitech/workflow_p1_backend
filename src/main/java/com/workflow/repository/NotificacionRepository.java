package com.workflow.repository;

import com.workflow.document.Notificacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends MongoRepository<Notificacion, String> {

    List<Notificacion> findByUsuarioIdOrderByFechaDesc(String usuarioId);

    List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByFechaDesc(String usuarioId);

    long countByUsuarioIdAndLeidaFalse(String usuarioId);
}
