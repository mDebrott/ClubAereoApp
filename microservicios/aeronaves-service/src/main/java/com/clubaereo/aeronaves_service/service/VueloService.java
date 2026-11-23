package com.clubaereo.aeronaves_service.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clubaereo.aeronaves_service.dto.AbrirVueloRequest;
import com.clubaereo.aeronaves_service.dto.CerrarVueloRequest;
import com.clubaereo.aeronaves_service.dto.EstadisticasVueloDTO;
import com.clubaereo.aeronaves_service.dto.VueloResponse;
import com.clubaereo.aeronaves_service.entities.Aeronave;
import com.clubaereo.aeronaves_service.entities.EstadoVuelo;
import com.clubaereo.aeronaves_service.entities.Piloto;
import com.clubaereo.aeronaves_service.entities.RegistroVuelo;
import com.clubaereo.aeronaves_service.exception.RecursoNoEncontradoException;
import com.clubaereo.aeronaves_service.exception.ReglaNegocioException;
import com.clubaereo.aeronaves_service.repository.AeronaveRepository;
import com.clubaereo.aeronaves_service.repository.PilotoRepository;
import com.clubaereo.aeronaves_service.repository.RegistroVueloRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VueloService {

    private final AeronaveRepository aeronaveRepository;
    private final PilotoRepository pilotoRepository;
    private final RegistroVueloRepository vueloRepository;

    @Transactional
    public VueloResponse abrirVuelo(AbrirVueloRequest req) {
        Aeronave aeronave = aeronaveRepository.findById(req.aeronaveId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Aeronave no encontrada: " + req.aeronaveId()));

        Piloto piloto = pilotoRepository.findById(req.pilotoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Piloto no encontrado: " + req.pilotoId()));

        if (!aeronave.getDisponible()) {
            throw new ReglaNegocioException(
                    "La aeronave " + aeronave.getMatricula() + " no está disponible");
        }
        if (!piloto.getLicenciaDgacVigente()) {
            throw new ReglaNegocioException(
                    "El piloto " + piloto.getNombre() + " no tiene licencia DGAC vigente");
        }
        if (vueloRepository.existsByAeronaveIdAndEstado(aeronave.getId(), EstadoVuelo.ABIERTO)) {
            throw new ReglaNegocioException(
                    "La aeronave " + aeronave.getMatricula() + " ya tiene un vuelo abierto");
        }
        if (vueloRepository.existsByPilotoIdAndEstado(piloto.getId(), EstadoVuelo.ABIERTO)) {
            throw new ReglaNegocioException(
                    "El piloto " + piloto.getNombre() + " ya tiene un vuelo abierto");
        }

        RegistroVuelo registro = RegistroVuelo.builder()
                .aeronave(aeronave)
                .piloto(piloto)
                .jornadaId(req.jornadaId())
                .horometroInicial(req.horometroInicial())
                .estado(EstadoVuelo.ABIERTO)
                .build();

        return VueloResponse.from(vueloRepository.save(registro));
    }

    @Transactional
    public VueloResponse cerrarVuelo(Long id, CerrarVueloRequest req) {
        RegistroVuelo registro = vueloRepository.findByIdConDetalle(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vuelo no encontrado: " + id));

        if (registro.getEstado() != EstadoVuelo.ABIERTO) {
            throw new ReglaNegocioException("El vuelo " + id + " ya está cerrado");
        }
        if (req.horometroFinal().compareTo(registro.getHorometroInicial()) <= 0) {
            throw new ReglaNegocioException(
                    "El horómetro final debe ser mayor al inicial ("
                            + registro.getHorometroInicial() + ")");
        }
        if (req.nTripulantes() > registro.getAeronave().getCapacidad()) {
            throw new ReglaNegocioException(
                    "Los tripulantes (" + req.nTripulantes() + ") superan la capacidad de la aeronave ("
                            + registro.getAeronave().getCapacidad() + ")");
        }

        BigDecimal horas = req.horometroFinal()
                .subtract(registro.getHorometroInicial())
                .setScale(1, RoundingMode.HALF_UP);

        registro.setHorometroFinal(req.horometroFinal());
        registro.setNTripulantes(req.nTripulantes());
        registro.setHorasConsumidas(horas);
        registro.setEstado(EstadoVuelo.CERRADO);

        return VueloResponse.from(vueloRepository.save(registro));
    }

    @Transactional(readOnly = true)
    public List<VueloResponse> listarPorJornada(Long jornadaId) {
        return vueloRepository.findByJornadaIdConDetalle(jornadaId).stream()
                .map(VueloResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EstadisticasVueloDTO obtenerEstadisticas(Long jornadaId) {
        EstadisticasVueloDTO e = vueloRepository.obtenerEstadisticas(jornadaId);
        return new EstadisticasVueloDTO(
                e.totalVuelos(),
                e.pilotosDistintos(),
                e.aeronavesDistintas(),
                e.totalTripulantes() != null ? e.totalTripulantes() : Long.valueOf(0L),
                e.horasTotales() != null ? e.horasTotales() : BigDecimal.ZERO);
    }
}
