package com.workflow.repository;

import com.workflow.document.PermisoDocumento;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PermisoDocumentoRepositorio extends MongoRepository<PermisoDocumento, String> {

    List<PermisoDocumento> findByDocumentoId(String documentoId);

    Optional<PermisoDocumento> findByDocumentoIdAndUsuarioId(String documentoId, String usuarioId);

    void deleteByDocumentoIdAndUsuarioId(String documentoId, String usuarioId);
}
