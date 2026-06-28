package com.web.agua_facil.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.web.agua_facil.models.Client;
import com.web.agua_facil.models.Property;
import com.web.agua_facil.models.PropertyCategory;
import com.web.agua_facil.models.Reading;
import com.web.agua_facil.models.Service;
import com.web.agua_facil.models.TariffTier;
import com.web.agua_facil.models.User;
import com.web.agua_facil.models.UserRole;

import com.web.agua_facil.repositories.BillRepository;
import com.web.agua_facil.repositories.ClientRepository;
import com.web.agua_facil.repositories.PropertyRepository;
import com.web.agua_facil.repositories.ReadingRepository;
import com.web.agua_facil.repositories.ServiceRepository;
import com.web.agua_facil.repositories.TariffTierRepository;
import com.web.agua_facil.repositories.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional 
public class BillIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private BillRepository billRepository;
    @Autowired private ReadingRepository readingRepository;
    @Autowired private PropertyRepository propertyRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private TariffTierRepository tariffTierRepository;

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    @DisplayName("Integração: Gerar Fatura a partir de uma Leitura e Serviços Extras")
    void testGenerateBillIntegration() throws Exception {
                
        User user = new User();
        user.setNome("Proprietário Integração");
        user.setEmail("prop@integracao.com");
        user.setPassword("123456");
        user.setRole(UserRole.CLIENTE);
        userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setCpf("111.222.333-44");
        client.setRua("Rua Integração");
        client.setNumero("10");
        client.setBairro("Centro");
        client.setTelefone("34999999999");
        clientRepository.save(client);

        Property property = new Property();
        property.setMatricula("MAT-INT-001");
        property.setLogradouro("Rua Integração");
        property.setNumero("10");
        property.setBairro("Centro");
        property.setCidade("Patrocínio");
        property.setUf("MG");
        property.setCep("38740-000");
        property.setCategoria(PropertyCategory.RESIDENCIAL);
        property.setCliente(client);
        propertyRepository.save(property);
        
        TariffTier tarifa = new TariffTier();
        tarifa.setDescricao("Residencial Padrão");
        tarifa.setConsumoMinimo(0L);
        tarifa.setConsumoMaximo(5000L);
        tarifa.setValorPorM3(new BigDecimal("3.50"));
        tarifa.setCategoria(PropertyCategory.RESIDENCIAL);
        tariffTierRepository.save(tarifa);
        
        Reading reading = new Reading();
        reading.setProperty(property);
        reading.setDataLeitura(LocalDate.now());
        reading.setValorMedido(1200L);
        Reading savedReading = readingRepository.save(reading);

        Service taxaEsgoto = new Service();
        taxaEsgoto.setNome("Taxa de Esgoto");
        taxaEsgoto.setValor(new BigDecimal("25.00"));
        Service savedService = serviceRepository.save(taxaEsgoto);

        assertTrue(billRepository.findAll().isEmpty(), "O banco de faturas deveria estar vazio antes do teste");
        
        mockMvc.perform(post("/bill/generate/" + savedReading.getId())
                        .with(csrf())
                        .param("servicosIds", savedService.getId().toString()))
        .andDo(print()).andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/bill"))
               .andExpect(flash().attributeExists("mensagemSucesso"));
        
        // Busca todas as faturas geradas no banco H2
        List<com.web.agua_facil.models.Bill> faturasGeradas = billRepository.findAll();
        
        // Verifica se exatamente 1 fatura foi salva no banco
        assertFalse(faturasGeradas.isEmpty(), "A fatura não foi salva no banco de dados!");
        assertTrue(faturasGeradas.size() == 1, "Deveria haver exatamente 1 fatura gerada");

        com.web.agua_facil.models.Bill faturaSalva = faturasGeradas.get(0);
        
        // Verificações profundas:
        assertTrue(faturaSalva.getReading().getId().equals(savedReading.getId()), "A fatura foi vinculada à leitura errada");
        assertTrue(faturaSalva.getServicos().stream().anyMatch(s -> s.getId().equals(savedService.getId())), "O serviço extra não foi vinculado à fatura");
        
    }
}