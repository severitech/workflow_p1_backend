package com.workflow.repository;

import com.workflow.document.FlujoRelacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlujoRelacionRepository extends MongoRepository<FlujoRelacion, String> {

    /** Relaciones salientes de un flujo padre */
    List<FlujoRelacion> findByPadreId(String padreId);

    /** Relaciones entrantes a un flujo hijo */
    List<FlujoRelacion> findByHijoId(String hijoId);

    /** Todas las relaciones de una política */
    List<FlujoRelacion> findByPoliticaId(String politicaId);

    /** Relaciones donde el flujo es padre o hijo — para eliminar al borrar el flujo */
    List<FlujoRelacion> findByPadreIdOrHijoId(String padreId, String hijoId);
}
