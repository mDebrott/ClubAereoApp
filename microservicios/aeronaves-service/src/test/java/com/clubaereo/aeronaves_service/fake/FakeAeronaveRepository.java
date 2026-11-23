package com.clubaereo.aeronaves_service.fake;

import java.util.List;

import com.clubaereo.aeronaves_service.entities.Aeronave;
import com.clubaereo.aeronaves_service.repository.AeronaveRepository;

public class FakeAeronaveRepository extends InMemoryRepository<Aeronave, AeronaveRepository> {

    @Override
    protected Long getId(Aeronave a) { return a.getId(); }

    @Override
    protected void setId(Aeronave a, Long id) { a.setId(id); }

    // Métodos propios de AeronaveRepository (consultas derivadas de Spring Data)

    public List<Aeronave> findByDisponibleTrue() {
        return datos.values().stream()
                .filter(Aeronave::getDisponible)
                .toList();
    }

    public boolean existsByMatricula(String matricula) {
        return datos.values().stream()
                .anyMatch(a -> a.getMatricula().equals(matricula));
    }

    public AeronaveRepository repo() {
        return comoRepositorio(AeronaveRepository.class);
    }
}
