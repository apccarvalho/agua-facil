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
import com.web.agua_facil.models.PropertyCategory;
import com.web.agua_facil.models.TariffTier;
import com.web.agua_facil.services.TariffTierService;
import com.web.agua_facil.services.impl.UserDetailsServiceImpl;

@WebMvcTest(TariffTierController.class)
@Import(SecurityConfig.class)
public class TariffTierControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TariffTierService tariffTierService;

	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;
	@MockitoBean
	private CustomAuthenticationSuccessHandler successHandler;

	@AfterEach
	void resetMocks() {
		reset(tariffTierService);
	}

	private TariffTier createValidTariffTier() {
		return new TariffTier(1L, "Residencial - Faixa 1", 0L, 10L, new BigDecimal("3.50"),
				PropertyCategory.RESIDENCIAL);
	}

	// =========================================================================
	// TESTES DE AUTORIZAÇÃO E AUTENTICAÇÃO
	// =========================================================================

	@Test
	@DisplayName("GET /tariff - Sem usuário logado deve redirecionar para o login")
	void testIndexNotAuthenticatedUser() throws Exception {
		mockMvc.perform(get("/tariff")).andDo(print()).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("**/login"));
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	@DisplayName("GET /tariff - Usuário com papel CLIENTE deve ter acesso negado (403)")
	void testIndexForbiddenForCliente() throws Exception {
		mockMvc.perform(get("/tariff")).andDo(print()).andExpect(status().isForbidden());
	}

	// =========================================================================
	// TESTES DE FLUXO PRINCIPAL
	// =========================================================================

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /tariff - Listar tarifas com FUNCIONARIO logado")
	void testIndexAuthenticatedFuncionario() throws Exception {
		when(tariffTierService.getAllTariffTiers()).thenReturn(List.of(createValidTariffTier()));

		mockMvc.perform(get("/tariff")).andDo(print()).andExpect(status().isOk()).andExpect(view().name("tariff/index"))
				.andExpect(model().attributeExists("tariffsList"));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /tariff/create - Exibir form de criação")
	void testCreateForm() throws Exception {
		mockMvc.perform(get("/tariff/create")).andDo(print()).andExpect(status().isOk())
				.andExpect(view().name("tariff/create")).andExpect(model().attributeExists("categorias"));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /tariff/save - Salvar com sucesso")
	void testSaveSuccess() throws Exception {
		TariffTier tariff = createValidTariffTier();

		mockMvc.perform(post("/tariff/save").with(csrf()).flashAttr("tariffTier", tariff)).andDo(print())
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/tariff"))
				.andExpect(flash().attributeExists("mensagemSucesso"));

		verify(tariffTierService).saveTariffTier(any(TariffTier.class));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /tariff/save - Falha na validação (consumo negativo)")
	void testSaveValidationError() throws Exception {
		TariffTier invalidTariff = new TariffTier(null, "Errado", -1L, 10L, new BigDecimal("1.0"),
				PropertyCategory.RESIDENCIAL);

		mockMvc.perform(post("/tariff/save").with(csrf()).flashAttr("tariffTier", invalidTariff)).andDo(print())
				.andExpect(status().isOk()).andExpect(view().name("tariff/create"))
				.andExpect(model().attributeHasErrors("tariffTier"));

		verify(tariffTierService, never()).saveTariffTier(any(TariffTier.class));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /tariff/edit/{id} - Edição encontrada")
	void testEditFormFound() throws Exception {
		when(tariffTierService.findTariffById(1L)).thenReturn(Optional.of(createValidTariffTier()));

		mockMvc.perform(get("/tariff/edit/1")).andDo(print()).andExpect(status().isOk())
				.andExpect(view().name("tariff/edit")).andExpect(model().attributeExists("tariffTier"));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /tariff/edit/{id} - Atualização com sucesso")
	void testUpdateSuccess() throws Exception {
		TariffTier tariff = createValidTariffTier();

		mockMvc.perform(post("/tariff/edit/1").with(csrf()).flashAttr("tariffTier", tariff)).andDo(print())
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/tariff"))
				.andExpect(flash().attribute("mensagemSucesso", "Faixa de tarifa atualizada com sucesso!"));

		verify(tariffTierService).updateTariffTier(eq(1L), any(TariffTier.class));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /tariff/delete/{id} - Exclusão com sucesso")
	void testDeleteSuccess() throws Exception {
		mockMvc.perform(post("/tariff/delete/1").with(csrf())).andDo(print()).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/tariff"));

		verify(tariffTierService).deleteTariffTier(1L);
	}
}