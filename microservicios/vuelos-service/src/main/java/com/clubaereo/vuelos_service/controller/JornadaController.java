package com.clubaereo.vuelos_service.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clubaereo.vuelos_service.dto.BloqueRequest;
import com.clubaereo.vuelos_service.dto.BloqueResponse;
import com.clubaereo.vuelos_service.dto.JornadaRequest;
import com.clubaereo.vuelos_service.dto.JornadaResponse;
import com.clubaereo.vuelos_service.service.JornadaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/jornadas")
@RequiredArgsConstructor
public class JornadaController {

    private final JornadaService service;

    @GetMapping
    public List<JornadaResponse> listar() {
        return service.listarJornadas();
    }

    @PostMapping
    public ResponseEntity<JornadaResponse> crear(@Valid @RequestBody JornadaRequest req) {
        JornadaResponse creada = service.crearJornada(req);
        return ResponseEntity.created(URI.create("/api/jornadas/" + creada.id())).body(creada);
    }

    @PostMapping("/{id}/bloques")
    public ResponseEntity<BloqueResponse> crearBloque(@PathVariable Long id,
                                                      @Valid @RequestBody BloqueRequest req) {
        BloqueResponse creado = service.crearBloque(id, req);
        return ResponseEntity.created(URI.create("/api/jornadas/" + id + "/bloques")).body(creado);
    }

    @GetMapping("/{id}/bloques")
    public List<BloqueResponse> bloques(@PathVariable Long id) {
        return service.obtenerBloques(id);
    }
}
