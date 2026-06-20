package com.web.agua_facil.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "property_id")
    private Long id;

    @NotBlank(message = "A matrícula é obrigatória")
    @Column(name = "matricula", unique = true, nullable = false)
    private String matricula;

    @NotBlank(message = "O logradouro é obrigatório")
    @Column(name = "logradouro", nullable = false)
    private String logradouro;

    @NotBlank(message = "O número é obrigatório")
    @Column(name = "numero", nullable = false)
    private String numero;

    @NotBlank(message = "O bairro é obrigatório")
    @Column(name = "bairro", nullable = false)
    private String bairro;

    @NotBlank(message = "A cidade é obrigatória")
    @Column(name = "cidade", nullable = false)
    private String cidade;

    @NotBlank(message = "A UF é obrigatória")
    @Size(min = 2, max = 2, message = "A UF deve ter exatamente 2 caracteres")
    @Column(name = "uf", length = 2, nullable = false)
    private String uf;

    @NotBlank(message = "O CEP é obrigatório")
    @Column(name = "cep", nullable = false)
    private String cep;

    @NotNull(message = "A categoria do imóvel é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private PropertyCategory categoria;

    @NotNull(message = "O cliente proprietário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Client cliente;
}