package com.workflow.repository;

import com.workflow.document.Bitacora;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BitacoraRepository extends MongoRepository<Bitacora, String> {

    List<Bitacora> findByTramiteIdOrderByFechaDesc(String tramiteId);

    List<Bitacora> findByUsuarioIdOrderByFechaDesc(String usuarioId);
}
