package com.clubaereo.aeronaves_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clubaereo.aeronaves_service.dto.PilotoRequest;
import com.clubaereo.aeronaves_service.dto.PilotoResponse;
import com.clubaereo.aeronaves_service.entities.Piloto;
import com.clubaereo.aeronaves_service.exception.RecursoNoEncontradoException;
import com.clubaereo.aeronaves_service.repository.PilotoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PilotoService {

    private final PilotoRepository pilotoRepository;

    @Transactional(readOnly = true)
    public List<PilotoResponse> listar() {
        return pilotoRepository.findAll().stream().map(PilotoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PilotoResponse obtener(Long id) {
        return PilotoResponse.from(buscar(id));
    }

    @Transactional
    public PilotoResponse crear(PilotoRequest req) {
        Piloto p = new Piloto();
        p.setNombre(req.nombre().trim());
        p.setLicenciaDgacVigente(req.licenciaDgacVigente() == null || req.licenciaDgacVigente());
        return PilotoResponse.from(pilotoRepository.save(p));
    }

    @Transactional
    public PilotoResponse cambiarLicencia(Long id, boolean vigente) {
        Piloto p = buscar(id);
        p.setLicenciaDgacVigente(vigente);
        return PilotoResponse.from(pilotoRepository.save(p));
    }

    private Piloto buscar(Long id) {
        return pilotoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Piloto no encontrado: " + id));
    }
}
