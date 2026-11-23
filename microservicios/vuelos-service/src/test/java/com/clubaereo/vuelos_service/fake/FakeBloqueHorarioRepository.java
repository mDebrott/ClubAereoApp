package com.clubaereo.vuelos_service.fake;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.clubaereo.vuelos_service.entities.BloqueHorario;
import com.clubaereo.vuelos_service.repository.BloqueHorarioRepository;

public class FakeBloqueHorarioRepository extends InMemoryRepository<BloqueHorario, BloqueHorarioRepository> {

    public FakeBloqueHorarioRepository() {
        super(BloqueHorarioRepository.class);
    }

    @Override
    protected Long obtenerId(BloqueHorario b) {
        return b.getId();
    }

    @Override
    protected void asignarId(BloqueHorario b, Long id) {
        b.setId(id);
    }

    /** Derived query: bloques de la jornada, ordenados por horaInicio ascendente. */
    public List<BloqueHorario> findByJornadaIdOrderByHoraInicio(Long jornadaId) {
        return datos.values().stream()
                .filter(b -> b.getJornada() != null && Objects.equals(b.getJornada().getId(), jornadaId))
                .sorted(Comparator.comparing(BloqueHorario::getHoraInicio,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }
}
