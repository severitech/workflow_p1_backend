package com.workflow.repository;

import com.workflow.document.Empresa;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpresaRepository extends MongoRepository<Empresa, String> {

    /** Busca empresas activas */
    List<Empresa> findByActivoTrue();
}
