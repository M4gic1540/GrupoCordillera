package com.main.bffservice.bff.dto;

/*
 * LoginRequest - DTO.
 * Responsibilities: Contrato de datos para capa API.
 * Patterns: DTO, BFF
 */


public class LoginRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
