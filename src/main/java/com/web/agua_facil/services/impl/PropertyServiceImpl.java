package com.web.agua_facil.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.agua_facil.models.Property;
import com.web.agua_facil.repositories.PropertyRepository;
import com.web.agua_facil.services.PropertyService;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyServiceImpl(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
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
}