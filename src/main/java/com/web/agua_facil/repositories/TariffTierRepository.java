package com.web.agua_facil.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.web.agua_facil.models.PropertyCategory;
import com.web.agua_facil.models.TariffTier;

@Repository
public interface TariffTierRepository extends JpaRepository<TariffTier, Long> {

	@Query("SELECT t FROM TariffTier t WHERE :consumo BETWEEN t.consumoMinimo AND t.consumoMaximo AND t.categoria = :categoria")
    Optional<TariffTier> findTierByConsumoAndCategoria(@Param("consumo") Long consumo, @Param("categoria") PropertyCategory categoria);
}