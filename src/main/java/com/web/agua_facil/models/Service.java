package com.web.agua_facil.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Long id;
    
    @Size(min = 3, max = 255, message = "Nome deve conter pelo mínimo 03 caracteres")
    @NotBlank(message = "O nome do serviço é obrigatório")
    @Column(name = "nome", unique = true, nullable = false)
    private String nome;
    
    @Size(min = 3, max = 255, message = "Descrição deve conter pelo mínimo 03 caracteres")
    @Column(name = "descricao")
    private String descricao;
    
    @Min(value = 0, message = "O valor não pode ser negativo")
    @NotNull(message = "O valor do serviço é obrigatório")
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @ManyToMany(mappedBy = "servicos")
    private List<Bill> faturas;
}