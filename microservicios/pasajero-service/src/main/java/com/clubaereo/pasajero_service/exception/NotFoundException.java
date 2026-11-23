package com.clubaereo.pasajero_service.exception;

/** El recurso pedido no existe. Se responde con HTTP 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
