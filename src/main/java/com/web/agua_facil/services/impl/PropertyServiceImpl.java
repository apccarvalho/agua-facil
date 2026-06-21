package com.web.agua_facil.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.agua_facil.models.Bill;
import com.web.agua_facil.models.Property;
import com.web.agua_facil.repositories.BillRepository;
import com.web.agua_facil.repositories.PropertyRepository;
import com.web.agua_facil.services.PropertyService;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final BillRepository billRepository;

    public PropertyServiceImpl(PropertyRepository propertyRepository, BillRepository billRepository) {
        this.propertyRepository = propertyRepository;
        this.billRepository = billRepository;
    }

    @Override
    @Transactional
    public Property saveProperty(Property property) {

        if (propertyRepository.existsByMatricula(property.getMatricula())) {
            throw new IllegalArgumentException("Já existe um imóvel registado com a matrícula: " + property.getMatricula());
        }
        return propertyRepository.save(property);
    }

    @Override
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    @Override
    public Optional<Property> findPropertyById(Long id) {
        return propertyRepository.findById(id);
    }

    @Override
    public Optional<Property> findPropertyByMatricula(String matricula) {
        return propertyRepository.findByMatricula(matricula);
    }

    @Override
    public List<Property> findPropertiesByClienteId(Long clienteId) {
        return propertyRepository.findByClienteId(clienteId);
    }

    @Override
    @Transactional
    public Property updateProperty(Long id, Property propertyDetails) {
        Property existingProperty = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imóvel não encontrado."));

        if (!existingProperty.getMatricula().equals(propertyDetails.getMatricula()) &&
            propertyRepository.existsByMatricula(propertyDetails.getMatricula())) {
            throw new IllegalArgumentException("Já existe outro imóvel registado com a matrícula: " + propertyDetails.getMatricula());
        }

        existingProperty.setMatricula(propertyDetails.getMatricula());
        existingProperty.setLogradouro(propertyDetails.getLogradouro());
        existingProperty.setNumero(propertyDetails.getNumero());
        existingProperty.setBairro(propertyDetails.getBairro());
        existingProperty.setCidade(propertyDetails.getCidade());
        existingProperty.setUf(propertyDetails.getUf());
        existingProperty.setCep(propertyDetails.getCep());
        existingProperty.setCategoria(propertyDetails.getCategoria());
        existingProperty.setCliente(propertyDetails.getCliente());

        return propertyRepository.save(existingProperty);
    }

    @Override
    @Transactional
    public void deleteProperty(Long id) {
        if (!propertyRepository.existsById(id)) {
            throw new IllegalArgumentException("Imóvel não encontrado.");
        }
        propertyRepository.deleteById(id);
    }
    
    @Override
    public Map<String, Object> getPropertyHistoryData(Long propertyId) {

        Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new IllegalArgumentException("Imóvel não encontrado."));

        List<Bill> bills = billRepository.findBillsByPropertyId(propertyId);
        bills.sort(Comparator.comparing(Bill::getDataVencimento));

        List<String> mesesLabels = bills.stream().map(Bill::getMesReferencia).collect(Collectors.toList());
        List<Long> consumosData = bills.stream().map(Bill::getConsumo).collect(Collectors.toList());

        Map<String, Object> dados = new HashMap<>();
        dados.put("property", property);
        dados.put("billsList", bills);
        dados.put("mesesLabels", mesesLabels);
        dados.put("consumosData", consumosData);

        return dados;
    }
    
}