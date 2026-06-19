package com.web.agua_facil.services.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.web.agua_facil.models.Client;
import com.web.agua_facil.models.User;
import com.web.agua_facil.models.UserRole;
import com.web.agua_facil.services.ClientService;
import com.web.agua_facil.services.UserService;

import jakarta.transaction.Transactional;

import com.web.agua_facil.repositories.ClientRepository;

@Service
public class ClientServiceImpl implements ClientService {
	
	private final ClientRepository clientRepository;
	private final UserService userService;

	public ClientServiceImpl(ClientRepository clientRepository, UserService userService) {
	    this.clientRepository = clientRepository;
	    this.userService = userService;
	}
	
	@Override
	public List <Client> getAllClients(){
		return clientRepository.findAll();
	}
	
	@Transactional
    @Override
    public Client saveClient(Client client) {
        User user = client.getUser();
        user.setRole(UserRole.CLIENTE);

        User savedUser = userService.saveUser(user, client.getCpf());

        client.setUser(savedUser);
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
