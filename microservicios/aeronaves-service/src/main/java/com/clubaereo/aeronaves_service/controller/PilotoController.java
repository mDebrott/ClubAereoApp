package com.clubaereo.aeronaves_service.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clubaereo.aeronaves_service.dto.DisponibilidadRequest;
import com.clubaereo.aeronaves_service.dto.PilotoRequest;
import com.clubaereo.aeronaves_service.dto.PilotoResponse;
import com.clubaereo.aeronaves_service.service.PilotoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pilotos")
@RequiredArgsConstructor
public class PilotoController {

    private final PilotoService pilotoService;

    @GetMapping
    public List<PilotoResponse> listar() {
        return pilotoService.listar();
    }

    @GetMapping("/{id}")
    public PilotoResponse obtener(@PathVariable Long id) {
        return pilotoService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<PilotoResponse> crear(@Valid @RequestBody PilotoRequest req) {
        PilotoResponse creado = pilotoService.crear(req);
        return ResponseEntity.created(URI.create("/api/pilotos/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}/licencia")
    public PilotoResponse cambiarLicencia(@PathVariable Long id,
                                          @Valid @RequestBody DisponibilidadRequest req) {
        return pilotoService.cambiarLicencia(id, req.valor());
    }
}
