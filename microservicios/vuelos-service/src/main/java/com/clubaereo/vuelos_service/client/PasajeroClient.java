package com.clubaereo.vuelos_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.clubaereo.vuelos_service.exception.RecursoNoEncontradoException;
import com.clubaereo.vuelos_service.exception.ServicioNoDisponibleException;

@Component
public class PasajeroClient {

    private final RestClient rest;

    public PasajeroClient(@Value("${clients.pasajero.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.rest = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public PasajeroExterno obtener(Long id) {
        try {
            return rest.get()
                    .uri("/api/pasajeros/{id}", id)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                        throw new RecursoNoEncontradoException("Pasajero " + id + " no existe");
                    })
                    .body(PasajeroExterno.class);
        } catch (RestClientException e) {
            throw new ServicioNoDisponibleException("pasajero-service no responde: " + e.getMessage());
        }
    }
}
