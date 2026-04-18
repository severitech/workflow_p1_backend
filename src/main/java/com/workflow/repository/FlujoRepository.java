package com.workflow.repository;

import com.workflow.document.Flujo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlujoRepository extends MongoRepository<Flujo, String> {

    List<Flujo> findByPoliticaIdOrderByOrden(String politicaId);

    /** Flujos raíz (sin padre) de una política */
    List<Flujo> findByPoliticaIdAndFlujoPadreIdIsNullOrderByOrden(String politicaId);

    /** Flujos hijos de un flujo padre */
    List<Flujo> findByFlujoPadreId(String flujoPadreId);

    /** Siguiente flujo raíz por orden */
    java.util.Optional<Flujo> findFirstByPoliticaIdAndFlujoPadreIdIsNullAndOrdenGreaterThanOrderByOrden(
            String politicaId, int orden);
}
