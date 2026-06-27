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
import com.web.agua_facil.models.User;
import com.web.agua_facil.models.UserRole;
import com.web.agua_facil.services.UserService;
import com.web.agua_facil.services.impl.UserDetailsServiceImpl;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;
	@MockitoBean
	private CustomAuthenticationSuccessHandler successHandler;

	@AfterEach
	void resetMocks() {
		reset(userService);
	}

	private List<User> testCreateUserList() {
		User user = new User();
		user.setId(1L);
		user.setNome("João Funcionário");
		user.setEmail("joao@aguafacil.com");
		user.setRole(UserRole.FUNCIONARIO);
		return List.of(user);
	}

	private User testCreateSingleUser() {
		User user = new User();
		user.setId(1L);
		user.setNome("João Funcionário");
		user.setEmail("joao@aguafacil.com");
		user.setRole(UserRole.FUNCIONARIO);
		return user;
	}

	// =========================================================================
	// TESTES DE AUTORIZAÇÃO E AUTENTICAÇÃO (REGRA: /user/** -> FUNCIONARIO)
	// =========================================================================

	@Test
	@DisplayName("GET /user - Sem usuário logado deve redirecionar para o login")
	void testIndexNotAuthenticatedUser() throws Exception {
		mockMvc.perform(get("/user")).andDo(print()).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("**/login"));
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	@DisplayName("GET /user - Usuário com papel CLIENTE deve ter acesso negado (403)")
	void testIndexForbiddenForCliente() throws Exception {
		mockMvc.perform(get("/user")).andDo(print()).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /user - Listar usuários na tela index com FUNCIONARIO logado")
	void testIndexAuthenticatedFuncionario() throws Exception {
		when(userService.getAllUsers()).thenReturn(testCreateUserList());

		mockMvc.perform(get("/user")).andDo(print()).andExpect(status().isOk()).andExpect(view().name("user/index"))
				.andExpect(model().attributeExists("usersList"));
	}

	// =========================================================================
	// TESTES DE EXIBIÇÃO DE FORMULÁRIOS
	// =========================================================================

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /user/novo - Exibe formulário de criação")
	void testCreateFormAuthorizedUser() throws Exception {
		mockMvc.perform(get("/user/novo")).andDo(print()).andExpect(status().isOk())
				.andExpect(view().name("user/create")).andExpect(model().attributeExists("user"))
				.andExpect(model().attributeExists("roles"));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("GET /user/edit/{id} - Exibe formulário de edição")
	void testEditFormAuthorizedUser() throws Exception {
		when(userService.getUserById(1L)).thenReturn(testCreateSingleUser());

		mockMvc.perform(get("/user/edit/1")).andDo(print()).andExpect(status().isOk())
				.andExpect(view().name("user/edit")).andExpect(model().attributeExists("user"))
				.andExpect(model().attributeExists("roles"));
	}

	// =========================================================================
	// TESTES DE PROCESSAMENTO DE FORMULÁRIOS (POST)
	// =========================================================================

	// Tentar salvar com dados inválidos, que não passam no Bean Validation
	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /user/save - Falha na validação e retorna para o formulário")
	void testSaveUserValidationError() throws Exception {
		User invalidUser = new User();

		mockMvc.perform(post("/user/save").with(csrf()).flashAttr("user", invalidUser)).andDo(print())
				.andExpect(status().isOk()).andExpect(view().name("user/create"))
				.andExpect(model().attributeHasErrors("user")).andExpect(model().attributeExists("roles"));

		verify(userService, never()).saveUser(any(User.class), any());
	}

	// Tentar salvar um usuário válido
	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /user/save - Usuário válido é salvo com sucesso")
	void testSaveValidUser() throws Exception {
		User validUser = testCreateSingleUser();

		mockMvc.perform(post("/user/save").with(csrf()).flashAttr("user", validUser).param("cpf", "12345678900"))
				.andDo(print()).andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/user"));

		verify(userService).saveUser(any(User.class), eq("12345678900"));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /user/edit/{id} - Atualização salva com sucesso")
	void testUpdateValidUser() throws Exception {
		User validUser = testCreateSingleUser();

		mockMvc.perform(post("/user/edit/1").with(csrf()).flashAttr("user", validUser)).andDo(print())
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/user"));

		verify(userService).updateUser(eq(1L), any(User.class));
	}

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("POST /user/delete/{id} - Usuário deletado com sucesso")
	void testDeleteUser() throws Exception {
		mockMvc.perform(post("/user/delete/1").with(csrf())).andDo(print()).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/user"));

		verify(userService).deleteUserById(1L);
	}
}