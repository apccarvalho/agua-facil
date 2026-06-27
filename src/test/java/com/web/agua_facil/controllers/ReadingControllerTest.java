package com.web.agua_facil.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import com.web.agua_facil.models.Property;
import com.web.agua_facil.models.Reading;
import com.web.agua_facil.models.User;
import com.web.agua_facil.services.PropertyService;
import com.web.agua_facil.services.ReadingService;
import com.web.agua_facil.services.impl.UserDetailsServiceImpl;

@WebMvcTest(ReadingController.class)
@Import(SecurityConfig.class)
public class ReadingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ReadingService readingService;

	@MockitoBean
	private PropertyService propertyService;

	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	@MockitoBean
	private CustomAuthenticationSuccessHandler successHandler;

	@AfterEach
	void resetMocks() {
		reset(readingService, propertyService);
	}

	@BeforeEach
	void setupDefaultMocks() {
		User user = new User();
		user.setNome("Proprietário Teste");

		Client client = new Client();
		client.setId(1L);
		client.setUser(user);

		Property property = new Property();
		property.setId(1L);
		property.setMatricula("MAT-12345");
		property.setLogradouro("Rua das Águas");
		property.setCliente(client);

		when(propertyService.getAllProperties()).thenReturn(List.of(property));
	}

	private Reading testCreateValidReading() {
        Reading reading = new Reading();
        reading.setId(1L);
        reading.setDataLeitura(LocalDate.now());
        reading.setValorMedido(1520L);
        
        User user = new User();
        user.setNome("Proprietário Teste");
        
        Client client = new Client();
        client.setId(1L);
        client.setUser(user);
        
        Property property = new Property();
        property.setId(1L);
        property.setMatricula("MAT-12345");
        property.setCliente(client);
        reading.setProperty(property);
        
        return reading;
    }

	// =========================================================================
	// TESTES DE SEGURANÇA E AUTORIZAÇÃO (RESTRIÇÕES DE ROTAS)
	// =========================================================================

	@Test
	@DisplayName("GET /reading - Anônimo redireciona para login")
	void testIndexNotAuthenticated() throws Exception {
		mockMvc.perform(get("/reading")).andDo(print()).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("**/login"));
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	@DisplayName("GET /reading - CLIENTE tem acesso negado (403)")
	void testIndexForbiddenForCliente() throws Exception {
		mockMvc.perform(get("/reading")).andDo(print()).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "LEITOR")
	@DisplayName("GET /reading - LEITOR tem permissão de acesso")
	void testIndexAllowedForLeitor() throws Exception {
		when(readingService.getAllReadings()).thenReturn(List.of(testCreateValidReading()));

		mockMvc.perform(get("/reading")).andDo(print()).andExpect(status().isOk())
				.andExpect(view().name("reading/index")).andExpect(model().attributeExists("readingsList"));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /reading - FUNCIONARIO tem permissão de acesso")
	void testIndexAllowedForFuncionario() throws Exception {
		mockMvc.perform(get("/reading")).andExpect(status().isOk());
	}

	// =========================================================================
	// TESTES DE EXIBIÇÃO DE FORMULÁRIOS
	// =========================================================================

	@Test
	@WithMockUser(roles = "LEITOR")
	@DisplayName("GET /reading/create - Exibe formulário com lista de imóveis")
	void testCreateForm() throws Exception {
		mockMvc.perform(get("/reading/create")).andDo(print()).andExpect(status().isOk())
				.andExpect(view().name("reading/create")).andExpect(model().attributeExists("reading"))
				.andExpect(model().attributeExists("properties"));
	}

	@Test
	@WithMockUser(roles = "LEITOR")
	@DisplayName("GET /reading/edit/{id} - Exibe formulário de edição")
	void testEditFormSuccess() throws Exception {
		when(readingService.findReadingById(1L)).thenReturn(testCreateValidReading());

		mockMvc.perform(get("/reading/edit/1")).andExpect(status().isOk()).andExpect(view().name("reading/edit"))
				.andExpect(model().attributeExists("reading")).andExpect(model().attributeExists("properties"));
	}

	@Test
	@WithMockUser(roles = "LEITOR")
	@DisplayName("GET /reading/edit/{id} - Erro ao buscar leitura redireciona")
	void testEditFormNotFound() throws Exception {
		when(readingService.findReadingById(99L)).thenThrow(new IllegalArgumentException("Leitura não encontrada"));

		mockMvc.perform(get("/reading/edit/99")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/reading")).andExpect(flash().attributeExists("erro"));
	}

	// =========================================================================
	// TESTES DE PROCESSAMENTO DE FORMULÁRIOS (POST)
	// =========================================================================

	@Test
	@WithMockUser(roles = "LEITOR")
	@DisplayName("POST /reading/save - Falha na validação do Bean retorna ao form")
	void testSaveValidationError() throws Exception {
		Reading invalidReading = new Reading();

		mockMvc.perform(post("/reading/save").with(csrf()).flashAttr("reading", invalidReading))
				.andExpect(status().isOk()).andExpect(view().name("reading/create"))
				.andExpect(model().attributeHasErrors("reading")).andExpect(model().attributeExists("properties"));

		verify(readingService, never()).registerReading(any(Reading.class));
	}

	@Test
	@WithMockUser(roles = "LEITOR")
	@DisplayName("POST /reading/save - Erro de regra de negócio (ex: medição menor que a anterior)")
	void testSaveBusinessRuleError() throws Exception {
		Reading validReading = testCreateValidReading();

		doThrow(new IllegalArgumentException("A medição atual não pode ser menor que a anterior")).when(readingService)
				.registerReading(any(Reading.class));

		mockMvc.perform(post("/reading/save").with(csrf()).flashAttr("reading", validReading))
				.andExpect(status().isOk()).andExpect(view().name("reading/create"))
				.andExpect(model().attributeExists("erroLeitura"));
	}

	@Test
	@WithMockUser(roles = "LEITOR")
	@DisplayName("POST /reading/save - Leitura salva com sucesso")
	void testSaveValidReading() throws Exception {
		Reading validReading = testCreateValidReading();

		mockMvc.perform(post("/reading/save").with(csrf()).flashAttr("reading", validReading))
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/reading"))
				.andExpect(flash().attributeExists("mensagemSucesso"));

		verify(readingService).registerReading(any(Reading.class));
	}

	@Test
	@WithMockUser(roles = "LEITOR")
	@DisplayName("POST /reading/delete/{id} - Deleção com sucesso")
	void testDeleteReadingSuccess() throws Exception {
		mockMvc.perform(post("/reading/delete/1").with(csrf())).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/reading")).andExpect(flash().attributeExists("mensagemSucesso"));

		verify(readingService).deleteReading(1L);
	}
}