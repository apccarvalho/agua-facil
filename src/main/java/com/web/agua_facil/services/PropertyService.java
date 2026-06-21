package com.web.agua_facil.services;

import com.web.agua_facil.models.Property;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PropertyService {
    
    Property saveProperty(Property property);    
    List<Property> getAllProperties();   
    Optional<Property> findPropertyById(Long id);    
    Optional<Property> findPropertyByMatricula(String matricula);    
    List<Property> findPropertiesByClienteId(Long clienteId);    
    Property updateProperty(Long id, Property propertyDetails); 
    void deleteProperty(Long id);
    Map<String, Object> getPropertyHistoryData(Long propertyId);
}