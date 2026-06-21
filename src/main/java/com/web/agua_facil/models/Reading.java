package com.web.agua_facil.models;

import java.time.LocalDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reading {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reading_id")
    private Long id;
        
    @NotNull(message = "O imóvel é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;
    
    @NotNull(message = "Data é um campo obrigatório")
    @Column(name = "data_leitura", nullable = false)
    private LocalDate dataLeitura;
    
    @NotNull(message = "Valor medido é um campo obrigatório")
    @Column(name = "valor_medido", nullable = false)
    private Long valorMedido;
    
}