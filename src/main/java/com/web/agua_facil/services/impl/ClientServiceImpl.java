package com.web.agua_facil.services.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.web.agua_facil.models.Client;
import com.web.agua_facil.models.UserRole;
import com.web.agua_facil.services.ClientService;
import com.web.agua_facil.repositories.ClientRepository;
import com.web.agua_facil.repositories.UserRepository;

@Service
public class ClientServiceImpl implements ClientService {
	
	private final ClientRepository clientRepository;
	private final UserRepository userRepository;

	public ClientServiceImpl(ClientRepository clientRepository, UserRepository userRepository) {
	    this.clientRepository = clientRepository;
	    this.userRepository = userRepository;
	}
	
	@Override
	public List <Client> getAllClients(){
		return clientRepository.findAll();
	}
	
	@Override
	public Client saveClient(Client client) {
	    client.getUser().setRole(UserRole.CLIENTE);
	    userRepository.save(client.getUser());
	    return clientRepository.save(client);
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
