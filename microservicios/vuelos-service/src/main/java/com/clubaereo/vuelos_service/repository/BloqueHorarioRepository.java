package com.clubaereo.vuelos_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.clubaereo.vuelos_service.entities.BloqueHorario;

public interface BloqueHorarioRepository extends JpaRepository<BloqueHorario, Long> {
    List<BloqueHorario> findByJornadaIdOrderByHoraInicio(Long jornadaId);
}
