package com.web.agua_facil.models;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Valid
    @JoinColumn(name = "user_id", nullable = false)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private User user;

    @NotBlank(message = "CPF é um campo obrigatório")
    @Size(min = 14, max = 14, message = "CPF deve conter exatamente 11 números")
    @Column(name = "cpf", nullable = false, unique = true)
    private String cpf;
    
    @Size(min = 3, max = 255, message = "Rua deve conter pelo mínimo 03 caracteres")
    @NotBlank(message = "Rua é um campo obrigatório")
    @Column(name = "rua", nullable = false)
    private String rua;
    
    @NotBlank(message = "Número é um campo obrigatório")
    @Column(name = "numero", nullable = false)
    private String numero;
    
    @Size(min = 3, max = 255, message = "Bairro deve conter pelo mínimo 03 caracteres")
    @NotBlank(message = "Bairro é um campo obrigatório")
    @Column(name = "bairro", nullable = false)
    private String bairro;
    
    @Size(min = 8, max = 15, message = "Telefone deve conter pelo mínimo 08 caracteres")
    @NotBlank(message = "Telefone é um campo obrigatório")
    @Column(name = "telefone", nullable = false)
    private String telefone;
    
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Property> propriedades;

}