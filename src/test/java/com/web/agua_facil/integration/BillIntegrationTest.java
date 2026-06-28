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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.web.agua_facil.models.Bill;
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

	@Autowired
	private BillRepository billRepository;
	@Autowired
	private ReadingRepository readingRepository;
	@Autowired
	private PropertyRepository propertyRepository;
	@Autowired
	private ClientRepository clientRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ServiceRepository serviceRepository;
	@Autowired
	private TariffTierRepository tariffTierRepository;

	private Long idLeituraSalva;
	private Long idServicoSalvo;

	@BeforeEach
	void setupCenárioBancoDeDados() { // Preparação do banco

		billRepository.deleteAll();
		readingRepository.deleteAll();
		propertyRepository.deleteAll();
		clientRepository.deleteAll();
		userRepository.deleteAll();
		serviceRepository.deleteAll();
		tariffTierRepository.deleteAll();

		User user = new User();
		user.setNome("Proprietário Teste");
		user.setEmail("prop@teste.com");
		user.setPassword("password123");
		user.setRole(UserRole.CLIENTE);
		userRepository.save(user);

		Client client = new Client();
		client.setUser(user);
		client.setCpf("000.111.222-33");
		client.setRua("Rua Principal");
		client.setNumero("100");
		client.setBairro("Centro");
		client.setTelefone("34988888888");
		clientRepository.save(client);

		Property property = new Property();
		property.setMatricula("MAT-999");
		property.setLogradouro("Rua Principal");
		property.setNumero("100");
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
		tarifa.setConsumoMaximo(1000L);
		tarifa.setValorPorM3(new BigDecimal("4.50"));
		tarifa.setCategoria(PropertyCategory.RESIDENCIAL);
		tariffTierRepository.save(tarifa);

		Reading reading = new Reading();
		reading.setProperty(property);
		reading.setDataLeitura(LocalDate.now());
		reading.setValorMedido(15L);
		Reading savedReading = readingRepository.save(reading);

		Service taxaExtra = new Service();
		taxaExtra.setNome("Manutenção de Rede");
		taxaExtra.setValor(new BigDecimal("15.00"));
		Service savedService = serviceRepository.save(taxaExtra);

		this.idLeituraSalva = savedReading.getId();
		this.idServicoSalvo = savedService.getId();
	}

	// =========================================================================
	// CENÁRIO AUTORIZADO (FUNCIONARIO)
	// =========================================================================

	@Test
	@WithMockUser(roles = "FUNCIONARIO")
	@DisplayName("Integração: FUNCIONARIO autorizado consegue gerar fatura")
	void testGenerateBillWithAuthorizedUser() throws Exception {

		mockMvc.perform(
				post("/bill/generate/" + idLeituraSalva).with(csrf()).param("servicosIds", idServicoSalvo.toString()))
				.andDo(print()).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/bill"))
				.andExpect(flash().attributeExists("mensagemSucesso"));

		// Auditoria do banco de dados pós-requisição bem-sucedida
		List<Bill> faturasNoBanco = billRepository.findAll();
		assertFalse(faturasNoBanco.isEmpty(), "A fatura deveria ter sido salva no banco!");
		assertTrue(faturasNoBanco.size() == 1, "Deveria existir exatamente 1 fatura no banco.");

		Bill faturaReal = faturasNoBanco.get(0);
		assertTrue(faturaReal.getConsumo() == 15L);
	}

	// =========================================================================
	// CENÁRIO NÃO AUTORIZADO (CLIENTE / LEITOR)
	// =========================================================================

	@Test
	@WithMockUser(roles = "CLIENTE")
	@DisplayName("Integração: CLIENTE não autorizado deve receber 403 e não salvar dados")
	void testGenerateBillWithUnauthorizedUser() throws Exception {

		mockMvc.perform(
				post("/bill/generate/" + idLeituraSalva).with(csrf()).param("servicosIds", idServicoSalvo.toString()))
				.andDo(print()).andExpect(status().isForbidden());

		List<Bill> faturasNoBanco = billRepository.findAll();

		assertTrue(faturasNoBanco.isEmpty(), "Segurança Falhou! Uma fatura foi gravada por um usuário não autorizado.");
		assertTrue(faturasNoBanco.size() == 0, "O número de faturas geradas deve ser zero.");
	}
}