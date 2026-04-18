package com.workflow.repository;

import com.workflow.document.Departamento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartamentoRepository extends MongoRepository<Departamento, String> {

    /** Busca departamentos por empresa */
    List<Departamento> findByEmpresaId(String empresaId);

    List<Departamento> findByEmpresaIdAndActivoTrue(String empresaId);
}
