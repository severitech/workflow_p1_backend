package com.workflow.repository;

import com.workflow.document.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    Optional<Usuario> findByCorreo(String correo);

    List<Usuario> findByEmpresaId(String empresaId);

    /** Busca usuarios de una empresa filtrando por rolId */
    List<Usuario> findByEmpresaIdAndRolId(String empresaId, String rolId);

    List<Usuario> findByEmpresaIdAndActivoTrue(String empresaId);

    /** Busca usuarios por departamento */
    List<Usuario> findByDepartamentoId(String departamentoId);
}
