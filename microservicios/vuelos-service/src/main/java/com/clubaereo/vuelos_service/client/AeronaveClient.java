package com.clubaereo.vuelos_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.clubaereo.vuelos_service.exception.RecursoNoEncontradoException;
import com.clubaereo.vuelos_service.exception.ServicioNoDisponibleException;

@Component
public class AeronaveClient {

    private final RestClient rest;

    public AeronaveClient(@Value("${clients.aeronaves.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.rest = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public AeronaveExterna obtenerAeronave(Long id) {
        try {
            return rest.get()
                    .uri("/api/aeronaves/{id}", id)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                        throw new RecursoNoEncontradoException("Aeronave " + id + " no existe");
                    })
                    .body(AeronaveExterna.class);
        } catch (RestClientException e) {
            throw new ServicioNoDisponibleException("aeronaves-service no responde: " + e.getMessage());
        }
    }

    public PilotoExterno obtenerPiloto(Long id) {
        try {
            return rest.get()
                    .uri("/api/pilotos/{id}", id)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                        throw new RecursoNoEncontradoException("Piloto " + id + " no existe");
                    })
                    .body(PilotoExterno.class);
        } catch (RestClientException e) {
            throw new ServicioNoDisponibleException("aeronaves-service no responde: " + e.getMessage());
        }
    }
}
