package com.clubaereo.vuelos_service.fake;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.clubaereo.vuelos_service.entities.Asignacion;
import com.clubaereo.vuelos_service.entities.Base;
import com.clubaereo.vuelos_service.entities.BloqueHorario;
import com.clubaereo.vuelos_service.entities.EstadoAsignacion;
import com.clubaereo.vuelos_service.entities.Jornada;
import com.clubaereo.vuelos_service.repository.AsignacionRepository;

/**
 * Verifica que el fake imita los @Query reales (COUNT = 0 sin filas, desigualdades estrictas,
 * exclusión de canceladas y de excluirId). OJO: esto comprueba el FAKE, no el JPQL real; el JPQL
 * real solo se prueba con una base de datos (por ejemplo @DataJpaTest).
 */
class FakeAsignacionRepositoryTest {

    private static final EstadoAsignacion CANCELADA = EstadoAsignacion.CANCELADA;
    private static final long SIN_EXCLUIR = -1L;

    private AsignacionRepository repo;
    private FakeAsignacionRepository fake;
    private Jornada jornada1;
    private BloqueHorario bloque9a10;

    @BeforeEach
    void preparar() {
        fake = new FakeAsignacionRepository();
        repo = fake.repo();
        jornada1 = new Jornada(1L, LocalDate.of(2026, 11, 20), Base.TOBALABA, new ArrayList<>());
        bloque9a10 = new BloqueHorario(1L, jornada1, LocalTime.of(9, 0), LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("COUNT sin filas devuelve 0 (no null)")
    void contar_sinFilas_devuelveCero() {
        assertEquals(0L, consultarPasajero(1L, 9, 0, 10, 0));
    }

    @Test
    @DisplayName("contarPasajeroSolapado: cuenta el solape en el mismo horario")
    void contarPasajeroSolapado_mismoHorario_cuenta() {
        guardar(1L, 1L, 1L, bloque9a10, EstadoAsignacion.PENDIENTE);

        assertEquals(1L, consultarPasajero(1L, 9, 0, 10, 0));
    }

    @Test
    @DisplayName("solape: un bloque que termina justo cuando empieza el otro NO se solapa (estricto)")
    void solape_bloquesContiguos_noCuentan() {
        guardar(1L, 1L, 1L, bloque9a10, EstadoAsignacion.PENDIENTE);

        assertEquals(0L, consultarPasajero(1L, 10, 0, 11, 0)); // empieza cuando el otro termina
        assertEquals(0L, consultarPasajero(1L, 8, 0, 9, 0));   // termina cuando el otro empieza
    }

    @Test
    @DisplayName("solape: un minuto de traslape, contenido o contenedor sí cuentan")
    void solape_traslapeContenidoYContenedor_cuentan() {
        guardar(1L, 1L, 1L, bloque9a10, EstadoAsignacion.PENDIENTE);

        assertEquals(1L, consultarPasajero(1L, 9, 59, 11, 0));
        assertEquals(1L, consultarPasajero(1L, 9, 15, 9, 45));
        assertEquals(1L, consultarPasajero(1L, 8, 0, 11, 0));
    }

    @Test
    @DisplayName("contar: ignora las CANCELADAS")
    void contar_ignoraCanceladas() {
        guardar(1L, 1L, 1L, bloque9a10, CANCELADA);

        assertEquals(0L, consultarPasajero(1L, 9, 0, 10, 0));
    }

    @Test
    @DisplayName("contar: excluye la asignación con id = excluirId")
    void contar_excluyeLaAsignacionIndicada() {
        Asignacion a = guardar(1L, 1L, 1L, bloque9a10, EstadoAsignacion.PENDIENTE);

        long conExclusion = repo.contarPasajeroSolapado(1L, 1L, LocalTime.of(9, 0), LocalTime.of(10, 0),
                CANCELADA, a.getId());

        assertEquals(0L, conExclusion);
    }

    @Test
    @DisplayName("contar: solo cuenta la jornada pedida")
    void contar_otraJornada_noCuenta() {
        Jornada jornada2 = new Jornada(2L, LocalDate.of(2026, 11, 21), Base.TALCAHUANO, new ArrayList<>());
        guardar(1L, 1L, 1L, new BloqueHorario(2L, jornada2, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                EstadoAsignacion.PENDIENTE);

        assertEquals(0L, consultarPasajero(1L, 9, 0, 10, 0));
    }

    @Test
    @DisplayName("contarOcupacion: exige MISMA aeronave y MISMO piloto")
    void contarOcupacion_exigeMismaAeronaveYPiloto() {
        guardar(1L, 1L, 1L, bloque9a10, EstadoAsignacion.PENDIENTE); // aeronave 1, piloto 1
        guardar(2L, 1L, 2L, bloque9a10, EstadoAsignacion.PENDIENTE); // aeronave 1, piloto 2
        guardar(3L, 2L, 1L, bloque9a10, EstadoAsignacion.PENDIENTE); // aeronave 2, piloto 1

        long ocupados = repo.contarOcupacion(1L, 1L, 1L, LocalTime.of(9, 0), LocalTime.of(10, 0),
                CANCELADA, SIN_EXCLUIR);

        assertEquals(1L, ocupados);
    }

    @Test
    @DisplayName("contarAeronaveConOtroPiloto: misma aeronave, piloto distinto")
    void contarAeronaveConOtroPiloto_soloPilotoDistinto() {
        guardar(1L, 1L, 1L, bloque9a10, EstadoAsignacion.PENDIENTE); // mismo piloto: no cuenta
        guardar(2L, 1L, 2L, bloque9a10, EstadoAsignacion.PENDIENTE); // otro piloto: cuenta
        guardar(3L, 2L, 3L, bloque9a10, EstadoAsignacion.PENDIENTE); // otra aeronave: no cuenta

        long n = repo.contarAeronaveConOtroPiloto(1L, 1L, 1L, LocalTime.of(9, 0), LocalTime.of(10, 0),
                CANCELADA, SIN_EXCLUIR);

        assertEquals(1L, n);
    }

    @Test
    @DisplayName("contarPilotoConOtraAeronave: mismo piloto, aeronave distinta")
    void contarPilotoConOtraAeronave_soloAeronaveDistinta() {
        guardar(1L, 1L, 1L, bloque9a10, EstadoAsignacion.PENDIENTE); // misma aeronave: no cuenta
        guardar(2L, 2L, 1L, bloque9a10, EstadoAsignacion.PENDIENTE); // otra aeronave: cuenta
        guardar(3L, 3L, 2L, bloque9a10, EstadoAsignacion.PENDIENTE); // otro piloto: no cuenta

        long n = repo.contarPilotoConOtraAeronave(1L, 1L, 1L, LocalTime.of(9, 0), LocalTime.of(10, 0),
                CANCELADA, SIN_EXCLUIR);

        assertEquals(1L, n);
    }

    @Test
    @DisplayName("findByJornadaId: devuelve las asignaciones de la jornada (incluidas las canceladas)")
    void findByJornadaId_devuelveLasDeLaJornadaIncluidasCanceladas() {
        Jornada jornada2 = new Jornada(2L, LocalDate.of(2026, 11, 21), Base.TALCAHUANO, new ArrayList<>());
        guardar(1L, 1L, 1L, bloque9a10, EstadoAsignacion.PENDIENTE);
        guardar(2L, 1L, 1L, bloque9a10, CANCELADA);
        guardar(3L, 1L, 1L, new BloqueHorario(2L, jornada2, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                EstadoAsignacion.PENDIENTE);

        assertEquals(2, repo.findByJornadaId(1L).size());
        assertEquals(1, repo.findByJornadaId(2L).size());
        assertEquals(0, repo.findByJornadaId(99L).size());
    }

    private long consultarPasajero(Long pasajeroId, int hIni, int mIni, int hFin, int mFin) {
        return repo.contarPasajeroSolapado(pasajeroId, 1L, LocalTime.of(hIni, mIni), LocalTime.of(hFin, mFin),
                CANCELADA, SIN_EXCLUIR);
    }

    private Asignacion guardar(Long pasajeroId, Long aeronaveId, Long pilotoId, BloqueHorario bloque,
                               EstadoAsignacion estado) {
        return fake.save(new Asignacion(null, pasajeroId, aeronaveId, pilotoId, bloque, estado));
    }
}
