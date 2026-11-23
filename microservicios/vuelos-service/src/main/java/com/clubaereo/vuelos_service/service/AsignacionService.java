package com.clubaereo.vuelos_service.service;

import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clubaereo.vuelos_service.client.AeronaveClient;
import com.clubaereo.vuelos_service.client.AeronaveExterna;
import com.clubaereo.vuelos_service.client.PasajeroClient;
import com.clubaereo.vuelos_service.client.PasajeroExterno;
import com.clubaereo.vuelos_service.client.PilotoExterno;
import com.clubaereo.vuelos_service.dto.AsignacionRequest;
import com.clubaereo.vuelos_service.dto.AsignacionResponse;
import com.clubaereo.vuelos_service.entities.Asignacion;
import com.clubaereo.vuelos_service.entities.BloqueHorario;
import com.clubaereo.vuelos_service.entities.EstadoAsignacion;
import com.clubaereo.vuelos_service.exception.ConflictoException;
import com.clubaereo.vuelos_service.exception.RecursoNoEncontradoException;
import com.clubaereo.vuelos_service.repository.AsignacionRepository;
import com.clubaereo.vuelos_service.repository.BloqueHorarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsignacionService {

    /** Id que nunca existe: se usa al crear para no excluir ninguna asignación. */
    private static final Long SIN_EXCLUIR = -1L;

    private final AsignacionRepository asignacionRepo;
    private final BloqueHorarioRepository bloqueRepo;
    private final PasajeroClient pasajeroClient;
    private final AeronaveClient aeronaveClient;

    @Transactional
    public AsignacionResponse crear(AsignacionRequest req) {
        BloqueHorario bloque = buscarBloque(req.bloqueId());
        validar(req, bloque, SIN_EXCLUIR);

        Asignacion a = new Asignacion();
        a.setPasajeroId(req.pasajeroId());
        a.setAeronaveId(req.aeronaveId());
        a.setPilotoId(req.pilotoId());
        a.setBloque(bloque);
        // estado queda en PENDIENTE por defecto

        return AsignacionResponse.from(asignacionRepo.save(a));
    }

    @Transactional
    public AsignacionResponse actualizar(Long id, AsignacionRequest req) {
        Asignacion a = buscarAsignacion(id);

        if (a.getEstado() != EstadoAsignacion.PENDIENTE) {
            throw new ConflictoException("Solo se pueden reasignar asignaciones PENDIENTES");
        }

        BloqueHorario bloque = a.getBloque().getId().equals(req.bloqueId())
                ? a.getBloque()
                : buscarBloque(req.bloqueId());

        // Se valida siempre (cambie el bloque, el pasajero, la aeronave o el piloto),
        // excluyendo la propia asignación para que no choque consigo misma.
        validar(req, bloque, id);

        a.setBloque(bloque);
        a.setPasajeroId(req.pasajeroId());
        a.setAeronaveId(req.aeronaveId());
        a.setPilotoId(req.pilotoId());

        return AsignacionResponse.from(a); // @Transactional persiste el cambio solo
    }

    @Transactional
    public AsignacionResponse confirmar(Long id) {
        Asignacion a = buscarAsignacion(id);

        if (a.getEstado() == EstadoAsignacion.CANCELADA) {
            throw new ConflictoException("No se puede confirmar una asignación cancelada");
        }
        if (a.getEstado() == EstadoAsignacion.CONFIRMADA) {
            return AsignacionResponse.from(a); // idempotente
        }

        PasajeroExterno pasajero = pasajeroClient.obtener(a.getPasajeroId());
        if (!pasajero.consentimientoFirmado()) {
            throw new ConflictoException(
                    "El pasajero debe firmar el consentimiento antes de confirmar el vuelo");
        }
        if (pasajero.pesoKg() == null) {
            throw new ConflictoException("El pasajero no tiene peso registrado");
        }

        a.setEstado(EstadoAsignacion.CONFIRMADA);
        return AsignacionResponse.from(a);
    }

    @Transactional
    public AsignacionResponse cancelar(Long id) {
        Asignacion a = buscarAsignacion(id);
        a.setEstado(EstadoAsignacion.CANCELADA); // idempotente
        return AsignacionResponse.from(a);
    }

    /** Reglas de negocio comunes a crear y actualizar. */
    private void validar(AsignacionRequest req, BloqueHorario bloque, Long excluirId) {
        Long jornadaId = bloque.getJornada().getId();

        PasajeroExterno pasajero = pasajeroClient.obtener(req.pasajeroId());
        if (!jornadaId.equals(pasajero.jornadaId())) {
            throw new ConflictoException("El pasajero está inscrito en otra jornada");
        }

        AeronaveExterna aeronave = aeronaveClient.obtenerAeronave(req.aeronaveId());
        if (!Boolean.TRUE.equals(aeronave.disponible())) {
            throw new ConflictoException("La aeronave " + aeronave.matricula() + " no está disponible");
        }

        PilotoExterno piloto = aeronaveClient.obtenerPiloto(req.pilotoId());
        if (!Boolean.TRUE.equals(piloto.licenciaDgacVigente())) {
            throw new ConflictoException(
                    "El piloto " + piloto.nombre() + " no tiene licencia DGAC vigente");
        }

        LocalTime inicio = bloque.getHoraInicio();
        LocalTime fin = bloque.getHoraFin();
        EstadoAsignacion cancelada = EstadoAsignacion.CANCELADA;

        if (asignacionRepo.contarPasajeroSolapado(
                req.pasajeroId(), jornadaId, inicio, fin, cancelada, excluirId) > 0) {
            throw new ConflictoException(
                    "El pasajero ya tiene una asignación en un bloque que se solapa");
        }
        if (asignacionRepo.contarAeronaveConOtroPiloto(
                req.aeronaveId(), req.pilotoId(), jornadaId, inicio, fin, cancelada, excluirId) > 0) {
            throw new ConflictoException(
                    "La aeronave ya está asignada a otro piloto en ese horario");
        }
        if (asignacionRepo.contarPilotoConOtraAeronave(
                req.pilotoId(), req.aeronaveId(), jornadaId, inicio, fin, cancelada, excluirId) > 0) {
            throw new ConflictoException(
                    "El piloto ya está asignado a otra aeronave en ese horario");
        }
        long ocupados = asignacionRepo.contarOcupacion(
                req.aeronaveId(), req.pilotoId(), jornadaId, inicio, fin, cancelada, excluirId);
        if (ocupados >= aeronave.capacidad()) {
            throw new ConflictoException(
                    "La aeronave " + aeronave.matricula() + " no tiene cupos en ese horario (capacidad "
                            + aeronave.capacidad() + ")");
        }
    }

    private BloqueHorario buscarBloque(Long id) {
        return bloqueRepo.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Bloque " + id + " no existe"));
    }

    private Asignacion buscarAsignacion(Long id) {
        return asignacionRepo.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Asignación " + id + " no existe"));
    }
}
