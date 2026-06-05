package com.workflow.repository;

import com.workflow.document.Flujo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlujoRepository extends MongoRepository<Flujo, String> {

    /** Todos los flujos de una política ordenados por orden */
    List<Flujo> findByPoliticaIdOrderByOrden(String politicaId);
}
