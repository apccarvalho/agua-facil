package com.web.agua_facil.controllers;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.web.agua_facil.config.CustomAuthenticationSuccessHandler;
import com.web.agua_facil.config.SecurityConfig;
import com.web.agua_facil.models.Client;
import com.web.agua_facil.models.Property;
import com.web.agua_facil.models.PropertyCategory;
import com.web.agua_facil.models.User;
import com.web.agua_facil.services.BillService;
import com.web.agua_facil.services.ClientService;
import com.web.agua_facil.services.impl.UserDetailsServiceImpl;

@WebMvcTest(PortalController.class)
@Import(SecurityConfig.class)
public class PortalControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ClientService clientService;

	@MockitoBean
	private BillService billService;

	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	@MockitoBean
	private CustomAuthenticationSuccessHandler successHandler;

	@AfterEach
	void resetMocks() {
		reset(clientService, billService);
	}

	private Client createCompleteClient() {
		User user = new User();
		user.setNome("João Cliente");
		user.setEmail("cliente@test.com");

		Property property = new Property();
		property.setId(1L);
		property.setLogradouro("Rua Falsa");
		property.setNumero("123");
		property.setBairro("Centro");
		property.setCategoria(PropertyCategory.RESIDENCIAL);

		Client client = new Client();
		client.setId(1L);
		client.setUser(user);
		client.setCpf("123.456.789-00");
		client.setTelefone("34999999999");
		client.setRua("Rua Falsa");
		client.setNumero("123");
		client.setBairro("Centro");
		client.setPropriedades(List.of(property));

		return client;
	}

	// =========================================================================
	// TESTES DE SEGURANÇA
	// =========================================================================

	@Test
	@DisplayName("GET /portal/minha-conta - Sem usuário logado redireciona para login")
	void testMinhaContaNotAuthenticated() throws Exception {
		mockMvc.perform(get("/portal/minha-conta")).andDo(print()).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("**/login"));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /portal/minha-conta - FUNCIONARIO tem acesso negado (403)")
	void testMinhaContaForbiddenForFuncionario() throws Exception {
		mockMvc.perform(get("/portal/minha-conta")).andDo(print()).andExpect(status().isForbidden());
	}

	// =========================================================================
	// TESTE DE FLUXO (CLIENTE LOGADO)
	// =========================================================================

	@Test
	@WithMockUser(username = "cliente@test.com", roles = "CLIENTE")
	@DisplayName("GET /portal/minha-conta - CLIENTE acessa perfil com sucesso")
	void testMinhaContaSuccess() throws Exception {
		Client client = createCompleteClient();

		when(clientService.findByUserEmail("cliente@test.com")).thenReturn(Optional.of(client));
		when(billService.getTop5FaturasRecentesDoCliente(1L)).thenReturn(List.of());

		mockMvc.perform(get("/portal/minha-conta")).andDo(print()).andExpect(status().isOk())
				.andExpect(view().name("client/perfil")).andExpect(model().attributeExists("client"))
				.andExpect(model().attributeExists("ultimasFaturas"));
	}
}