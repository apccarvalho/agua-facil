package com.web.agua_facil.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web.agua_facil.models.Property;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    Optional<Property> findByMatricula(String matricula);

    List<Property> findByClienteId(Long clienteId);

    boolean existsByMatricula(String matricula);
}