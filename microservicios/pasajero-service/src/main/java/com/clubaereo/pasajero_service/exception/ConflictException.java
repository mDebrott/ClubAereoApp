package com.clubaereo.pasajero_service.exception;

/** La petición es válida pero choca con el estado actual. Se responde con HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
