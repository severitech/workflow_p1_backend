package com.workflow.repository;

import com.workflow.document.Documento;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DocumentoRepositorio extends MongoRepository<Documento, String> {

    /** Documentos activos de un trámite */
    List<Documento> findByTramiteIdAndActivoTrue(String tramiteId);

    /** Repositorio general de una empresa (sin tramite asociado) */
    List<Documento> findByEmpresaIdAndTramiteIdIsNullAndActivoTrue(String empresaId);

    /** Todos los documentos activos de una empresa */
    List<Documento> findByEmpresaIdAndActivoTrue(String empresaId);

    /** Documentos de una actividad específica */
    List<Documento> findByActividadIdAndActivoTrue(String actividadId);
}
