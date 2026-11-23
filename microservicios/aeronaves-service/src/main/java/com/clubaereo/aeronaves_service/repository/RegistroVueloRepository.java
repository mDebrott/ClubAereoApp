package com.clubaereo.aeronaves_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.clubaereo.aeronaves_service.dto.EstadisticasVueloDTO;
import com.clubaereo.aeronaves_service.entities.EstadoVuelo;
import com.clubaereo.aeronaves_service.entities.RegistroVuelo;

public interface RegistroVueloRepository extends JpaRepository<RegistroVuelo, Long> {

    boolean existsByAeronaveIdAndEstado(Long aeronaveId, EstadoVuelo estado);

    boolean existsByPilotoIdAndEstado(Long pilotoId, EstadoVuelo estado);

    @Query("""
           SELECT r FROM RegistroVuelo r
           JOIN FETCH r.aeronave
           JOIN FETCH r.piloto
           WHERE r.jornadaId = :jornadaId
           """)
    List<RegistroVuelo> findByJornadaIdConDetalle(@Param("jornadaId") Long jornadaId);

    @Query("""
           SELECT r FROM RegistroVuelo r
           JOIN FETCH r.aeronave
           JOIN FETCH r.piloto
           WHERE r.id = :id
           """)
    Optional<RegistroVuelo> findByIdConDetalle(@Param("id") Long id);

    @Query("""
           SELECT new com.clubaereo.aeronaves_service.dto.EstadisticasVueloDTO(
               COUNT(r),
               COUNT(DISTINCT r.piloto.id),
               COUNT(DISTINCT r.aeronave.id),
               SUM(r.nTripulantes),
               SUM(r.horasConsumidas)
           )
           FROM RegistroVuelo r
           WHERE r.jornadaId = :jornadaId
             AND r.estado = com.clubaereo.aeronaves_service.entities.EstadoVuelo.CERRADO
           """)
    EstadisticasVueloDTO obtenerEstadisticas(@Param("jornadaId") Long jornadaId);
}
