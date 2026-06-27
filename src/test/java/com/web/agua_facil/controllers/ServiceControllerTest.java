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

import java.math.BigDecimal;
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

import com.web.agua_facil.config.SecurityConfig;
import com.web.agua_facil.config.CustomAuthenticationSuccessHandler;
import com.web.agua_facil.models.Service;
import com.web.agua_facil.services.ServiceService;
import com.web.agua_facil.services.impl.UserDetailsServiceImpl;

@WebMvcTest(ServiceController.class)
@Import(SecurityConfig.class)
public class ServiceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ServiceService serviceService;

	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;
	@MockitoBean
	private CustomAuthenticationSuccessHandler successHandler;

	@AfterEach
	void resetMocks() {
		reset(serviceService);
	}

	private Service createValidService() {
		return new Service(1L, "Taxa de Religação", "Serviço padrão de religação", new BigDecimal("50.00"), null);
	}

	// =========================================================================
	// TESTES DE AUTORIZAÇÃO E AUTENTICAÇÃO
	// =========================================================================

	@Test
	@DisplayName("GET /service - Sem usuário logado deve redirecionar para o login")
	void testIndexNotAuthenticatedUser() throws Exception {
		mockMvc.perform(get("/service")).andDo(print()).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("**/login"));
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	@DisplayName("GET /service - Usuário com papel CLIENTE deve ter acesso negado (403)")
	void testIndexForbiddenForCliente() throws Exception {
		mockMvc.perform(get("/service")).andDo(print()).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /service - Listar serviços na tela index com FUNCIONARIO logado")
	void testIndexAuthenticatedFuncionario() throws Exception {
		when(serviceService.getAllServices()).thenReturn(List.of(createValidService()));

		mockMvc.perform(get("/service")).andDo(print()).andExpect(status().isOk())
				.andExpect(view().name("service/index")).andExpect(model().attributeExists("servicesList"));
	}

	// =========================================================================
	// TESTES DE LISTAGEM E CADASTRO
	// =========================================================================

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /service - Listar serviços com sucesso")
	void testListServices() throws Exception {
		when(serviceService.getAllServices()).thenReturn(List.of(createValidService()));

		mockMvc.perform(get("/service")).andDo(print()).andExpect(status().isOk())
				.andExpect(view().name("service/index")).andExpect(model().attributeExists("servicesList"));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /service/create - Cadastro válido com sucesso")
	void testSaveValidService() throws Exception {
		Service service = createValidService();

		mockMvc.perform(post("/service/create").with(csrf()).flashAttr("service", service)).andDo(print())
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/service"))
				.andExpect(flash().attribute("mensagemSucesso", "Serviço cadastrado com sucesso!"));

		verify(serviceService).saveService(any(Service.class));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /service/create - Falha na validação (nome vazio)")
	void testSaveInvalidService() throws Exception {
		Service invalidService = new Service(null, "", "", new BigDecimal("-1.0"), null);

		mockMvc.perform(post("/service/create").with(csrf()).flashAttr("service", invalidService)).andDo(print())
				.andExpect(status().isOk()).andExpect(view().name("service/create"))
				.andExpect(model().attributeHasErrors("service"));

		verify(serviceService, never()).saveService(any(Service.class));
	}

	// =========================================================================
	// TESTES DE EDIÇÃO E DELEÇÃO
	// =========================================================================

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /service/edit/{id} - Edição encontrada")
	void testEditServiceFound() throws Exception {
		when(serviceService.findServiceById(1L)).thenReturn(Optional.of(createValidService()));

		mockMvc.perform(get("/service/edit/1")).andDo(print()).andExpect(status().isOk())
				.andExpect(view().name("service/edit"));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /service/edit/{id} - Atualização com sucesso")
	void testUpdateValidService() throws Exception {
		Service service = createValidService();

		mockMvc.perform(post("/service/edit/1").with(csrf()).flashAttr("service", service)).andDo(print())
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/service"));

		verify(serviceService).updateService(eq(1L), any(Service.class));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /service/delete/{id} - Exclusão com sucesso")
	void testDeleteService() throws Exception {
		mockMvc.perform(post("/service/delete/1").with(csrf())).andExpect(status().is3xxRedirection()).andDo(print())
				.andExpect(view().name("redirect:/service"))
				.andExpect(flash().attribute("mensagemSucesso", "Serviço excluído com sucesso!"));

		verify(serviceService).deleteService(1L);
	}
}