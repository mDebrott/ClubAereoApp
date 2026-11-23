package com.clubaereo.vuelos_service.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clubaereo.vuelos_service.dto.AsignacionRequest;
import com.clubaereo.vuelos_service.dto.AsignacionResponse;
import com.clubaereo.vuelos_service.service.AsignacionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api/asignaciones")
@RequiredArgsConstructor 
public class AsignacionController {

    private final AsignacionService service;

    @PostMapping 
    public ResponseEntity<AsignacionResponse> crear(@Valid @RequestBody AsignacionRequest req) {
        AsignacionResponse creada = service.crear(req);
        URI location = URI.create("/api/asignaciones/" + creada.id());
        return ResponseEntity.created(location).body(creada); // 201
    }

    @PutMapping ("/{id}")
    public AsignacionResponse actualizar(@PathVariable Long id,
                                         @Valid @RequestBody AsignacionRequest req) {
        return service.actualizar(id, req);
    }

    @PutMapping("/{id}/confirmar")
    public AsignacionResponse confirmar(@PathVariable Long id) {
        return service.confirmar(id);
    }

    @PutMapping("/{id}/cancelar")
    public AsignacionResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}
