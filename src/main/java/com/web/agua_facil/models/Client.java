package com.web.agua_facil.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Nome é um campo obrigatório")
    @Size(min = 7, max = 50, message = "Nome deve conter entre 7 e 50 caracteres")
    @Column(name = "nome", nullable = false)
    private String nome;

    @NotBlank(message = "CPF é um campo obrigatório")
    @Size(min = 11, max = 11, message = "CPF deve conter exatamente 11 números")
    @Column(name = "cpf", nullable = false, unique = true)
    private String cpf;

    @NotBlank(message = "Rua é um campo obrigatório")
    @Column(name = "rua", nullable = false)
    private String rua;

    @NotBlank(message = "Número é um campo obrigatório")
    @Column(name = "numero", nullable = false)
    private String numero;

    @NotBlank(message = "Bairro é um campo obrigatório")
    @Column(name = "bairro", nullable = false)
    private String bairro;

    @NotBlank(message = "Telefone é um campo obrigatório")
    @Column(name = "telefone", nullable = false)
    private String telefone;

}