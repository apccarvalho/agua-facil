package com.web.agua_facil.models;

public enum PropertyCategory {
    RESIDENCIAL("RESIDENCIAL"),
    COMERCIAL("COMERCIAL"),
    INDUSTRIAL("INDUSTRIAL"),
    PUBLICO("PÚBLICO");

    private final String descricao;

    PropertyCategory(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}