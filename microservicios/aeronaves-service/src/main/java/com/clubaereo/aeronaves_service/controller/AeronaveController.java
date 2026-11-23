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

import com.clubaereo.aeronaves_service.dto.AeronaveRequest;
import com.clubaereo.aeronaves_service.dto.AeronaveResponse;
import com.clubaereo.aeronaves_service.dto.DisponibilidadRequest;
import com.clubaereo.aeronaves_service.service.AeronaveService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/aeronaves")
@RequiredArgsConstructor
public class AeronaveController {

    private final AeronaveService aeronaveService;

    @GetMapping
    public List<AeronaveResponse> listarDisponibles() {
        return aeronaveService.listarDisponibles();
    }

    @GetMapping("/{id}")
    public AeronaveResponse obtener(@PathVariable Long id) {
        return aeronaveService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<AeronaveResponse> crear(@Valid @RequestBody AeronaveRequest req) {
        AeronaveResponse creada = aeronaveService.crear(req);
        return ResponseEntity.created(URI.create("/api/aeronaves/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}/disponibilidad")
    public AeronaveResponse cambiarDisponibilidad(@PathVariable Long id,
                                                  @Valid @RequestBody DisponibilidadRequest req) {
        return aeronaveService.cambiarDisponibilidad(id, req.valor());
    }
}
