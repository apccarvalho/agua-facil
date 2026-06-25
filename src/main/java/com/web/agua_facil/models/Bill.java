package com.web.agua_facil.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bills")
public class Bill {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "A leitura é obrigatória")
    @OneToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "reading_id", nullable = false, unique = true)
    private Reading reading;
    
    @NotBlank(message = "O mês de referência é obrigatório")
    @Column(name = "mes_referencia", nullable = false)
    private String mesReferencia;
    
    @NotNull(message = "Consumo é um campo obrigatório")
    @Column(name = "consumo", nullable = false)
    private Long consumo;
    
    @NotNull(message = "Valor total é um campo obrigatório")
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2) 
    private BigDecimal valorTotal;
    
    @NotNull(message = "Data de vencimento é um campo obrigatório")
    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;
    
    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;
    
    @NotNull(message = "O status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BillStatus status;
    
    @ManyToMany
    @JoinTable(
        name = "bill_services",
        joinColumns = @JoinColumn(name = "bill_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<com.web.agua_facil.models.Service> servicos;
}