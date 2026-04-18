package com.workflow.repository;

import com.workflow.document.Formulario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormularioRepository extends MongoRepository<Formulario, String> {

    List<Formulario> findByEmpresaId(String empresaId);

    List<Formulario> findByEmpresaIdAndActivoTrue(String empresaId);
}
