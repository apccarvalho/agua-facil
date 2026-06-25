package com.web.agua_facil.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.web.agua_facil.models.Bill;
import com.web.agua_facil.models.BillStatus;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByReadingId(Long readingId);

    List<Bill> findByStatus(BillStatus status);

    @Query("SELECT b FROM Bill b WHERE b.reading.property.id = :propertyId")
    List<Bill> findBillsByPropertyId(@Param("propertyId") Long propertyId);

    //Busca uma fatura pelo mês de referência, para que não fature duas vezes
    @Query("SELECT b FROM Bill b WHERE b.reading.property.id = :propertyId AND b.mesReferencia = :mesReferencia")
    Optional<Bill> findByPropertyIdAndMesReferencia(@Param("propertyId") Long propertyId, @Param("mesReferencia") String mesReferencia);
    
    //Lista apenas as últimas 05 faturas
    List<Bill> findTop5ByReadingPropertyClienteIdOrderByDataVencimentoDesc(Long clienteId);
}