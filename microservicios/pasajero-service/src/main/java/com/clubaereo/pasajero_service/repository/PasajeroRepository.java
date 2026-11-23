package com.clubaereo.pasajero_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clubaereo.pasajero_service.entities.Pasajero;

public interface PasajeroRepository extends JpaRepository<Pasajero, Long>
{
    List<Pasajero> findByJornadaId(Long jornadaId);

    Optional<Pasajero> findByRut(String rut);

    Optional<Pasajero> findByRutAndJornadaId(String rut, Long jornadaId);

    boolean existsByRutAndJornadaId(String rut, Long jornadaId);
}
