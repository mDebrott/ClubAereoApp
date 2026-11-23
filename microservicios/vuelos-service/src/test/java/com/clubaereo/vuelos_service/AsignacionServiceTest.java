package com.clubaereo.vuelos_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.clubaereo.vuelos_service.client.AeronaveExterna;
import com.clubaereo.vuelos_service.client.PasajeroExterno;
import com.clubaereo.vuelos_service.client.PilotoExterno;
import com.clubaereo.vuelos_service.dto.AsignacionRequest;
import com.clubaereo.vuelos_service.dto.AsignacionResponse;
import com.clubaereo.vuelos_service.entities.Asignacion;
import com.clubaereo.vuelos_service.entities.Base;
import com.clubaereo.vuelos_service.entities.BloqueHorario;
import com.clubaereo.vuelos_service.entities.EstadoAsignacion;
import com.clubaereo.vuelos_service.entities.Jornada;
import com.clubaereo.vuelos_service.exception.ConflictoException;
import com.clubaereo.vuelos_service.exception.RecursoNoEncontradoException;
import com.clubaereo.vuelos_service.exception.ServicioNoDisponibleException;
import com.clubaereo.vuelos_service.fake.FakeAeronaveClient;
import com.clubaereo.vuelos_service.fake.FakeAsignacionRepository;
import com.clubaereo.vuelos_service.fake.FakeBloqueHorarioRepository;
import com.clubaereo.vuelos_service.fake.FakePasajeroClient;
import com.clubaereo.vuelos_service.service.AsignacionService;

/**
 * Pruebas de AsignacionService con fakes en memoria (sin Mockito).
 *
 * Datos base de cada test: jornada 1, bloque 1 (09:00-10:00), pasajero 1 (jornada 1, consentimiento
 * firmado), aeronave 1 "CC-ABC" (capacidad 3, disponible) y piloto 1 "Ana" (licencia vigente).
 */
class AsignacionServiceTest {

    private static final String SOLAPE_PASAJERO = "El pasajero ya tiene una asignación en un bloque que se solapa";
    private static final String AERONAVE_OTRO_PILOTO = "La aeronave ya está asignada a otro piloto en ese horario";
    private static final String PILOTO_OTRA_AERONAVE = "El piloto ya está asignado a otra aeronave en ese horario";
    private static final String OTRA_JORNADA = "El pasajero está inscrito en otra jornada";
    private static final String SOLO_PENDIENTES = "Solo se pueden reasignar asignaciones PENDIENTES";

    private FakeAsignacionRepository asignaciones;
    private FakeBloqueHorarioRepository bloques;
    private FakePasajeroClient pasajeros;
    private FakeAeronaveClient aeronaves;
    private AsignacionService service;

    private Jornada jornada1;
    private Jornada jornada2;
    private BloqueHorario bloque1;

    @BeforeEach
    void preparar() {
        asignaciones = new FakeAsignacionRepository();
        bloques = new FakeBloqueHorarioRepository();
        pasajeros = new FakePasajeroClient();
        aeronaves = new FakeAeronaveClient();
        // Mismo orden que los campos (@RequiredArgsConstructor)
        service = new AsignacionService(asignaciones.repo(), bloques.repo(), pasajeros, aeronaves);

        jornada1 = new Jornada(1L, LocalDate.of(2026, 11, 20), Base.TOBALABA, new ArrayList<>());
        jornada2 = new Jornada(2L, LocalDate.of(2026, 11, 21), Base.TALCAHUANO, new ArrayList<>());
        bloque1 = sembrarBloque(1L, jornada1, 9, 0, 10, 0);

        pasajeros.registrar(pasajero(1L, 1L));
        aeronaves.registrarAeronave(new AeronaveExterna(1L, "CC-ABC", 3, true));
        aeronaves.registrarPiloto(new PilotoExterno(1L, "Ana", true));
    }

    // ------------------------------------------------------------------ crear

    @Test
    @DisplayName("crear: con datos válidos devuelve la asignación PENDIENTE con los datos pedidos")
    void crear_datosValidos_devuelveAsignacionPendiente() {
        // Arrange
        AsignacionRequest req = req(1L, 1L, 1L, 1L);

        // Act
        AsignacionResponse res = service.crear(req);

        // Assert
        assertEquals(1L, res.id());
        assertEquals(1L, res.pasajeroId());
        assertEquals(1L, res.bloqueId());
        assertEquals(1L, res.aeronaveId());
        assertEquals(1L, res.pilotoId());
        assertEquals(EstadoAsignacion.PENDIENTE, res.estado());
    }

    @Test
    @DisplayName("crear: con datos válidos guarda exactamente una asignación en el repositorio")
    void crear_datosValidos_persisteLaAsignacion() {
        // Arrange
        AsignacionRequest req = req(1L, 1L, 1L, 1L);

        // Act
        service.crear(req);

        // Assert
        assertEquals(1, asignaciones.count());
        Asignacion guardada = asignaciones.findById(1L).orElseThrow();
        assertEquals(1L, guardada.getPasajeroId());
        assertEquals(1L, guardada.getAeronaveId());
        assertEquals(1L, guardada.getPilotoId());
        assertSame(bloque1, guardada.getBloque());
        assertEquals(EstadoAsignacion.PENDIENTE, guardada.getEstado());
    }

    @Test
    @DisplayName("crear: el bloque no existe -> RecursoNoEncontrado y no se guarda nada")
    void crear_bloqueNoExiste_lanzaRecursoNoEncontrado() {
        assertNoEncontrado("Bloque 99 no existe", () -> service.crear(req(1L, 99L, 1L, 1L)));
        assertEquals(0, asignaciones.count());
    }

    @Test
    @DisplayName("crear: el pasajero no existe -> propaga RecursoNoEncontrado y no se guarda nada")
    void crear_pasajeroNoExiste_propagaRecursoNoEncontrado() {
        assertNoEncontrado("Pasajero 99 no existe", () -> service.crear(req(99L, 1L, 1L, 1L)));
        assertEquals(0, asignaciones.count());
    }

    @Test
    @DisplayName("crear: la aeronave no existe -> propaga RecursoNoEncontrado y no se guarda nada")
    void crear_aeronaveNoExiste_propagaRecursoNoEncontrado() {
        assertNoEncontrado("Aeronave 99 no existe", () -> service.crear(req(1L, 1L, 99L, 1L)));
        assertEquals(0, asignaciones.count());
    }

    @Test
    @DisplayName("crear: el piloto no existe -> propaga RecursoNoEncontrado y no se guarda nada")
    void crear_pilotoNoExiste_propagaRecursoNoEncontrado() {
        assertNoEncontrado("Piloto 99 no existe", () -> service.crear(req(1L, 1L, 1L, 99L)));
        assertEquals(0, asignaciones.count());
    }

    @Test
    @DisplayName("crear: pasajero-service caído -> propaga ServicioNoDisponible y no se guarda nada")
    void crear_servicioPasajeroCaido_propagaServicioNoDisponible() {
        pasajeros.caer();

        ServicioNoDisponibleException ex = assertThrows(ServicioNoDisponibleException.class,
                () -> service.crear(req(1L, 1L, 1L, 1L)));

        assertEquals("pasajero-service no responde: simulado", ex.getMessage());
        assertEquals(0, asignaciones.count());
    }

    @Test
    @DisplayName("crear: aeronaves-service caído -> propaga ServicioNoDisponible y no se guarda nada")
    void crear_servicioAeronavesCaido_propagaServicioNoDisponible() {
        aeronaves.caer();

        ServicioNoDisponibleException ex = assertThrows(ServicioNoDisponibleException.class,
                () -> service.crear(req(1L, 1L, 1L, 1L)));

        assertEquals("aeronaves-service no responde: simulado", ex.getMessage());
        assertEquals(0, asignaciones.count());
    }

    @Test
    @DisplayName("crear: el pasajero está inscrito en otra jornada -> Conflicto")
    void crear_pasajeroDeOtraJornada_lanzaConflicto() {
        pasajeros.registrar(pasajero(2L, 2L));

        assertConflicto(OTRA_JORNADA, () -> service.crear(req(2L, 1L, 1L, 1L)));

        assertEquals(0, asignaciones.count());
    }

    @Test
    @DisplayName("crear: el pasajero no tiene jornada (null) -> Conflicto")
    void crear_pasajeroSinJornada_lanzaConflicto() {
        pasajeros.registrar(new PasajeroExterno(2L, null, new BigDecimal("70.0"), "FIRMADO"));

        assertConflicto(OTRA_JORNADA, () -> service.crear(req(2L, 1L, 1L, 1L)));

        assertEquals(0, asignaciones.count());
    }

    @Test
    @DisplayName("crear: aeronave no disponible -> Conflicto con la matrícula")
    void crear_aeronaveNoDisponible_lanzaConflicto() {
        aeronaves.registrarAeronave(new AeronaveExterna(1L, "CC-ABC", 3, false));

        assertConflicto("La aeronave CC-ABC no está disponible", () -> service.crear(req(1L, 1L, 1L, 1L)));

        assertEquals(0, asignaciones.count());
    }

    @Test
    @DisplayName("crear: disponibilidad de la aeronave null -> se trata como no disponible")
    void crear_disponibilidadNula_lanzaConflicto() {
        aeronaves.registrarAeronave(new AeronaveExterna(1L, "CC-ABC", 3, null));

        assertConflicto("La aeronave CC-ABC no está disponible", () -> service.crear(req(1L, 1L, 1L, 1L)));

        assertEquals(0, asignaciones.count());
    }

    @Test
    @DisplayName("crear: piloto sin licencia DGAC vigente -> Conflicto con el nombre")
    void crear_pilotoSinLicencia_lanzaConflicto() {
        aeronaves.registrarPiloto(new PilotoExterno(1L, "Ana", false));

        assertConflicto("El piloto Ana no tiene licencia DGAC vigente", () -> service.crear(req(1L, 1L, 1L, 1L)));

        assertEquals(0, asignaciones.count());
    }

    @Test
    @DisplayName("crear: licencia del piloto null -> se trata como no vigente")
    void crear_licenciaNula_lanzaConflicto() {
        aeronaves.registrarPiloto(new PilotoExterno(1L, "Ana", null));

        assertConflicto("El piloto Ana no tiene licencia DGAC vigente", () -> service.crear(req(1L, 1L, 1L, 1L)));

        assertEquals(0, asignaciones.count());
    }

    @Test
    @DisplayName("crear: el pasajero ya está en un bloque solapado -> Conflicto")
    void crear_pasajeroConBloqueSolapado_lanzaConflicto() {
        BloqueHorario solapado = sembrarBloque(2L, jornada1, 9, 30, 10, 30);
        sembrar(1L, 2L, 2L, solapado, EstadoAsignacion.PENDIENTE);

        assertConflicto(SOLAPE_PASAJERO, () -> service.crear(req(1L, 1L, 1L, 1L)));

        assertEquals(1, asignaciones.count());
    }

    @Test
    @DisplayName("crear: el pasajero tiene un bloque contiguo (termina justo cuando empieza el nuevo) -> se crea")
    void crear_pasajeroConBloqueContiguo_creaAsignacion() {
        BloqueHorario anterior = sembrarBloque(2L, jornada1, 8, 0, 9, 0);
        sembrar(1L, 1L, 1L, anterior, EstadoAsignacion.PENDIENTE);

        AsignacionResponse res = service.crear(req(1L, 1L, 1L, 1L));

        assertEquals(EstadoAsignacion.PENDIENTE, res.estado());
        assertEquals(2, asignaciones.count());
    }

    @Test
    @DisplayName("crear: una asignación CANCELADA solapada del pasajero no bloquea la nueva")
    void crear_pasajeroConAsignacionCanceladaSolapada_creaAsignacion() {
        sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.CANCELADA);

        AsignacionResponse res = service.crear(req(1L, 1L, 1L, 1L));

        assertEquals(EstadoAsignacion.PENDIENTE, res.estado());
        assertEquals(2, asignaciones.count());
    }

    @Test
    @DisplayName("crear: la aeronave ya está con otro piloto en ese horario -> Conflicto")
    void crear_aeronaveConOtroPiloto_lanzaConflicto() {
        sembrar(10L, 1L, 2L, bloque1, EstadoAsignacion.PENDIENTE);

        assertConflicto(AERONAVE_OTRO_PILOTO, () -> service.crear(req(1L, 1L, 1L, 1L)));

        assertEquals(1, asignaciones.count());
    }

    @Test
    @DisplayName("crear: el piloto ya vuela otra aeronave en ese horario -> Conflicto")
    void crear_pilotoConOtraAeronave_lanzaConflicto() {
        sembrar(10L, 2L, 1L, bloque1, EstadoAsignacion.PENDIENTE);

        assertConflicto(PILOTO_OTRA_AERONAVE, () -> service.crear(req(1L, 1L, 1L, 1L)));

        assertEquals(1, asignaciones.count());
    }

    @Test
    @DisplayName("crear: ocupación igual a la capacidad -> Conflicto sin cupos")
    void crear_ocupacionIgualACapacidad_lanzaConflicto() {
        sembrarOcupantes(3);

        assertConflicto("La aeronave CC-ABC no tiene cupos en ese horario (capacidad 3)",
                () -> service.crear(req(1L, 1L, 1L, 1L)));

        assertEquals(3, asignaciones.count());
    }

    @Test
    @DisplayName("crear: ocupación = capacidad - 1 (queda un cupo) -> se crea")
    void crear_ocupacionUnaMenosQueCapacidad_creaAsignacion() {
        sembrarOcupantes(2);

        AsignacionResponse res = service.crear(req(1L, 1L, 1L, 1L));

        assertEquals(EstadoAsignacion.PENDIENTE, res.estado());
        assertEquals(3, asignaciones.count());
    }

    @Test
    @DisplayName("crear: los ocupantes CANCELADOS no consumen cupo")
    void crear_ocupantesCancelados_noConsumenCupo() {
        aeronaves.registrarAeronave(new AeronaveExterna(1L, "CC-ABC", 1, true));
        sembrar(10L, 1L, 1L, bloque1, EstadoAsignacion.CANCELADA);

        AsignacionResponse res = service.crear(req(1L, 1L, 1L, 1L));

        assertEquals(EstadoAsignacion.PENDIENTE, res.estado());
    }

    @Test
    @DisplayName("crear: la ocupación de un bloque parcialmente solapado también cuenta para el cupo")
    void crear_ocupantesEnBloqueSolapado_cuentanParaElCupo() {
        aeronaves.registrarAeronave(new AeronaveExterna(1L, "CC-ABC", 1, true));
        BloqueHorario solapado = sembrarBloque(2L, jornada1, 9, 30, 10, 30);
        sembrar(10L, 1L, 1L, solapado, EstadoAsignacion.PENDIENTE);

        assertConflicto("La aeronave CC-ABC no tiene cupos en ese horario (capacidad 1)",
                () -> service.crear(req(1L, 1L, 1L, 1L)));
    }

    @Test
    @DisplayName("crear: la ocupación de la misma aeronave en OTRA jornada no cuenta")
    void crear_ocupacionEnOtraJornada_noCuenta() {
        aeronaves.registrarAeronave(new AeronaveExterna(1L, "CC-ABC", 1, true));
        BloqueHorario deOtraJornada = sembrarBloque(2L, jornada2, 9, 0, 10, 0);
        sembrar(10L, 1L, 1L, deOtraJornada, EstadoAsignacion.PENDIENTE);

        AsignacionResponse res = service.crear(req(1L, 1L, 1L, 1L));

        assertEquals(EstadoAsignacion.PENDIENTE, res.estado());
    }

    @Test
    @DisplayName("crear: aeronave con capacidad 0 -> Conflicto aunque no haya ocupantes")
    void crear_capacidadCero_lanzaConflicto() {
        aeronaves.registrarAeronave(new AeronaveExterna(1L, "CC-ABC", 0, true));

        assertConflicto("La aeronave CC-ABC no tiene cupos en ese horario (capacidad 0)",
                () -> service.crear(req(1L, 1L, 1L, 1L)));

        assertEquals(0, asignaciones.count());
    }

    @Test
    @DisplayName("crear: capacidad null -> hoy lanza NullPointerException (posible bug, ver informe)")
    void crear_capacidadNula_lanzaNullPointer_posibleBug() {
        aeronaves.registrarAeronave(new AeronaveExterna(1L, "CC-ABC", null, true));

        assertThrows(NullPointerException.class, () -> service.crear(req(1L, 1L, 1L, 1L)));

        assertEquals(0, asignaciones.count());
    }

    @Test
    @DisplayName("crear: si fallan varias reglas, gana la del pasajero en otra jornada")
    void crear_variasReglasVioladas_priorizaOtraJornada() {
        pasajeros.registrar(pasajero(2L, 2L));
        aeronaves.registrarAeronave(new AeronaveExterna(1L, "CC-ABC", 3, false));
        aeronaves.registrarPiloto(new PilotoExterno(1L, "Ana", false));

        assertConflicto(OTRA_JORNADA, () -> service.crear(req(2L, 1L, 1L, 1L)));
    }

    @Test
    @DisplayName("crear: si fallan aeronave y piloto, gana la aeronave no disponible")
    void crear_aeronaveYPilotoInvalidos_priorizaAeronave() {
        aeronaves.registrarAeronave(new AeronaveExterna(1L, "CC-ABC", 3, false));
        aeronaves.registrarPiloto(new PilotoExterno(1L, "Ana", false));

        assertConflicto("La aeronave CC-ABC no está disponible", () -> service.crear(req(1L, 1L, 1L, 1L)));
    }

    @Test
    @DisplayName("crear: si el pasajero se solapa y además la aeronave tiene otro piloto, gana el solape del pasajero")
    void crear_solapePasajeroYAeronaveOtroPiloto_priorizaPasajero() {
        sembrar(1L, 2L, 2L, bloque1, EstadoAsignacion.PENDIENTE); // pasajero 1 ocupado
        sembrar(10L, 1L, 2L, bloque1, EstadoAsignacion.PENDIENTE); // aeronave 1 con piloto 2

        assertConflicto(SOLAPE_PASAJERO, () -> service.crear(req(1L, 1L, 1L, 1L)));
    }

    @Test
    @DisplayName("crear: al llenar la aeronave, la siguiente asignación falla y las anteriores quedan intactas")
    void crear_variasAsignacionesHastaLlenarAeronave_laSiguienteFalla() {
        aeronaves.registrarAeronave(new AeronaveExterna(1L, "CC-ABC", 2, true));
        pasajeros.registrar(pasajero(2L, 1L));
        pasajeros.registrar(pasajero(3L, 1L));

        AsignacionResponse primera = service.crear(req(1L, 1L, 1L, 1L));
        AsignacionResponse segunda = service.crear(req(2L, 1L, 1L, 1L));
        assertConflicto("La aeronave CC-ABC no tiene cupos en ese horario (capacidad 2)",
                () -> service.crear(req(3L, 1L, 1L, 1L)));

        assertEquals(1L, primera.id());
        assertEquals(2L, segunda.id());
        assertEquals(2, asignaciones.count());
    }

    // ------------------------------------------------------------------ actualizar

    @Test
    @DisplayName("actualizar: cambia aeronave y piloto de una asignación PENDIENTE")
    void actualizar_cambiaAeronaveYPiloto_actualizaAsignacion() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        registrarAeronave2YPiloto2();

        AsignacionResponse res = service.actualizar(a.getId(), req(1L, 1L, 2L, 2L));

        assertEquals(2L, res.aeronaveId());
        assertEquals(2L, res.pilotoId());
        assertEquals(EstadoAsignacion.PENDIENTE, res.estado());
        assertAsignacion(a.getId(), 1L, 1L, 2L, 2L, EstadoAsignacion.PENDIENTE);
    }

    @Test
    @DisplayName("actualizar: cambia de bloque dentro de la misma jornada")
    void actualizar_cambiaDeBloque_actualizaBloque() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        BloqueHorario bloque2 = sembrarBloque(2L, jornada1, 10, 0, 11, 0);

        AsignacionResponse res = service.actualizar(a.getId(), req(1L, 2L, 1L, 1L));

        assertEquals(2L, res.bloqueId());
        assertSame(bloque2, asignaciones.findById(a.getId()).orElseThrow().getBloque());
    }

    @Test
    @DisplayName("actualizar: cambia de pasajero")
    void actualizar_cambiaDePasajero_actualizaAsignacion() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        pasajeros.registrar(pasajero(2L, 1L));

        AsignacionResponse res = service.actualizar(a.getId(), req(2L, 1L, 1L, 1L));

        assertEquals(2L, res.pasajeroId());
        assertAsignacion(a.getId(), 2L, 1L, 1L, 1L, EstadoAsignacion.PENDIENTE);
    }

    @Test
    @DisplayName("actualizar: reenviar los mismos datos no choca consigo misma (aunque la aeronave tenga 1 cupo)")
    void actualizar_mismosDatos_noChocaConsigoMisma() {
        aeronaves.registrarAeronave(new AeronaveExterna(1L, "CC-ABC", 1, true));
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);

        AsignacionResponse res = service.actualizar(a.getId(), req(1L, 1L, 1L, 1L));

        assertEquals(EstadoAsignacion.PENDIENTE, res.estado());
        assertEquals(1, asignaciones.count());
    }

    @Test
    @DisplayName("actualizar: la asignación no existe -> RecursoNoEncontrado")
    void actualizar_asignacionNoExiste_lanzaRecursoNoEncontrado() {
        assertNoEncontrado("Asignación 99 no existe", () -> service.actualizar(99L, req(1L, 1L, 1L, 1L)));
    }

    @Test
    @DisplayName("actualizar: asignación CONFIRMADA -> Conflicto y no cambia nada")
    void actualizar_asignacionConfirmada_lanzaConflicto() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.CONFIRMADA);
        registrarAeronave2YPiloto2();

        assertConflicto(SOLO_PENDIENTES, () -> service.actualizar(a.getId(), req(1L, 1L, 2L, 2L)));

        assertAsignacion(a.getId(), 1L, 1L, 1L, 1L, EstadoAsignacion.CONFIRMADA);
    }

    @Test
    @DisplayName("actualizar: asignación CANCELADA -> Conflicto y no cambia nada")
    void actualizar_asignacionCancelada_lanzaConflicto() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.CANCELADA);
        registrarAeronave2YPiloto2();

        assertConflicto(SOLO_PENDIENTES, () -> service.actualizar(a.getId(), req(1L, 1L, 2L, 2L)));

        assertAsignacion(a.getId(), 1L, 1L, 1L, 1L, EstadoAsignacion.CANCELADA);
    }

    @Test
    @DisplayName("actualizar: el bloque nuevo no existe -> RecursoNoEncontrado y no cambia nada")
    void actualizar_bloqueNuevoNoExiste_lanzaRecursoNoEncontrado() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);

        assertNoEncontrado("Bloque 99 no existe", () -> service.actualizar(a.getId(), req(1L, 99L, 1L, 1L)));

        assertAsignacion(a.getId(), 1L, 1L, 1L, 1L, EstadoAsignacion.PENDIENTE);
    }

    @Test
    @DisplayName("actualizar: el pasajero nuevo es de otra jornada -> Conflicto y no cambia nada")
    void actualizar_pasajeroNuevoDeOtraJornada_lanzaConflicto() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        pasajeros.registrar(pasajero(3L, 2L));

        assertConflicto(OTRA_JORNADA, () -> service.actualizar(a.getId(), req(3L, 1L, 1L, 1L)));

        assertAsignacion(a.getId(), 1L, 1L, 1L, 1L, EstadoAsignacion.PENDIENTE);
    }

    @Test
    @DisplayName("actualizar: mover a un bloque de otra jornada -> Conflicto y no cambia nada")
    void actualizar_bloqueDeOtraJornada_lanzaConflicto() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        sembrarBloque(3L, jornada2, 9, 0, 10, 0);

        assertConflicto(OTRA_JORNADA, () -> service.actualizar(a.getId(), req(1L, 3L, 1L, 1L)));

        assertAsignacion(a.getId(), 1L, 1L, 1L, 1L, EstadoAsignacion.PENDIENTE);
    }

    @Test
    @DisplayName("actualizar: aeronave nueva no disponible -> Conflicto y no cambia nada")
    void actualizar_aeronaveNuevaNoDisponible_lanzaConflicto() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        aeronaves.registrarAeronave(new AeronaveExterna(2L, "CC-XYZ", 2, false));

        assertConflicto("La aeronave CC-XYZ no está disponible", () -> service.actualizar(a.getId(), req(1L, 1L, 2L, 1L)));

        assertAsignacion(a.getId(), 1L, 1L, 1L, 1L, EstadoAsignacion.PENDIENTE);
    }

    @Test
    @DisplayName("actualizar: piloto nuevo sin licencia -> Conflicto y no cambia nada")
    void actualizar_pilotoNuevoSinLicencia_lanzaConflicto() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        aeronaves.registrarPiloto(new PilotoExterno(2L, "Luis", false));

        assertConflicto("El piloto Luis no tiene licencia DGAC vigente", () -> service.actualizar(a.getId(), req(1L, 1L, 1L, 2L)));

        assertAsignacion(a.getId(), 1L, 1L, 1L, 1L, EstadoAsignacion.PENDIENTE);
    }

    @Test
    @DisplayName("actualizar: el nuevo bloque se solapa con otra asignación del pasajero -> Conflicto y no cambia nada")
    void actualizar_nuevoBloqueSolapadoConOtraAsignacionDelPasajero_lanzaConflicto() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        BloqueHorario bloque2 = sembrarBloque(2L, jornada1, 10, 0, 11, 0);
        sembrar(1L, 2L, 2L, bloque2, EstadoAsignacion.PENDIENTE);

        assertConflicto(SOLAPE_PASAJERO, () -> service.actualizar(a.getId(), req(1L, 2L, 1L, 1L)));

        assertAsignacion(a.getId(), 1L, 1L, 1L, 1L, EstadoAsignacion.PENDIENTE);
    }

    @Test
    @DisplayName("actualizar: la aeronave no tiene cupos en el nuevo horario -> Conflicto y no cambia nada")
    void actualizar_aeronaveLlenaEnNuevoHorario_lanzaConflicto() {
        aeronaves.registrarAeronave(new AeronaveExterna(1L, "CC-ABC", 1, true));
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        BloqueHorario bloque2 = sembrarBloque(2L, jornada1, 10, 0, 11, 0);
        sembrar(10L, 1L, 1L, bloque2, EstadoAsignacion.PENDIENTE);

        assertConflicto("La aeronave CC-ABC no tiene cupos en ese horario (capacidad 1)",
                () -> service.actualizar(a.getId(), req(1L, 2L, 1L, 1L)));

        assertAsignacion(a.getId(), 1L, 1L, 1L, 1L, EstadoAsignacion.PENDIENTE);
    }

    @Test
    @DisplayName("actualizar: la aeronave tiene otro piloto en el nuevo horario -> Conflicto y no cambia nada")
    void actualizar_aeronaveConOtroPilotoEnNuevoHorario_lanzaConflicto() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        BloqueHorario bloque2 = sembrarBloque(2L, jornada1, 10, 0, 11, 0);
        sembrar(10L, 1L, 2L, bloque2, EstadoAsignacion.PENDIENTE);

        assertConflicto(AERONAVE_OTRO_PILOTO, () -> service.actualizar(a.getId(), req(1L, 2L, 1L, 1L)));

        assertAsignacion(a.getId(), 1L, 1L, 1L, 1L, EstadoAsignacion.PENDIENTE);
    }

    @Test
    @DisplayName("actualizar: el piloto vuela otra aeronave en el nuevo horario -> Conflicto y no cambia nada")
    void actualizar_pilotoConOtraAeronaveEnNuevoHorario_lanzaConflicto() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        BloqueHorario bloque2 = sembrarBloque(2L, jornada1, 10, 0, 11, 0);
        sembrar(10L, 2L, 1L, bloque2, EstadoAsignacion.PENDIENTE);

        assertConflicto(PILOTO_OTRA_AERONAVE, () -> service.actualizar(a.getId(), req(1L, 2L, 1L, 1L)));

        assertAsignacion(a.getId(), 1L, 1L, 1L, 1L, EstadoAsignacion.PENDIENTE);
    }

    // ------------------------------------------------------------------ confirmar

    @Test
    @DisplayName("confirmar: pasajero con consentimiento firmado y peso -> queda CONFIRMADA")
    void confirmar_pasajeroConConsentimientoYPeso_confirma() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);

        AsignacionResponse res = service.confirmar(a.getId());

        assertEquals(EstadoAsignacion.CONFIRMADA, res.estado());
        assertEquals(EstadoAsignacion.CONFIRMADA, asignaciones.findById(a.getId()).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("confirmar: la asignación no existe -> RecursoNoEncontrado")
    void confirmar_asignacionNoExiste_lanzaRecursoNoEncontrado() {
        assertNoEncontrado("Asignación 99 no existe", () -> service.confirmar(99L));
    }

    @Test
    @DisplayName("confirmar: asignación CANCELADA -> Conflicto y sigue CANCELADA")
    void confirmar_asignacionCancelada_lanzaConflicto() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.CANCELADA);

        assertConflicto("No se puede confirmar una asignación cancelada", () -> service.confirmar(a.getId()));

        assertEquals(EstadoAsignacion.CANCELADA, asignaciones.findById(a.getId()).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("confirmar: ya CONFIRMADA es idempotente (ni siquiera consulta a pasajero-service)")
    void confirmar_yaConfirmada_esIdempotente() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.CONFIRMADA);
        pasajeros.caer(); // si el service lo consultara, fallaría con ServicioNoDisponible

        AsignacionResponse res = service.confirmar(a.getId());

        assertEquals(EstadoAsignacion.CONFIRMADA, res.estado());
    }

    @Test
    @DisplayName("confirmar: consentimiento PENDIENTE -> Conflicto y sigue PENDIENTE")
    void confirmar_consentimientoPendiente_lanzaConflicto() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        pasajeros.registrar(new PasajeroExterno(1L, 1L, new BigDecimal("70.0"), "PENDIENTE"));

        assertConflicto("El pasajero debe firmar el consentimiento antes de confirmar el vuelo",
                () -> service.confirmar(a.getId()));

        assertEquals(EstadoAsignacion.PENDIENTE, asignaciones.findById(a.getId()).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("confirmar: consentimiento null -> Conflicto")
    void confirmar_consentimientoNulo_lanzaConflicto() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        pasajeros.registrar(new PasajeroExterno(1L, 1L, new BigDecimal("70.0"), null));

        assertConflicto("El pasajero debe firmar el consentimiento antes de confirmar el vuelo",
                () -> service.confirmar(a.getId()));

        assertEquals(EstadoAsignacion.PENDIENTE, asignaciones.findById(a.getId()).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("confirmar: 'firmado' en minúsculas no vale (la comparación distingue mayúsculas) -> Conflicto")
    void confirmar_consentimientoEnMinusculas_lanzaConflicto() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        pasajeros.registrar(new PasajeroExterno(1L, 1L, new BigDecimal("70.0"), "firmado"));

        assertConflicto("El pasajero debe firmar el consentimiento antes de confirmar el vuelo",
                () -> service.confirmar(a.getId()));
    }

    @Test
    @DisplayName("confirmar: pasajero sin peso registrado -> Conflicto y sigue PENDIENTE")
    void confirmar_pesoNulo_lanzaConflicto() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        pasajeros.registrar(new PasajeroExterno(1L, 1L, null, "FIRMADO"));

        assertConflicto("El pasajero no tiene peso registrado", () -> service.confirmar(a.getId()));

        assertEquals(EstadoAsignacion.PENDIENTE, asignaciones.findById(a.getId()).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("confirmar: sin consentimiento y sin peso, se reporta primero el consentimiento")
    void confirmar_sinConsentimientoYSinPeso_priorizaConsentimiento() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        pasajeros.registrar(new PasajeroExterno(1L, 1L, null, "PENDIENTE"));

        assertConflicto("El pasajero debe firmar el consentimiento antes de confirmar el vuelo",
                () -> service.confirmar(a.getId()));
    }

    @Test
    @DisplayName("confirmar: peso 0 kg -> hoy se confirma (posible bug, ver informe)")
    void confirmar_pesoCero_confirma_posibleBug() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        pasajeros.registrar(new PasajeroExterno(1L, 1L, BigDecimal.ZERO, "FIRMADO"));

        AsignacionResponse res = service.confirmar(a.getId());

        assertEquals(EstadoAsignacion.CONFIRMADA, res.estado());
    }

    @Test
    @DisplayName("confirmar: el pasajero ya no existe en pasajero-service -> propaga RecursoNoEncontrado y sigue PENDIENTE")
    void confirmar_pasajeroNoExiste_propagaRecursoNoEncontrado() {
        Asignacion a = sembrar(77L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);

        assertNoEncontrado("Pasajero 77 no existe", () -> service.confirmar(a.getId()));

        assertEquals(EstadoAsignacion.PENDIENTE, asignaciones.findById(a.getId()).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("confirmar: pasajero-service caído -> propaga ServicioNoDisponible y sigue PENDIENTE")
    void confirmar_servicioPasajeroCaido_propagaServicioNoDisponible() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        pasajeros.caer();

        assertThrows(ServicioNoDisponibleException.class, () -> service.confirmar(a.getId()));

        assertEquals(EstadoAsignacion.PENDIENTE, asignaciones.findById(a.getId()).orElseThrow().getEstado());
    }

    // ------------------------------------------------------------------ cancelar

    @Test
    @DisplayName("cancelar: una asignación PENDIENTE queda CANCELADA")
    void cancelar_asignacionPendiente_quedaCancelada() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);

        AsignacionResponse res = service.cancelar(a.getId());

        assertEquals(EstadoAsignacion.CANCELADA, res.estado());
        assertEquals(EstadoAsignacion.CANCELADA, asignaciones.findById(a.getId()).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("cancelar: una asignación CONFIRMADA también se puede cancelar")
    void cancelar_asignacionConfirmada_quedaCancelada() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.CONFIRMADA);

        AsignacionResponse res = service.cancelar(a.getId());

        assertEquals(EstadoAsignacion.CANCELADA, res.estado());
    }

    @Test
    @DisplayName("cancelar: cancelar una ya CANCELADA es idempotente")
    void cancelar_yaCancelada_esIdempotente() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.CANCELADA);

        AsignacionResponse res = service.cancelar(a.getId());

        assertEquals(EstadoAsignacion.CANCELADA, res.estado());
    }

    @Test
    @DisplayName("cancelar: la asignación no existe -> RecursoNoEncontrado")
    void cancelar_asignacionNoExiste_lanzaRecursoNoEncontrado() {
        assertNoEncontrado("Asignación 99 no existe", () -> service.cancelar(99L));
    }

    @Test
    @DisplayName("cancelar: no borra la asignación, solo cambia su estado")
    void cancelar_asignacion_noLaElimina() {
        Asignacion a = sembrar(1L, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);

        service.cancelar(a.getId());

        assertEquals(1, asignaciones.count());
    }

    @Test
    @DisplayName("cancelar: libera el cupo de una aeronave llena y permite una nueva asignación")
    void cancelar_asignacionDeAeronaveLlena_liberaCupo() {
        aeronaves.registrarAeronave(new AeronaveExterna(1L, "CC-ABC", 1, true));
        pasajeros.registrar(pasajero(2L, 1L));
        AsignacionResponse primera = service.crear(req(1L, 1L, 1L, 1L));
        assertConflicto("La aeronave CC-ABC no tiene cupos en ese horario (capacidad 1)",
                () -> service.crear(req(2L, 1L, 1L, 1L)));

        service.cancelar(primera.id());
        AsignacionResponse segunda = service.crear(req(2L, 1L, 1L, 1L));

        assertEquals(EstadoAsignacion.PENDIENTE, segunda.estado());
        assertEquals(2, asignaciones.count());
        assertEquals(EstadoAsignacion.CANCELADA, asignaciones.findById(primera.id()).orElseThrow().getEstado());
    }

    // ------------------------------------------------------------------ ayudas

    private static AsignacionRequest req(long pasajeroId, long bloqueId, long aeronaveId, long pilotoId) {
        return new AsignacionRequest(pasajeroId, bloqueId, aeronaveId, pilotoId);
    }

    private static PasajeroExterno pasajero(long id, long jornadaId) {
        return new PasajeroExterno(id, jornadaId, new BigDecimal("70.0"), "FIRMADO");
    }

    private BloqueHorario sembrarBloque(long id, Jornada jornada, int hIni, int mIni, int hFin, int mFin) {
        return bloques.save(new BloqueHorario(id, jornada, LocalTime.of(hIni, mIni), LocalTime.of(hFin, mFin)));
    }

    private Asignacion sembrar(Long pasajeroId, Long aeronaveId, Long pilotoId, BloqueHorario bloque,
                               EstadoAsignacion estado) {
        return asignaciones.save(new Asignacion(null, pasajeroId, aeronaveId, pilotoId, bloque, estado));
    }

    /** Siembra N asignaciones activas de la aeronave 1 con el piloto 1 en el bloque 1 (pasajeros 10, 11...). */
    private void sembrarOcupantes(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            sembrar(10L + i, 1L, 1L, bloque1, EstadoAsignacion.PENDIENTE);
        }
    }

    private void registrarAeronave2YPiloto2() {
        aeronaves.registrarAeronave(new AeronaveExterna(2L, "CC-XYZ", 2, true));
        aeronaves.registrarPiloto(new PilotoExterno(2L, "Luis", true));
    }

    /** Relee la asignación desde el fake y comprueba todos sus campos. */
    private void assertAsignacion(Long id, long pasajeroId, long bloqueId, long aeronaveId, long pilotoId,
                                  EstadoAsignacion estado) {
        Asignacion a = asignaciones.findById(id).orElseThrow();
        assertEquals(pasajeroId, a.getPasajeroId());
        assertEquals(bloqueId, a.getBloque().getId());
        assertEquals(aeronaveId, a.getAeronaveId());
        assertEquals(pilotoId, a.getPilotoId());
        assertEquals(estado, a.getEstado());
    }

    private static void assertConflicto(String mensajeEsperado, Executable accion) {
        ConflictoException ex = assertThrows(ConflictoException.class, accion);
        assertEquals(mensajeEsperado, ex.getMessage());
    }

    private static void assertNoEncontrado(String mensajeEsperado, Executable accion) {
        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class, accion);
        assertEquals(mensajeEsperado, ex.getMessage());
    }
}
