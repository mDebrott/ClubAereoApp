package com.clubaereo.aeronaves_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.clubaereo.aeronaves_service.entities.Aeronave;

public interface AeronaveRepository extends JpaRepository<Aeronave, Long>{

    List<Aeronave> findByDisponibleTrue();

    boolean existsByMatricula(String matricula);
}
