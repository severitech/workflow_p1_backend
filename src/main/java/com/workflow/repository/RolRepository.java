package com.workflow.repository;

import com.workflow.document.Rol;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends MongoRepository<Rol, String> {

    /** Busca un rol por nombre */
    Optional<Rol> findByNombre(String nombre);
}
