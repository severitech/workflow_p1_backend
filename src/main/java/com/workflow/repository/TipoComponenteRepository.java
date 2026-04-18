package com.workflow.repository;

import com.workflow.document.TipoComponente;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoComponenteRepository extends MongoRepository<TipoComponente, String> {

    Optional<TipoComponente> findByNombre(String nombre);
}
