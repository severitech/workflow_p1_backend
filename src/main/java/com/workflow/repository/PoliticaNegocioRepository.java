package com.workflow.repository;

import com.workflow.document.PoliticaNegocio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoliticaNegocioRepository extends MongoRepository<PoliticaNegocio, String> {

    List<PoliticaNegocio> findByEmpresaId(String empresaId);

    List<PoliticaNegocio> findByEmpresaIdAndEstado(String empresaId, String estado);

    /** Busca la política activa más reciente por empresa */
    Optional<PoliticaNegocio> findFirstByEmpresaIdAndEstadoOrderByVersionDesc(String empresaId, String estado);
}
