package com.clubaereo.vuelos_service.exception;

/** Otro microservicio no responde. Se traduce a HTTP 503. */
public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String mensaje) { super(mensaje); }
}
