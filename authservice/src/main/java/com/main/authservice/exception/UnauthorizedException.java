package com.main.authservice.exception;

/*
 * UnauthorizedException - Componente.
 * Responsibilities: Logica principal del modulo.
 * Patterns: N/A
 */


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
