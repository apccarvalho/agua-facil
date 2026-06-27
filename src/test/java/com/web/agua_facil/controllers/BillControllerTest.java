package com.web.agua_facil.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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
import com.web.agua_facil.models.Bill;
import com.web.agua_facil.models.BillStatus;
import com.web.agua_facil.models.Client;
import com.web.agua_facil.models.Property;
import com.web.agua_facil.models.Reading;
import com.web.agua_facil.models.Service;
import com.web.agua_facil.models.User;
import com.web.agua_facil.services.BillService;
import com.web.agua_facil.services.ServiceService;
import com.web.agua_facil.services.impl.UserDetailsServiceImpl;

@WebMvcTest(BillController.class)
@Import(SecurityConfig.class)
public class BillControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BillService billService;

	@MockitoBean
	private ServiceService serviceService;

	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	@MockitoBean
	private CustomAuthenticationSuccessHandler successHandler;

	@AfterEach
	void resetMocks() {
		reset(billService, serviceService);
	}

	@BeforeEach
	void setupDefaultMocks() {
		when(serviceService.getAllServices()).thenReturn(
				List.of(new Service(1L, "Taxa de Manutenção", "Taxa mensal", new BigDecimal("10.00"), null)));
	}

	private Bill createValidBill() {
		User user = new User();
		user.setNome("Proprietário Teste");

		Client client = new Client();
		client.setId(1L);
		client.setUser(user);

		Property property = new Property();
		property.setId(1L);
		property.setMatricula("MAT-999");
		property.setCliente(client);

		Reading reading = new Reading();
		reading.setId(1L);
		reading.setProperty(property);

		Bill bill = new Bill();
		bill.setId(1L);
		bill.setReading(reading);
		bill.setMesReferencia("06/2026");
		bill.setConsumo(100L);
		bill.setValorTotal(new BigDecimal("150.00"));
		bill.setDataVencimento(LocalDate.now().plusDays(10));
		bill.setStatus(BillStatus.PENDENTE);

		return bill;
	}

	// =========================================================================
	// TESTES DE AUTORIZAÇÃO E AUTENTICAÇÃO
	// =========================================================================

	@Test
	@DisplayName("GET /bill - Sem usuário logado deve redirecionar para o login")
	void testIndexNotAuthenticatedUser() throws Exception {
		mockMvc.perform(get("/bill")).andDo(print()).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("**/login"));
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	@DisplayName("GET /bill - Usuário com papel CLIENTE deve ter acesso negado (403)")
	void testIndexForbiddenForCliente() throws Exception {
		mockMvc.perform(get("/bill")).andDo(print()).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "LEITOR")
	@DisplayName("GET /bill - Usuário com papel LEITOR deve ter acesso negado (403)")
	void testIndexForbiddenForLeitor() throws Exception {
		mockMvc.perform(get("/bill")).andDo(print()).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /bill - Listar faturas na tela index com FUNCIONARIO logado")
	void testIndexAuthenticatedFuncionario() throws Exception {
		when(billService.getAllBills()).thenReturn(List.of(createValidBill()));

		mockMvc.perform(get("/bill")).andDo(print()).andExpect(status().isOk()).andExpect(view().name("bill/index"))
				.andExpect(model().attributeExists("billsList"));
	}

	// =========================================================================
	// TESTES DE EXIBIÇÃO DE FORMULÁRIOS
	// =========================================================================

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /bill - Listar faturas com sucesso")
	void testListBills() throws Exception {
		when(billService.getAllBills()).thenReturn(List.of(createValidBill()));

		mockMvc.perform(get("/bill")).andDo(print()).andExpect(status().isOk()).andExpect(view().name("bill/index"))
				.andExpect(model().attributeExists("billsList"));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /bill/generate/{id} - Exibe form de pré-geração")
	void testShowPreGenerateForm() throws Exception {
		mockMvc.perform(get("/bill/generate/1")).andDo(print()).andExpect(status().isOk())
				.andExpect(view().name("bill/pre-generate")).andExpect(model().attributeExists("readingId"))
				.andExpect(model().attributeExists("servicesList"));
	}

	// =========================================================================
	// TESTES DE PROCESSAMENTO DE FORMULÁRIOS (POST)
	// =========================================================================

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /bill/generate/{id} - Geração de fatura com sucesso")
	void testGenerateBillSuccess() throws Exception {
		mockMvc.perform(post("/bill/generate/1").with(csrf()).param("servicosIds", "1")).andDo(print())
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/bill"))
				.andExpect(flash().attributeExists("mensagemSucesso"));

		verify(billService).gerarFatura(eq(1L), anyList());
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /bill/edit/{id} - Edição salva com sucesso")
	void testUpdateBillSuccess() throws Exception {
		Bill bill = createValidBill();

		mockMvc.perform(post("/bill/edit/1").with(csrf()).flashAttr("bill", bill)).andDo(print())
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/bill"))
				.andExpect(flash().attribute("mensagemSucesso", "Fatura atualizada com sucesso!"));

		verify(billService).updateBill(eq(1L), any(Bill.class));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /bill/delete/{id} - Deleção com sucesso")
	void testDeleteBillSuccess() throws Exception {
		mockMvc.perform(post("/bill/delete/1").with(csrf())).andDo(print()).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/bill"))
				.andExpect(flash().attribute("mensagemSucesso", "Fatura excluída com sucesso!"));

		verify(billService).deleteBill(1L);
	}
}