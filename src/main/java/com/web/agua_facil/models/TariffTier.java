package com.web.agua_facil.models;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "tariff_tiers")
public class TariffTier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Descrição é um campo obrigatório")
    @Column(name = "descricao", nullable = false)
    private String descricao;
    
    @NotNull(message = "Consumo mínimo é um valor obrigatório")
    @Column(name = "consumo_minimo", nullable = false)
    private Long consumoMinimo;
	
    @NotNull(message = "Consumo máximo é um valor obrigatório")
    @Column(name = "consumo_maximo", nullable = false)
    private Long consumoMaximo;
    
    @NotNull(message = "Valor é um campo obrigatório")
    @Column(name = "valor_por_m3", nullable = false, precision = 10, scale = 2) 
    private BigDecimal valorPorM3;
    
    @NotNull(message = "A categoria do imóvel é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private PropertyCategory categoria;
}