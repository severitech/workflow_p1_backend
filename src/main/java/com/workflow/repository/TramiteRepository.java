package com.workflow.repository;

import com.workflow.document.Tramite;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TramiteRepository extends MongoRepository<Tramite, String> {

    List<Tramite> findByEmpresaIdAndEstadoIn(String empresaId, List<String> estados);

    List<Tramite> findByEmpresaId(String empresaId);

    /** Trámites urgentes por empresa (semáforo rojo) */
    List<Tramite> findByEmpresaIdAndSemaforo(String empresaId, String semaforo);

    List<Tramite> findByClienteId(String clienteId);

    List<Tramite> findByRecepcionistaId(String recepcionistaId);
}
