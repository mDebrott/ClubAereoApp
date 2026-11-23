package com.clubaereo.vuelos_service.fake;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import com.clubaereo.vuelos_service.entities.Base;
import com.clubaereo.vuelos_service.entities.Jornada;
import com.clubaereo.vuelos_service.repository.JornadaRepository;

public class FakeJornadaRepository extends InMemoryRepository<Jornada, JornadaRepository> {

    public FakeJornadaRepository() {
        super(JornadaRepository.class);
    }

    @Override
    protected Long obtenerId(Jornada j) {
        return j.getId();
    }

    @Override
    protected void asignarId(Jornada j, Long id) {
        j.setId(id);
    }

    public Optional<Jornada> findByFechaAndBase(LocalDate fecha, Base base) {
        return datos.values().stream()
                .filter(j -> Objects.equals(j.getFecha(), fecha) && j.getBase() == base)
                .findFirst();
    }
}
