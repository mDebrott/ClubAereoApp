package com.clubaereo.pasajero_service.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.clubaereo.pasajero_service.dto.*;
import com.clubaereo.pasajero_service.service.PasajeroService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pasajeros")
public class PasajeroController {

    private final PasajeroService service;

    public PasajeroController(PasajeroService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<PasajeroResponse> inscribir(@Valid @RequestBody PasajeroRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.inscribir(req));
    }

    @GetMapping
    public List<PasajeroResponse> listar(@RequestParam Long jornadaId) {
        return service.listarPorJornada(jornadaId);
    }

    @GetMapping("/{id}")
    public PasajeroResponse obtener(@PathVariable Long id) { return service.obtener(id); }

    // POST (y no GET) para que el RUT viaje en el cuerpo y no en la URL
    @PostMapping("/busqueda")
    public PasajeroResponse buscarPorRut(@Valid @RequestBody BusquedaRutRequest req) {
        return service.buscarPorRut(req.rut(), req.jornadaId());
    }

    @PutMapping("/{id}/peso")
    public PasajeroResponse registrarPeso(@PathVariable Long id, @Valid @RequestBody PesoRequest req) {
        return service.registrarPeso(id, req);
    }

    @PostMapping("/{id}/consentimiento")
    public PasajeroResponse firmar(@PathVariable Long id, @Valid @RequestBody ConsentimientoRequest req) {
        return service.firmar(id, req);
    }
}