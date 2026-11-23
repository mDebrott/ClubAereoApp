package com.clubaereo.vuelos_service.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clubaereo.vuelos_service.dto.AsignacionResponse;
import com.clubaereo.vuelos_service.dto.BloqueRequest;
import com.clubaereo.vuelos_service.dto.BloqueResponse;
import com.clubaereo.vuelos_service.dto.JornadaRequest;
import com.clubaereo.vuelos_service.dto.JornadaResponse;
import com.clubaereo.vuelos_service.entities.BloqueHorario;
import com.clubaereo.vuelos_service.entities.Jornada;
import com.clubaereo.vuelos_service.exception.ConflictoException;
import com.clubaereo.vuelos_service.exception.RecursoNoEncontradoException;
import com.clubaereo.vuelos_service.repository.AsignacionRepository;
import com.clubaereo.vuelos_service.repository.BloqueHorarioRepository;
import com.clubaereo.vuelos_service.repository.JornadaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JornadaService {

    private final JornadaRepository jornadaRepo;
    private final BloqueHorarioRepository bloqueRepo;
    private final AsignacionRepository asignacionRepo;

    @Transactional(readOnly = true)
    public List<JornadaResponse> listarJornadas() {
        return jornadaRepo.findAll().stream().map(JornadaResponse::from).toList();
    }

    @Transactional
    public JornadaResponse crearJornada(JornadaRequest req) {
        if (jornadaRepo.findByFechaAndBase(req.fecha(), req.base()).isPresent()) {
            throw new ConflictoException("Ya existe una jornada para esa fecha y base");
        }
        Jornada j = new Jornada();
        j.setFecha(req.fecha());
        j.setBase(req.base());
        return JornadaResponse.from(jornadaRepo.save(j));
    }

    @Transactional
    public BloqueResponse crearBloque(Long jornadaId, BloqueRequest req) {
        Jornada jornada = jornadaRepo.findById(jornadaId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Jornada " + jornadaId + " no existe"));

        if (!req.horaFin().isAfter(req.horaInicio())) {
            throw new ConflictoException("La hora de fin debe ser posterior a la hora de inicio");
        }
        boolean seSolapa = bloqueRepo.findByJornadaIdOrderByHoraInicio(jornadaId).stream()
            .anyMatch(b -> req.horaInicio().isBefore(b.getHoraFin())
                        && req.horaFin().isAfter(b.getHoraInicio()));
        if (seSolapa) {
            throw new ConflictoException("El bloque se solapa con otro bloque de la jornada");
        }

        BloqueHorario b = new BloqueHorario();
        b.setJornada(jornada);
        b.setHoraInicio(req.horaInicio());
        b.setHoraFin(req.horaFin());
        b = bloqueRepo.save(b);
        return new BloqueResponse(b.getId(), b.getHoraInicio(), b.getHoraFin(), List.of());
    }

    @Transactional(readOnly = true)
    public List<BloqueResponse> obtenerBloques(Long jornadaId) {
        if (!jornadaRepo.existsById(jornadaId)) {
            throw new RecursoNoEncontradoException("Jornada " + jornadaId + " no existe");
        }

        // Consulta 1: bloques
        List<BloqueHorario> bloques = bloqueRepo.findByJornadaIdOrderByHoraInicio(jornadaId);

        // Consulta 2: asignaciones, agrupadas por bloque
        Map<Long, List<AsignacionResponse>> porBloque = asignacionRepo.findByJornadaId(jornadaId)
            .stream()
            .map(AsignacionResponse::from)
            .collect(Collectors.groupingBy(AsignacionResponse::bloqueId));

        return bloques.stream()
            .map(b -> new BloqueResponse(
                b.getId(), b.getHoraInicio(), b.getHoraFin(),
                porBloque.getOrDefault(b.getId(), List.of())))
            .toList();
    }
}
