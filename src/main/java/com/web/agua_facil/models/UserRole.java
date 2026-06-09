package com.web.agua_facil.models;

public enum UserRole {
    CLIENTE("CLIENTE"),
    FUNCIONARIO("FUNCIONARIO"),
    LEITOR("LEITOR");

    private String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}