package com.clubaereo.vuelos_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clubaereo.vuelos_service.entities.Base;
import com.clubaereo.vuelos_service.entities.Jornada;
import java.time.LocalDate;


public interface JornadaRepository extends JpaRepository<Jornada, Long> {
    Optional<Jornada> findByFechaAndBase(LocalDate fecha, Base base);
}
