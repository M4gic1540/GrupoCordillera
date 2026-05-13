package com.main.authservice.dto;

/*
 * UpdateUserRoleRequest - DTO.
 * Responsibilities: Contrato de datos para capa API.
 * Patterns: DTO
 */


import com.main.authservice.model.Role;
import jakarta.validation.constraints.NotNull;

public class UpdateUserRoleRequest {

    @NotNull
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
