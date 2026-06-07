package com.web.agua_facil.services;

import java.util.List;

import com.web.agua_facil.models.Client;

public interface ClientService {
	List <Client> getAllClients();
	Client saveClient(Client client);
	Client getClientById(Long id);
	void deleteClientById(Long id);
}
