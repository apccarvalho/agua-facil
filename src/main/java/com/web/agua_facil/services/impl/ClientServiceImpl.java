package com.web.agua_facil.services.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.web.agua_facil.models.Client;
import com.web.agua_facil.services.ClientService;
import com.web.agua_facil.repositories.ClientRepository;

@Service
public class ClientServiceImpl implements ClientService {
	
	@Autowired
	private ClientRepository clientRepository;
	
	@Override
	public List <Client> getAllClients(){
		return clientRepository.findAll();
	}
	
	@Override
	public Client saveClient(Client client) {
	    return this.clientRepository.save(client);
	}
	
	@Override
	public Client getClientById(Long id) {
		Optional < Client > optional = clientRepository.findById(id);
		if (optional.isPresent()) {
			return optional.get();
		}else {
			throw new RuntimeException("Client not found with id: " + id);
		}
	}
	
	@Override
	public void deleteClientById(Long id) {
	    if (!clientRepository.existsById(id)) {
	        throw new IllegalArgumentException("Cliente não encontrado com id: " + id);
	    }
	    this.clientRepository.deleteById(id);
	}
	
}
