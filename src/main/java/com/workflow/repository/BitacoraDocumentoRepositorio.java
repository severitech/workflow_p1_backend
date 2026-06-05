package com.workflow.repository;

import com.workflow.document.BitacoraDocumento;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BitacoraDocumentoRepositorio extends MongoRepository<BitacoraDocumento, String> {

    /** Bitácora de un documento ordenada por fecha descendente */
    List<BitacoraDocumento> findByDocumentoIdOrderByFechaDesc(String documentoId);
}
