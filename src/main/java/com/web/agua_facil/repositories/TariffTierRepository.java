package com.web.agua_facil.repositories;

import com.web.agua_facil.models.TariffTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TariffTierRepository extends JpaRepository<TariffTier, Long> {

    @Query("SELECT t FROM TariffTier t WHERE :consumo >= t.consumoMinimo AND :consumo <= t.consumoMaximo")
    Optional<TariffTier> findTierByConsumo(@Param("consumo") Long consumo);
}