package com.web.agua_facil.services.impl;

import com.web.agua_facil.models.Service;
import com.web.agua_facil.repositories.ServiceRepository;
import com.web.agua_facil.services.ServiceService;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;

@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceServiceImpl(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }
    
    @Override
    public Service saveService(Service service) {
        return serviceRepository.save(service);
    }

    @Override
    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    @Override
    public Optional<Service> findServiceById(Long id) {
        return serviceRepository.findById(id);
    }

    @Override
    public Service updateService(Long id, Service serviceDetails) {
        return serviceRepository.findById(id).map(existingService -> {
            existingService.setNome(serviceDetails.getNome());
            existingService.setDescricao(serviceDetails.getDescricao());
            existingService.setValor(serviceDetails.getValor());
                        
            return serviceRepository.save(existingService);
        }).orElseThrow(() -> new RuntimeException("Serviço não encontrado com o ID: " + id));

    }

    @Override
    public void deleteService(Long id) {
        Service existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado com o ID: " + id));
                
        try {
            serviceRepository.delete(existingService);
            serviceRepository.flush(); 
            
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Ação bloqueada: Não é possível excluir este serviço, pois ele já está vinculado a faturas geradas no histórico do sistema.");
        }
    }
}