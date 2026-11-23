package com.clubaereo.pasajero_service.exception;

/** La petición es inválida (por ejemplo, un RUT mal formado). Se responde con HTTP 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
