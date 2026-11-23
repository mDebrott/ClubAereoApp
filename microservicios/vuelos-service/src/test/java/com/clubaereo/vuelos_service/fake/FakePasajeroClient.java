package com.clubaereo.vuelos_service.fake;

import java.util.HashMap;
import java.util.Map;

import com.clubaereo.vuelos_service.client.PasajeroClient;
import com.clubaereo.vuelos_service.client.PasajeroExterno;
import com.clubaereo.vuelos_service.exception.RecursoNoEncontradoException;
import com.clubaereo.vuelos_service.exception.ServicioNoDisponibleException;

/**
 * Cliente falso de pasajero-service. {@link PasajeroClient} es una clase concreta, pero no es final
 * y sus métodos son públicos, así que se puede sustituir por herencia (sin Mockito y sin tocar
 * producción). El constructor padre solo construye un RestClient: no abre ninguna conexión.
 */
public class FakePasajeroClient extends PasajeroClient {

    private final Map<Long, PasajeroExterno> pasajeros = new HashMap<>();
    private boolean caido = false;

    public FakePasajeroClient() {
        super("http://pasajero-service.fake");
    }

    /** Agrega o reemplaza un pasajero (por su id). */
    public void registrar(PasajeroExterno pasajero) {
        pasajeros.put(pasajero.id(), pasajero);
    }

    /** Simula que pasajero-service no responde. */
    public void caer() {
        caido = true;
    }

    @Override
    public PasajeroExterno obtener(Long id) {
        if (caido) {
            throw new ServicioNoDisponibleException("pasajero-service no responde: simulado");
        }
        PasajeroExterno p = pasajeros.get(id);
        if (p == null) { // el cliente real traduce el 404 a esta excepción
            throw new RecursoNoEncontradoException("Pasajero " + id + " no existe");
        }
        return p;
    }
}
