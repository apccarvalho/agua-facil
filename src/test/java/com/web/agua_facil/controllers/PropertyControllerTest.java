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

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.web.agua_facil.models.PropertyCategory;
import com.web.agua_facil.models.User;
import com.web.agua_facil.services.ClientService;
import com.web.agua_facil.services.PropertyService;
import com.web.agua_facil.services.impl.UserDetailsServiceImpl;

@WebMvcTest(PropertyController.class)
@Import(SecurityConfig.class)
public class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PropertyService propertyService;
    
    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    
    @MockitoBean
    private CustomAuthenticationSuccessHandler successHandler;

    @AfterEach
    void resetMocks() {
        reset(propertyService, clientService);
    }

    @BeforeEach
    void setupDefaultMocks() {
    	User user = new User();
        user.setNome("Cliente da Lista");
        
        Client clienteSelect = new Client();
        clienteSelect.setId(100L);
        clienteSelect.setUser(user);

        when(clientService.getAllClients()).thenReturn(List.of(clienteSelect));
    }

    private Property testCreateValidProperty() {
        Property property = new Property();
        property.setId(1L);
        property.setMatricula("MAT-102030");
        property.setLogradouro("Avenida Faria Pereira");
        property.setNumero("1000");
        property.setBairro("Centro");
        property.setCidade("Patrocínio");
        property.setUf("MG");
        property.setCep("38740-000");
        
        property.setCategoria(PropertyCategory.values()[0]); 
        
        User user = new User();
        user.setNome("Cliente Teste");
        
        Client cliente = new Client();
        cliente.setId(1L);
        cliente.setUser(user);
        cliente.setPropriedades(List.of(property));
        
        property.setCliente(cliente);
        
        return property;
    }

    // =========================================================================
    // TESTES DE SEGURANÇA E AUTORIZAÇÃO (RESTRIÇÕES DE ROTAS)
    // =========================================================================

    @Test
    @DisplayName("GET /property - Anônimo redireciona para login")
    void testIndexNotAuthenticated() throws Exception {
        mockMvc.perform(get("/property"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GET /property - CLIENTE tem acesso negado ao index (403)")
    void testIndexForbiddenForCliente() throws Exception {
        mockMvc.perform(get("/property"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GET /property/{id}/history - CLIENTE TEM ACESSO ao histórico")
    void testHistoryAllowedForCliente() throws Exception {
        Property validProperty = testCreateValidProperty();

        when(propertyService.getPropertyHistoryData(1L)).thenReturn(Map.of(
                "historico", "dados_falsos",
                "property", validProperty,
                "billsList", List.of(),    
                "listaPropriedadesDoCliente", List.of(validProperty)
            ));

        mockMvc.perform(get("/property/1/history"))
               .andExpect(status().isOk())
               .andExpect(view().name("property/history"))
               .andExpect(model().attributeExists("property"))
               .andExpect(model().attributeExists("historico"));
    }

    // =========================================================================
    // TESTES DE EXIBIÇÃO DE FORMULÁRIOS
    // =========================================================================

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    @DisplayName("GET /property/create - Exibe formulário com as listas carregadas")
    void testCreateForm() throws Exception {
        mockMvc.perform(get("/property/create"))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(view().name("property/create"))
               .andExpect(model().attributeExists("property"))
               .andExpect(model().attributeExists("clientes"))
               .andExpect(model().attributeExists("categorias"));
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    @DisplayName("GET /property/edit/{id} - Exibe formulário de edição")
    void testEditForm() throws Exception {
        when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(testCreateValidProperty()));

        mockMvc.perform(get("/property/edit/1"))
               .andExpect(status().isOk())
               .andExpect(view().name("property/edit"))
               .andExpect(model().attributeExists("property"));
    }

    // =========================================================================
    // TESTES DE PROCESSAMENTO DE FORMULÁRIOS (POST)
    // =========================================================================

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    @DisplayName("POST /property/save - Falha na validação do Bean retorna ao form")
    void testSaveValidationError() throws Exception {
        Property invalidProperty = new Property();

        mockMvc.perform(post("/property/save")
                        .with(csrf())
                        .flashAttr("property", invalidProperty))
        .andDo(print())
               .andExpect(status().isOk())
               .andExpect(view().name("property/create"))
               .andExpect(model().attributeHasErrors("property"))
               .andExpect(model().attributeExists("clientes")); 

        verify(propertyService, never()).saveProperty(any(Property.class));
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    @DisplayName("POST /property/save - Matrícula duplicada é tratada e retorna erro no model")
    void testSaveDuplicateMatricula() throws Exception {
        Property validProperty = testCreateValidProperty();
        
        doThrow(new IllegalArgumentException("Matrícula já cadastrada"))
            .when(propertyService).saveProperty(any(Property.class));

        mockMvc.perform(post("/property/save")
                        .with(csrf())
                        .flashAttr("property", validProperty))
               .andExpect(status().isOk())
               .andExpect(view().name("property/create"))
               .andExpect(model().attributeExists("erroMatricula"));
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    @DisplayName("POST /property/save - Imóvel válido salvo com sucesso")
    void testSaveValidProperty() throws Exception {
        Property validProperty = testCreateValidProperty();

        mockMvc.perform(post("/property/save")
                        .with(csrf())
                        .flashAttr("property", validProperty))
               .andDo(print())
               .andExpect(status().is3xxRedirection())
               .andExpect(view().name("redirect:/property"))
               .andExpect(flash().attributeExists("mensagemSucesso"));

        verify(propertyService).saveProperty(any(Property.class));
    }

    // =========================================================================
    // TESTES DE HISTÓRICO
    // =========================================================================

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    @DisplayName("GET /property/{id}/history - Erro ao buscar histórico redireciona com flash attribute")
    void testHistoryIllegalArgumentException() throws Exception {
        when(propertyService.getPropertyHistoryData(99L))
            .thenThrow(new IllegalArgumentException("Imóvel não encontrado"));

        mockMvc.perform(get("/property/99/history"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/property"))
               .andExpect(flash().attributeExists("erro"));
    }
}