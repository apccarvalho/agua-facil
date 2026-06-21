package com.web.agua_facil.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web.agua_facil.models.Reading;

@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {

    // Busca todas as leituras de um imóvel, ordenadas da mais recente para a mais antiga
    List<Reading> findByPropertyIdOrderByDataLeituraDesc(Long propertyId);

    // Busca a leitura de um imóvel em uma data específica
    Optional<Reading> findByPropertyIdAndDataLeitura(Long propertyId, LocalDate dataLeitura);

    // Busca a leitura imediatamente anterior a uma data dada para um imóvel específico. Utilizado para validar se a leitura está correta.
    Optional<Reading> findFirstByPropertyIdAndDataLeituraBeforeOrderByDataLeituraDesc(Long propertyId, LocalDate dataLeitura);
    
    // Busca todas as leituras de um cliente específico, ordenadas da mais recente para a mais antiga
    @EntityGraph(attributePaths = {"property"})
    List<Reading> findByProperty_ClienteIdOrderByDataLeituraDesc(Long clienteId);
}