package com.web.agua_facil.services.impl;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.agua_facil.models.Bill;
import com.web.agua_facil.models.BillStatus;
import com.web.agua_facil.models.PropertyCategory;
import com.web.agua_facil.models.Reading;
import com.web.agua_facil.models.TariffTier;
import com.web.agua_facil.repositories.BillRepository;
import com.web.agua_facil.repositories.ReadingRepository;
import com.web.agua_facil.services.BillService;
import com.web.agua_facil.services.TariffTierService;

@Service
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final ReadingRepository readingRepository;
    private final TariffTierService tariffTierService;

    public BillServiceImpl(BillRepository billRepository, ReadingRepository readingRepository,TariffTierService tariffTierService) {
			this.billRepository = billRepository;
			this.readingRepository = readingRepository;
			this.tariffTierService = tariffTierService;
	}

    @Override
    @Transactional
    public Bill saveBill(Bill bill) {
        
        if (bill.getReading() != null && bill.getReading().getId() != null) {
            Optional<Bill> faturaExistente = billRepository.findByReadingId(bill.getReading().getId());
            if (faturaExistente.isPresent()) {
                throw new IllegalArgumentException("Erro: Já existe uma fatura gerada para esta leitura.");
            }
        }

        if (bill.getReading() != null && bill.getReading().getProperty() != null) {
            Long propertyId = bill.getReading().getProperty().getId();
            Optional<Bill> faturaMesmoMes = billRepository.findByPropertyIdAndMesReferencia(propertyId, bill.getMesReferencia());
            
            if (faturaMesmoMes.isPresent()) {
                throw new IllegalArgumentException("Erro: O imóvel já possui uma fatura para o mês de " + bill.getMesReferencia() + ".");
            }
        }

        return billRepository.save(bill);
    }

    @Override
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    @Override
    public Optional<Bill> findBillById(Long id) {
        return billRepository.findById(id);
    }

    @Override
    public List<Bill> findBillsByPropertyId(Long propertyId) {
        return billRepository.findBillsByPropertyId(propertyId);
    }

    @Override
    @Transactional
    public Bill updateBill(Long id, Bill billDetails) {
        
        Bill existingBill = billRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Erro: Fatura com ID " + id + " não encontrada."));

        existingBill.setMesReferencia(billDetails.getMesReferencia());
        existingBill.setDataVencimento(billDetails.getDataVencimento());
        existingBill.setDataPagamento(billDetails.getDataPagamento());
        existingBill.setStatus(billDetails.getStatus());
        
        return billRepository.save(existingBill);
    }

    @Override
    @Transactional
    public void deleteBill(Long id) {
        if (!billRepository.existsById(id)) {
            throw new IllegalArgumentException("Erro: Não é possível excluir. Fatura com ID " + id + " não encontrada.");
        }
        billRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public Bill gerarFatura(Long readingId) {
        
        Reading leituraAtual = readingRepository.findById(readingId)
            .orElseThrow(() -> new IllegalArgumentException("Erro: Leitura com ID " + readingId + " não encontrada."));

        Optional<Reading> leituraAnteriorOpt = readingRepository.findFirstByPropertyIdAndDataLeituraBeforeOrderByDataLeituraDesc(
                leituraAtual.getProperty().getId(), 
                leituraAtual.getDataLeitura()
        );

        Long valorAnterior = leituraAnteriorOpt.map(Reading::getValorMedido).orElse(0L);
        
        Long consumo = leituraAtual.getValorMedido() - valorAnterior;

        if (consumo < 0) {
            throw new IllegalArgumentException("Erro: O consumo calculado resultou em um valor negativo.");
        }
       
        PropertyCategory categoriaDoImovel = leituraAtual.getProperty().getCategoria();

        TariffTier faixaTarifa = tariffTierService.findTariffTierByConsumoAndCategoria(consumo, categoriaDoImovel)
            .orElseThrow(() -> new IllegalArgumentException(
                "Erro: Nenhuma faixa de tarifa configurada para o consumo de " + consumo + 
                " m³ na categoria " + categoriaDoImovel + ". Verifique o cadastro de tarifas."
            ));

        BigDecimal valorAgua = faixaTarifa.getValorPorM3().multiply(BigDecimal.valueOf(consumo));
        BigDecimal taxaEsgoto = valorAgua.multiply(new BigDecimal("0.50"));
        BigDecimal valorTotal = valorAgua.add(taxaEsgoto);

        Bill novaFatura = new Bill();
        novaFatura.setReading(leituraAtual);
        novaFatura.setConsumo(consumo);
        novaFatura.setValorTotal(valorTotal);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        novaFatura.setMesReferencia(leituraAtual.getDataLeitura().format(formatter));
        
        novaFatura.setDataVencimento(leituraAtual.getDataLeitura().plusDays(30));
        
        novaFatura.setStatus(BillStatus.PENDENTE);

        return this.saveBill(novaFatura);
    }
}