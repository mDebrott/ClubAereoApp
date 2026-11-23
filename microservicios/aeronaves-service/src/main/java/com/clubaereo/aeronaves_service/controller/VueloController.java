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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clubaereo.aeronaves_service.dto.AbrirVueloRequest;
import com.clubaereo.aeronaves_service.dto.CerrarVueloRequest;
import com.clubaereo.aeronaves_service.dto.EstadisticasVueloDTO;
import com.clubaereo.aeronaves_service.dto.VueloResponse;
import com.clubaereo.aeronaves_service.service.VueloService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api/vuelos")
@RequiredArgsConstructor 
public class VueloController {

    private final VueloService vueloService;

    @PostMapping 
    public ResponseEntity<VueloResponse> abrir(@Valid @RequestBody AbrirVueloRequest request) {
        VueloResponse creado = vueloService.abrirVuelo(request);
        return ResponseEntity
                .created(URI.create("/api/vuelos/" + creado.id()))
                .body(creado);
    }

    @PutMapping ("/{id}/cierre")
    public VueloResponse cerrar(@PathVariable Long id,
                                @Valid @RequestBody CerrarVueloRequest request) {
        return vueloService.cerrarVuelo(id, request);
    }

    @GetMapping
    public List<VueloResponse> listarPorJornada(@RequestParam Long jornadaId) {
        return vueloService.listarPorJornada(jornadaId);
    }

    @GetMapping ("/estadisticas")
    public EstadisticasVueloDTO estadisticas(@RequestParam Long jornadaId) {
        return vueloService.obtenerEstadisticas(jornadaId);
    }
}