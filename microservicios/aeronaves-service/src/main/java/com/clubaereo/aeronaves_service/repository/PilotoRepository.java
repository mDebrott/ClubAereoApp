package com.clubaereo.aeronaves_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.clubaereo.aeronaves_service.entities.Piloto;

public interface PilotoRepository extends JpaRepository<Piloto, Long> {

}
