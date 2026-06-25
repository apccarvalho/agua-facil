package com.web.agua_facil.services;

import java.util.List;
import java.util.Optional;
import com.web.agua_facil.models.Bill;

public interface BillService {
    Bill saveBill(Bill bill);    
    List<Bill> getAllBills();   
    Optional<Bill> findBillById(Long id);    
    List<Bill> findBillsByPropertyId(Long propertyId);
    Bill updateBill(Long id, Bill billDetails);
    void deleteBill(Long id);
    Bill gerarFatura(Long readingId, List<Long> servicosIds);
    List<Bill> getTop5FaturasRecentesDoCliente(Long clienteId);
}