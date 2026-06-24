package com.web.agua_facil.services;

import java.util.List;
import java.util.Optional;

import com.web.agua_facil.models.Service;


public interface ServiceService {
    Service saveService(Service Service);    
    List<Service> getAllServices();   
    Optional<Service> findServiceById(Long id);    
    Service updateService(Long id, Service serviceDetails);
    void deleteService(Long id);
}
