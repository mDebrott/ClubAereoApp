package com.clubaereo.vuelos_service.fake;

import java.util.HashMap;
import java.util.Map;

import com.clubaereo.vuelos_service.client.AeronaveClient;
import com.clubaereo.vuelos_service.client.AeronaveExterna;
import com.clubaereo.vuelos_service.client.PilotoExterno;
import com.clubaereo.vuelos_service.exception.RecursoNoEncontradoException;
import com.clubaereo.vuelos_service.exception.ServicioNoDisponibleException;

/** Cliente falso de aeronaves-service (aeronaves y pilotos). Ver nota en {@link FakePasajeroClient}. */
public class FakeAeronaveClient extends AeronaveClient {

    private final Map<Long, AeronaveExterna> aeronaves = new HashMap<>();
    private final Map<Long, PilotoExterno> pilotos = new HashMap<>();
    private boolean caido = false;

    public FakeAeronaveClient() {
        super("http://aeronaves-service.fake");
    }

    public void registrarAeronave(AeronaveExterna aeronave) {
        aeronaves.put(aeronave.id(), aeronave);
    }

    public void registrarPiloto(PilotoExterno piloto) {
        pilotos.put(piloto.id(), piloto);
    }

    /** Simula que aeronaves-service no responde. */
    public void caer() {
        caido = true;
    }

    @Override
    public AeronaveExterna obtenerAeronave(Long id) {
        verificarDisponible();
        AeronaveExterna a = aeronaves.get(id);
        if (a == null) {
            throw new RecursoNoEncontradoException("Aeronave " + id + " no existe");
        }
        return a;
    }

    @Override
    public PilotoExterno obtenerPiloto(Long id) {
        verificarDisponible();
        PilotoExterno p = pilotos.get(id);
        if (p == null) {
            throw new RecursoNoEncontradoException("Piloto " + id + " no existe");
        }
        return p;
    }

    private void verificarDisponible() {
        if (caido) {
            throw new ServicioNoDisponibleException("aeronaves-service no responde: simulado");
        }
    }
}
