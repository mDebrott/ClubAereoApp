package com.clubaereo.vuelos_service.fake;

import java.time.LocalTime;
import java.util.List;

import com.clubaereo.vuelos_service.entities.Asignacion;
import com.clubaereo.vuelos_service.entities.EstadoAsignacion;
import com.clubaereo.vuelos_service.repository.AsignacionRepository;

/**
 * Fake de {@link AsignacionRepository}. Cada método reimplementa en Java el @Query real.
 * Los COUNT devuelven 0 cuando no hay filas (como SQL).
 */
public class FakeAsignacionRepository extends InMemoryRepository<Asignacion, AsignacionRepository> {

    public FakeAsignacionRepository() {
        super(AsignacionRepository.class);
    }

    @Override
    protected Long obtenerId(Asignacion a) {
        return a.getId();
    }

    @Override
    protected void asignarId(Asignacion a, Long id) {
        a.setId(id);
    }

    /** select a from Asignacion a join fetch a.bloque b where b.jornada.id = :jornadaId (incluye canceladas). */
    public List<Asignacion> findByJornadaId(Long jornadaId) {
        return datos.values().stream()
                .filter(a -> a.getBloque() != null && a.getBloque().getJornada() != null
                        && sqlIgual(a.getBloque().getJornada().getId(), jornadaId))
                .toList();
    }

    /** Mismo pasajero, no cancelada, distinta de excluirId, misma jornada y bloque solapado. */
    public long contarPasajeroSolapado(Long pasajeroId, Long jornadaId, LocalTime inicio, LocalTime fin,
                                       EstadoAsignacion cancelada, Long excluirId) {
        return datos.values().stream()
                .filter(a -> sqlIgual(a.getPasajeroId(), pasajeroId))
                .filter(a -> activaYDistinta(a, cancelada, excluirId))
                .filter(a -> solapa(a, jornadaId, inicio, fin))
                .count();
    }

    /** Misma aeronave Y mismo piloto (ocupación de cupos). */
    public long contarOcupacion(Long aeronaveId, Long pilotoId, Long jornadaId, LocalTime inicio,
                                LocalTime fin, EstadoAsignacion cancelada, Long excluirId) {
        return datos.values().stream()
                .filter(a -> sqlIgual(a.getAeronaveId(), aeronaveId))
                .filter(a -> sqlIgual(a.getPilotoId(), pilotoId))
                .filter(a -> activaYDistinta(a, cancelada, excluirId))
                .filter(a -> solapa(a, jornadaId, inicio, fin))
                .count();
    }

    /** Misma aeronave, piloto DISTINTO. */
    public long contarAeronaveConOtroPiloto(Long aeronaveId, Long pilotoId, Long jornadaId, LocalTime inicio,
                                            LocalTime fin, EstadoAsignacion cancelada, Long excluirId) {
        return datos.values().stream()
                .filter(a -> sqlIgual(a.getAeronaveId(), aeronaveId))
                .filter(a -> sqlDistinto(a.getPilotoId(), pilotoId))
                .filter(a -> activaYDistinta(a, cancelada, excluirId))
                .filter(a -> solapa(a, jornadaId, inicio, fin))
                .count();
    }

    /** Mismo piloto, aeronave DISTINTA. */
    public long contarPilotoConOtraAeronave(Long pilotoId, Long aeronaveId, Long jornadaId, LocalTime inicio,
                                            LocalTime fin, EstadoAsignacion cancelada, Long excluirId) {
        return datos.values().stream()
                .filter(a -> sqlIgual(a.getPilotoId(), pilotoId))
                .filter(a -> sqlDistinto(a.getAeronaveId(), aeronaveId))
                .filter(a -> activaYDistinta(a, cancelada, excluirId))
                .filter(a -> solapa(a, jornadaId, inicio, fin))
                .count();
    }

    // a.estado <> :cancelada and a.id <> :excluirId
    private static boolean activaYDistinta(Asignacion a, EstadoAsignacion cancelada, Long excluirId) {
        return sqlDistinto(a.getEstado(), cancelada) && sqlDistinto(a.getId(), excluirId);
    }

    // a.bloque.jornada.id = :jornadaId and b.horaInicio < :fin and b.horaFin > :inicio (desigualdades estrictas)
    private static boolean solapa(Asignacion a, Long jornadaId, LocalTime inicio, LocalTime fin) {
        if (a.getBloque() == null || a.getBloque().getJornada() == null) {
            return false;
        }
        return sqlIgual(a.getBloque().getJornada().getId(), jornadaId)
                && a.getBloque().getHoraInicio() != null && inicio != null && fin != null
                && a.getBloque().getHoraFin() != null
                && a.getBloque().getHoraInicio().isBefore(fin)
                && a.getBloque().getHoraFin().isAfter(inicio);
    }
}
