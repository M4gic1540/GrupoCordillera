package com.main.authservice.exception;

/*
 * NotFoundException - Componente.
 * Responsibilities: Logica principal del modulo.
 * Patterns: N/A
 */


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
