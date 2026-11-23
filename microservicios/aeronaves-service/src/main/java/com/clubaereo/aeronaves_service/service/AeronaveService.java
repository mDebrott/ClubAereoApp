package com.clubaereo.aeronaves_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clubaereo.aeronaves_service.dto.AeronaveRequest;
import com.clubaereo.aeronaves_service.dto.AeronaveResponse;
import com.clubaereo.aeronaves_service.entities.Aeronave;
import com.clubaereo.aeronaves_service.exception.RecursoNoEncontradoException;
import com.clubaereo.aeronaves_service.exception.ReglaNegocioException;
import com.clubaereo.aeronaves_service.repository.AeronaveRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AeronaveService {

    private final AeronaveRepository aeronaveRepository;

    @Transactional(readOnly = true)
    public List<AeronaveResponse> listarDisponibles() {
        return aeronaveRepository.findByDisponibleTrue()
                .stream()
                .map(AeronaveResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AeronaveResponse obtener(Long id) {
        return AeronaveResponse.from(buscar(id));
    }

    @Transactional
    public AeronaveResponse crear(AeronaveRequest req) {
        String matricula = req.matricula().trim().toUpperCase();
        if (aeronaveRepository.existsByMatricula(matricula)) {
            throw new ReglaNegocioException("Ya existe una aeronave con matrícula " + matricula);
        }
        Aeronave a = new Aeronave();
        a.setMatricula(matricula);
        a.setModelo(req.modelo().trim());
        a.setCapacidad(req.capacidad());
        a.setDisponible(true);
        return AeronaveResponse.from(aeronaveRepository.save(a));
    }

    @Transactional
    public AeronaveResponse cambiarDisponibilidad(Long id, boolean disponible) {
        Aeronave a = buscar(id);
        a.setDisponible(disponible);
        return AeronaveResponse.from(aeronaveRepository.save(a));
    }

    private Aeronave buscar(Long id) {
        return aeronaveRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Aeronave no encontrada: " + id));
    }
}
