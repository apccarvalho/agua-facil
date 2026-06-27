package com.web.agua_facil.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.web.agua_facil.config.SecurityConfig;
import com.web.agua_facil.config.CustomAuthenticationSuccessHandler;
import com.web.agua_facil.models.Client;
import com.web.agua_facil.models.User;
import com.web.agua_facil.models.UserRole;
import com.web.agua_facil.services.ClientService;
import com.web.agua_facil.services.impl.UserDetailsServiceImpl;

@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
public class ClientControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ClientService clientService;

	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;
	@MockitoBean
	private CustomAuthenticationSuccessHandler successHandler;

	@AfterEach
	void resetMocks() {
		reset(clientService);
	}

	private Client testCreateSingleClient() {
		Client client = new Client();
		client.setId(1L);
		client.setCpf("123.456.789-00");
		client.setRua("Rua das Flores");
		client.setNumero("123");
		client.setBairro("Centro");
		client.setTelefone("34999999999");
		
		User user = new User();
		user.setNome("Cliente Teste");
		user.setId(1L);
		user.setEmail("joao@aguafacil.com");
		user.setRole(UserRole.CLIENTE);
		
		client.setUser(user);
		return client;
	}
	
	private List<Client> testCreateClientList() {
		return List.of(testCreateSingleClient());
	}

	// =========================================================================
	// TESTES DE AUTORIZAÇÃO E AUTENTICAÇÃO
	// =========================================================================

	@Test
	@DisplayName("GET /client - Sem usuário logado deve redirecionar para o login")
	void testIndexNotAuthenticatedUser() throws Exception {
		mockMvc.perform(get("/client")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("**/login"));
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	@DisplayName("GET /client - Usuário com papel CLIENTE deve ter acesso negado (403)")
	void testIndexForbiddenForCliente() throws Exception {
		mockMvc.perform(get("/client")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /client - Listar clientes na tela index com FUNCIONARIO logado")
	void testIndexAuthenticatedFuncionario() throws Exception {
		when(clientService.getAllClients()).thenReturn(testCreateClientList());

		mockMvc.perform(get("/client")).andDo(print())
				.andExpect(status().isOk()).andExpect(view().name("client/index"))
				.andExpect(model().attributeExists("clientsList"));
	}

	// =========================================================================
	// TESTES DE EXIBIÇÃO DE FORMULÁRIOS
	// =========================================================================

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /client/create - Exibe formulário de criação")
	void testCreateFormAuthorizedUser() throws Exception {
		mockMvc.perform(get("/client/create")).andDo(print()).andExpect(status().isOk())
				.andExpect(view().name("client/create")).andExpect(model().attributeExists("client"));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /client/edit/{id} - Exibe formulário de edição")
	void testEditFormAuthorizedUser() throws Exception {
		when(clientService.getClientById(1L)).thenReturn(testCreateSingleClient());

		mockMvc.perform(get("/client/edit/1")).andDo(print()).andExpect(status().isOk())
				.andExpect(view().name("client/edit")).andExpect(model().attributeExists("client"));
	}

	// =========================================================================
	// TESTES DE PROCESSAMENTO DE FORMULÁRIOS (POST)
	// =========================================================================

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /client/save - Falha na validação e retorna para o formulário")
	void testSaveClientValidationError() throws Exception {
		Client invalidClient = new Client();

		mockMvc.perform(post("/client/save").with(csrf()).flashAttr("client", invalidClient)).andExpect(status().isOk())
				.andExpect(view().name("client/create")).andExpect(model().attributeHasErrors("client"));

		verify(clientService, never()).saveClient(any(Client.class));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /client/save - Cliente válido é salvo com sucesso")
	void testSaveValidClient() throws Exception {
		Client validClient = testCreateSingleClient();

		mockMvc.perform(post("/client/save").with(csrf()).flashAttr("client", validClient)).andDo(print())
																											
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/client"));

		verify(clientService).saveClient(any(Client.class));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /client/edit/{id} - Falha na validação retorna para edição")
	void testUpdateClientValidationError() throws Exception {
		Client invalidClient = new Client();

		mockMvc.perform(post("/client/edit/1").with(csrf()).flashAttr("client", invalidClient))
				.andExpect(status().isOk()).andExpect(view().name("client/edit"))
				.andExpect(model().attributeHasErrors("client"));

		verify(clientService, never()).updateClient(eq(1L), any(Client.class));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /client/edit/{id} - Atualização salva com sucesso")
	void testUpdateValidClient() throws Exception {
		Client validClient = testCreateSingleClient();

		mockMvc.perform(post("/client/edit/1").with(csrf()).flashAttr("client", validClient)).andDo(print())
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/client"))
				.andExpect(flash().attribute("mensagemSucesso", "Cliente atualizado com sucesso!"));
																										
		verify(clientService).updateClient(eq(1L), any(Client.class));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /client/delete/{id} - Cliente deletado com sucesso")
	void testDeleteClient() throws Exception {
		mockMvc.perform(post("/client/delete/1").with(csrf())).andDo(print()).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/client"));

		verify(clientService).deleteClientById(1L);
	}
}