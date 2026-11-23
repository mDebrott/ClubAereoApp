package com.clubaereo.aeronaves_service.fake;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.clubaereo.aeronaves_service.dto.EstadisticasVueloDTO;
import com.clubaereo.aeronaves_service.entities.EstadoVuelo;
import com.clubaereo.aeronaves_service.entities.RegistroVuelo;
import com.clubaereo.aeronaves_service.repository.RegistroVueloRepository;

public class FakeRegistroVueloRepository
        extends InMemoryRepository<RegistroVuelo, RegistroVueloRepository> {

    @Override
    protected Long getId(RegistroVuelo r) { return r.getId(); }

    @Override
    protected void setId(RegistroVuelo r, Long id) { r.setId(id); }

    public boolean existsByAeronaveIdAndEstado(Long aeronaveId, EstadoVuelo estado) {
        return datos.values().stream().anyMatch(r ->
                r.getAeronave().getId().equals(aeronaveId) && r.getEstado() == estado);
    }

    public boolean existsByPilotoIdAndEstado(Long pilotoId, EstadoVuelo estado) {
        return datos.values().stream().anyMatch(r ->
                r.getPiloto().getId().equals(pilotoId) && r.getEstado() == estado);
    }

    // En la BD real el JOIN FETCH solo evita LazyInitialization; en memoria no hace falta.
    public List<RegistroVuelo> findByJornadaIdConDetalle(Long jornadaId) {
        return datos.values().stream()
                .filter(r -> r.getJornadaId().equals(jornadaId))
                .toList();
    }

    public Optional<RegistroVuelo> findByIdConDetalle(Long id) {
        return findById(id);
    }

    /** Imita la consulta JPQL: solo vuelos CERRADOS; COUNT da 0 y SUM da null si no hay filas. */
    public EstadisticasVueloDTO obtenerEstadisticas(Long jornadaId) {
        List<RegistroVuelo> cerrados = datos.values().stream()
                .filter(r -> r.getJornadaId().equals(jornadaId))
                .filter(r -> r.getEstado() == EstadoVuelo.CERRADO)
                .toList();

        long pilotos = cerrados.stream().map(r -> r.getPiloto().getId()).distinct().count();
        long aeronaves = cerrados.stream().map(r -> r.getAeronave().getId()).distinct().count();

        Long tripulantes = cerrados.isEmpty() ? null
                : cerrados.stream().map(RegistroVuelo::getNTripulantes)
                        .filter(Objects::nonNull).mapToLong(Integer::longValue).sum();
        BigDecimal horas = cerrados.isEmpty() ? null
                : cerrados.stream().map(RegistroVuelo::getHorasConsumidas)
                        .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new EstadisticasVueloDTO((long) cerrados.size(), pilotos, aeronaves, tripulantes, horas);
    }

    public RegistroVueloRepository repo() {
        return comoRepositorio(RegistroVueloRepository.class);
    }
}
