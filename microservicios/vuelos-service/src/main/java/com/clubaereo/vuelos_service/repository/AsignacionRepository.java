package com.clubaereo.vuelos_service.repository;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.clubaereo.vuelos_service.entities.Asignacion;
import com.clubaereo.vuelos_service.entities.EstadoAsignacion;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    @Query("""
           select a from Asignacion a
           join fetch a.bloque b
           where b.jornada.id = :jornadaId
           """)
    List<Asignacion> findByJornadaId(@Param("jornadaId") Long jornadaId);

    /** Asignaciones activas del pasajero en bloques que se solapan (incluye el mismo bloque). */
    @Query("""
           select count(a) from Asignacion a
           where a.pasajeroId = :pasajeroId
             and a.estado <> :cancelada
             and a.id <> :excluirId
             and a.bloque.jornada.id = :jornadaId
             and a.bloque.horaInicio < :fin
             and a.bloque.horaFin > :inicio
           """)
    long contarPasajeroSolapado(@Param("pasajeroId") Long pasajeroId,
                                @Param("jornadaId") Long jornadaId,
                                @Param("inicio") LocalTime inicio,
                                @Param("fin") LocalTime fin,
                                @Param("cancelada") EstadoAsignacion cancelada,
                                @Param("excluirId") Long excluirId);

    /** Pasajeros ya asignados a esa aeronave (con ese piloto) en bloques solapados: ocupación. */
    @Query("""
           select count(a) from Asignacion a
           where a.aeronaveId = :aeronaveId
             and a.pilotoId = :pilotoId
             and a.estado <> :cancelada
             and a.id <> :excluirId
             and a.bloque.jornada.id = :jornadaId
             and a.bloque.horaInicio < :fin
             and a.bloque.horaFin > :inicio
           """)
    long contarOcupacion(@Param("aeronaveId") Long aeronaveId,
                         @Param("pilotoId") Long pilotoId,
                         @Param("jornadaId") Long jornadaId,
                         @Param("inicio") LocalTime inicio,
                         @Param("fin") LocalTime fin,
                         @Param("cancelada") EstadoAsignacion cancelada,
                         @Param("excluirId") Long excluirId);

    /** La aeronave ya está usada con OTRO piloto en un horario solapado. */
    @Query("""
           select count(a) from Asignacion a
           where a.aeronaveId = :aeronaveId
             and a.pilotoId <> :pilotoId
             and a.estado <> :cancelada
             and a.id <> :excluirId
             and a.bloque.jornada.id = :jornadaId
             and a.bloque.horaInicio < :fin
             and a.bloque.horaFin > :inicio
           """)
    long contarAeronaveConOtroPiloto(@Param("aeronaveId") Long aeronaveId,
                                     @Param("pilotoId") Long pilotoId,
                                     @Param("jornadaId") Long jornadaId,
                                     @Param("inicio") LocalTime inicio,
                                     @Param("fin") LocalTime fin,
                                     @Param("cancelada") EstadoAsignacion cancelada,
                                     @Param("excluirId") Long excluirId);

    /** El piloto ya vuela OTRA aeronave en un horario solapado. */
    @Query("""
           select count(a) from Asignacion a
           where a.pilotoId = :pilotoId
             and a.aeronaveId <> :aeronaveId
             and a.estado <> :cancelada
             and a.id <> :excluirId
             and a.bloque.jornada.id = :jornadaId
             and a.bloque.horaInicio < :fin
             and a.bloque.horaFin > :inicio
           """)
    long contarPilotoConOtraAeronave(@Param("pilotoId") Long pilotoId,
                                     @Param("aeronaveId") Long aeronaveId,
                                     @Param("jornadaId") Long jornadaId,
                                     @Param("inicio") LocalTime inicio,
                                     @Param("fin") LocalTime fin,
                                     @Param("cancelada") EstadoAsignacion cancelada,
                                     @Param("excluirId") Long excluirId);
}
