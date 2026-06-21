package com.web.agua_facil.services.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.agua_facil.models.TariffTier;
import com.web.agua_facil.repositories.TariffTierRepository;
import com.web.agua_facil.services.TariffTierService;

@Service
public class TariffTierServiceImpl implements TariffTierService {

    private final TariffTierRepository tariffTierRepository;

    public TariffTierServiceImpl(TariffTierRepository tariffTierRepository) {
        this.tariffTierRepository = tariffTierRepository;
    }

    @Override
    @Transactional
    public TariffTier saveTariffTier(TariffTier tariffTier) {
        
        if (tariffTier.getConsumoMinimo() > tariffTier.getConsumoMaximo()) {
            throw new IllegalArgumentException(
                "Erro: O consumo mínimo (" + tariffTier.getConsumoMinimo() + 
                " m³) não pode ser maior que o consumo máximo (" + tariffTier.getConsumoMaximo() + " m³)."
            );
        }

        if (tariffTier.getValorPorM3().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Erro: O valor por m³ não pode ser um número negativo.");
        }

        return tariffTierRepository.save(tariffTier);
    }
    
    @Override
    public List<TariffTier> getAllTariffTiers() {
        return tariffTierRepository.findAll();
    }	
    
    @Override
    public Optional<TariffTier> findTariffById(Long id) {
        return tariffTierRepository.findById(id);
    }
    
    @Override
    public Optional<TariffTier> findTariffTierByConsumo(Long consumo) {
        if (consumo < 0) {
            throw new IllegalArgumentException("Erro: O consumo medido não pode ser negativo.");
        }
        
        return tariffTierRepository.findTierByConsumo(consumo);
    }
    
    @Override
    @Transactional
    public TariffTier updateTariffTier(Long id, TariffTier tariffTierDetails) {
        
        TariffTier existingTier = tariffTierRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Erro: Faixa de tarifa com ID " + id + " não encontrada."));

        if (tariffTierDetails.getConsumoMinimo() > tariffTierDetails.getConsumoMaximo()) {
            throw new IllegalArgumentException(
                "Erro: O consumo mínimo não pode ser maior que o consumo máximo."
            );
        }

        if (tariffTierDetails.getValorPorM3().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Erro: O valor por m³ não pode ser negativo.");
        }

        existingTier.setDescricao(tariffTierDetails.getDescricao());
        existingTier.setConsumoMinimo(tariffTierDetails.getConsumoMinimo());
        existingTier.setConsumoMaximo(tariffTierDetails.getConsumoMaximo());
        existingTier.setValorPorM3(tariffTierDetails.getValorPorM3());

        return tariffTierRepository.save(existingTier);
    }
    
    @Override
    @Transactional
    public void deleteTariffTier(Long id) {
        
        if (!tariffTierRepository.existsById(id)) {
            throw new IllegalArgumentException("Erro: Não é possível excluir. Faixa de tarifa com ID " + id + " não encontrada.");
        }

        tariffTierRepository.deleteById(id);
    }
    
}