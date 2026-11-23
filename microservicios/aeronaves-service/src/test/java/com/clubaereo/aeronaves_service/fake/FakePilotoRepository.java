package com.clubaereo.aeronaves_service.fake;

import com.clubaereo.aeronaves_service.entities.Piloto;
import com.clubaereo.aeronaves_service.repository.PilotoRepository;

public class FakePilotoRepository extends InMemoryRepository<Piloto, PilotoRepository> {

    @Override
    protected Long getId(Piloto p) { return p.getId(); }

    @Override
    protected void setId(Piloto p, Long id) { p.setId(id); }

    // PilotoRepository no declara métodos propios: con el CRUD base basta.

    public PilotoRepository repo() {
        return comoRepositorio(PilotoRepository.class);
    }
}
