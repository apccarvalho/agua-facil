package com.web.agua_facil.services;

import java.util.List;
import java.util.Optional;

import com.web.agua_facil.models.PropertyCategory;
import com.web.agua_facil.models.TariffTier;

public interface TariffTierService {
    TariffTier saveTariffTier(TariffTier tariffTier);    
    List<TariffTier> getAllTariffTiers();   
    Optional<TariffTier> findTariffById(Long id);    
    Optional<TariffTier> findTariffTierByConsumoAndCategoria(Long consumo, PropertyCategory categoria);    
    TariffTier updateTariffTier(Long id, TariffTier tariffTierDetails); 
    void deleteTariffTier(Long id);
}
