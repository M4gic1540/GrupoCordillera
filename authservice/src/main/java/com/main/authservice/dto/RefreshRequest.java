package com.main.authservice.dto;

/*
 * RefreshRequest - DTO.
 * Responsibilities: Contrato de datos para capa API.
 * Patterns: DTO
 */


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RefreshRequest {

    @NotBlank
    @Size(max = 512)
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
