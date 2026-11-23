package com.clubaereo.vuelos_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.clubaereo.vuelos_service.dto.BloqueRequest;
import com.clubaereo.vuelos_service.dto.BloqueResponse;
import com.clubaereo.vuelos_service.dto.JornadaRequest;
import com.clubaereo.vuelos_service.dto.JornadaResponse;
import com.clubaereo.vuelos_service.entities.Asignacion;
import com.clubaereo.vuelos_service.entities.Base;
import com.clubaereo.vuelos_service.entities.BloqueHorario;
import com.clubaereo.vuelos_service.entities.EstadoAsignacion;
import com.clubaereo.vuelos_service.entities.Jornada;
import com.clubaereo.vuelos_service.exception.ConflictoException;
import com.clubaereo.vuelos_service.exception.RecursoNoEncontradoException;
import com.clubaereo.vuelos_service.fake.FakeAsignacionRepository;
import com.clubaereo.vuelos_service.fake.FakeBloqueHorarioRepository;
import com.clubaereo.vuelos_service.fake.FakeJornadaRepository;
import com.clubaereo.vuelos_service.service.JornadaService;

/** Pruebas de JornadaService con fakes en memoria (sin Mockito). */
class JornadaServiceTest {

    private static final String SOLAPE = "El bloque se solapa con otro bloque de la jornada";
    private static final String HORAS_INVALIDAS = "La hora de fin debe ser posterior a la hora de inicio";
    private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);

    private FakeJornadaRepository jornadas;
    private FakeBloqueHorarioRepository bloques;
    private FakeAsignacionRepository asignaciones;
    private JornadaService service;

    @BeforeEach
    void preparar() {
        jornadas = new FakeJornadaRepository();
        bloques = new FakeBloqueHorarioRepository();
        asignaciones = new FakeAsignacionRepository();
        // Mismo orden que los campos (@RequiredArgsConstructor)
        service = new JornadaService(jornadas.repo(), bloques.repo(), asignaciones.repo());
    }

    // ------------------------------------------------------------------ crearJornada

    @Test
    @DisplayName("crearJornada: con fecha y base nuevas devuelve la jornada creada")
    void crearJornada_datosNuevos_devuelveJornada() {
        // Arrange
        JornadaRequest req = new JornadaRequest(FECHA, Base.TOBALABA);

        // Act
        JornadaResponse res = service.crearJornada(req);

        // Assert
        assertEquals(1L, res.id());
        assertEquals(FECHA, res.fecha());
        assertEquals(Base.TOBALABA, res.base());
    }

    @Test
    @DisplayName("crearJornada: guarda la jornada sin bloques")
    void crearJornada_datosNuevos_persisteJornadaSinBloques() {
        service.crearJornada(new JornadaRequest(FECHA, Base.TOBALABA));

        assertEquals(1, jornadas.count());
        Jornada guardada = jornadas.findById(1L).orElseThrow();
        assertEquals(FECHA, guardada.getFecha());
        assertEquals(Base.TOBALABA, guardada.getBase());
        assertTrue(guardada.getBloques().isEmpty());
    }

    @Test
    @DisplayName("crearJornada: misma fecha y misma base -> Conflicto y no se guarda otra")
    void crearJornada_fechaYBaseRepetidas_lanzaConflicto() {
        sembrarJornada(FECHA, Base.TOBALABA);

        assertConflicto("Ya existe una jornada para esa fecha y base",
                () -> service.crearJornada(new JornadaRequest(FECHA, Base.TOBALABA)));

        assertEquals(1, jornadas.count());
    }

    @Test
    @DisplayName("crearJornada: misma fecha pero otra base -> se crea")
    void crearJornada_mismaFechaOtraBase_creaJornada() {
        sembrarJornada(FECHA, Base.TOBALABA);

        JornadaResponse res = service.crearJornada(new JornadaRequest(FECHA, Base.TALCAHUANO));

        assertEquals(Base.TALCAHUANO, res.base());
        assertEquals(2, jornadas.count());
    }

    @Test
    @DisplayName("crearJornada: misma base pero otra fecha -> se crea")
    void crearJornada_mismaBaseOtraFecha_creaJornada() {
        sembrarJornada(FECHA, Base.TOBALABA);

        JornadaResponse res = service.crearJornada(new JornadaRequest(FECHA.plusDays(1), Base.TOBALABA));

        assertEquals(FECHA.plusDays(1), res.fecha());
        assertEquals(2, jornadas.count());
    }

    // ------------------------------------------------------------------ crearBloque

    @Test
    @DisplayName("crearBloque: con horas válidas devuelve el bloque sin asignaciones")
    void crearBloque_datosValidos_devuelveBloqueSinAsignaciones() {
        Jornada j = sembrarJornada(FECHA, Base.TOBALABA);

        BloqueResponse res = service.crearBloque(j.getId(), horas(9, 0, 10, 0));

        assertEquals(1L, res.id());
        assertEquals(LocalTime.of(9, 0), res.horaInicio());
        assertEquals(LocalTime.of(10, 0), res.horaFin());
        assertEquals(List.of(), res.asignaciones());
    }

    @Test
    @DisplayName("crearBloque: guarda el bloque asociado a la jornada")
    void crearBloque_datosValidos_persisteBloqueEnLaJornada() {
        Jornada j = sembrarJornada(FECHA, Base.TOBALABA);

        service.crearBloque(j.getId(), horas(9, 0, 10, 0));

        assertEquals(1, bloques.count());
        BloqueHorario guardado = bloques.findById(1L).orElseThrow();
        assertSame(j, guardado.getJornada());
        assertEquals(LocalTime.of(9, 0), guardado.getHoraInicio());
        assertEquals(LocalTime.of(10, 0), guardado.getHoraFin());
    }

    @Test
    @DisplayName("crearBloque: la jornada no existe -> RecursoNoEncontrado y no se guarda nada")
    void crearBloque_jornadaNoExiste_lanzaRecursoNoEncontrado() {
        assertNoEncontrado("Jornada 99 no existe", () -> service.crearBloque(99L, horas(9, 0, 10, 0)));

        assertEquals(0, bloques.count());
    }

    @Test
    @DisplayName("crearBloque: jornada inexistente y horas inválidas -> gana jornada no encontrada")
    void crearBloque_jornadaNoExisteYHorasInvalidas_priorizaJornada() {
        assertNoEncontrado("Jornada 99 no existe", () -> service.crearBloque(99L, horas(10, 0, 9, 0)));
    }

    @Test
    @DisplayName("crearBloque: hora de fin anterior a la de inicio -> Conflicto")
    void crearBloque_finAnteriorAInicio_lanzaConflicto() {
        Jornada j = sembrarJornada(FECHA, Base.TOBALABA);

        assertConflicto(HORAS_INVALIDAS, () -> service.crearBloque(j.getId(), horas(10, 0, 9, 0)));

        assertEquals(0, bloques.count());
    }

    @Test
    @DisplayName("crearBloque: hora de fin igual a la de inicio (bloque de duración cero) -> Conflicto")
    void crearBloque_finIgualAInicio_lanzaConflicto() {
        Jornada j = sembrarJornada(FECHA, Base.TOBALABA);

        assertConflicto(HORAS_INVALIDAS, () -> service.crearBloque(j.getId(), horas(9, 0, 9, 0)));

        assertEquals(0, bloques.count());
    }

    @Test
    @DisplayName("crearBloque: un bloque que termina a las 00:00 se rechaza (limitación actual, ver informe)")
    void crearBloque_terminaEnMedianoche_lanzaConflicto_limitacionActual() {
        Jornada j = sembrarJornada(FECHA, Base.TOBALABA);

        assertConflicto(HORAS_INVALIDAS, () -> service.crearBloque(j.getId(), horas(23, 0, 0, 0)));
    }

    @Test
    @DisplayName("crearBloque: se solapa por el final del existente (empieza dentro de él) -> Conflicto")
    void crearBloque_empiezaDentroDeOtro_lanzaConflicto() {
        Jornada j = jornadaConBloque(9, 0, 10, 0);

        assertConflicto(SOLAPE, () -> service.crearBloque(j.getId(), horas(9, 30, 10, 30)));

        assertEquals(1, bloques.count());
    }

    @Test
    @DisplayName("crearBloque: se solapa por el inicio del existente (termina dentro de él) -> Conflicto")
    void crearBloque_terminaDentroDeOtro_lanzaConflicto() {
        Jornada j = jornadaConBloque(9, 0, 10, 0);

        assertConflicto(SOLAPE, () -> service.crearBloque(j.getId(), horas(8, 30, 9, 30)));

        assertEquals(1, bloques.count());
    }

    @Test
    @DisplayName("crearBloque: queda contenido dentro de otro -> Conflicto")
    void crearBloque_contenidoEnOtro_lanzaConflicto() {
        Jornada j = jornadaConBloque(9, 0, 10, 0);

        assertConflicto(SOLAPE, () -> service.crearBloque(j.getId(), horas(9, 15, 9, 45)));
    }

    @Test
    @DisplayName("crearBloque: contiene por completo a otro -> Conflicto")
    void crearBloque_contieneAOtro_lanzaConflicto() {
        Jornada j = jornadaConBloque(9, 0, 10, 0);

        assertConflicto(SOLAPE, () -> service.crearBloque(j.getId(), horas(8, 0, 11, 0)));
    }

    @Test
    @DisplayName("crearBloque: idéntico a otro -> Conflicto")
    void crearBloque_identicoAOtro_lanzaConflicto() {
        Jornada j = jornadaConBloque(9, 0, 10, 0);

        assertConflicto(SOLAPE, () -> service.crearBloque(j.getId(), horas(9, 0, 10, 0)));
    }

    @Test
    @DisplayName("crearBloque: un solo minuto de solape ya es Conflicto")
    void crearBloque_unMinutoDeSolape_lanzaConflicto() {
        Jornada j = jornadaConBloque(9, 0, 10, 0);

        assertConflicto(SOLAPE, () -> service.crearBloque(j.getId(), horas(9, 59, 11, 0)));
    }

    @Test
    @DisplayName("crearBloque: contiguo después del existente (inicio == fin del otro) -> se crea")
    void crearBloque_contiguoDespues_creaBloque() {
        Jornada j = jornadaConBloque(9, 0, 10, 0);

        BloqueResponse res = service.crearBloque(j.getId(), horas(10, 0, 11, 0));

        assertEquals(LocalTime.of(10, 0), res.horaInicio());
        assertEquals(2, bloques.count());
    }

    @Test
    @DisplayName("crearBloque: contiguo antes del existente (fin == inicio del otro) -> se crea")
    void crearBloque_contiguoAntes_creaBloque() {
        Jornada j = jornadaConBloque(9, 0, 10, 0);

        BloqueResponse res = service.crearBloque(j.getId(), horas(8, 0, 9, 0));

        assertEquals(LocalTime.of(9, 0), res.horaFin());
        assertEquals(2, bloques.count());
    }

    @Test
    @DisplayName("crearBloque: un hueco exacto entre dos bloques se puede rellenar")
    void crearBloque_huecoExactoEntreDosBloques_creaBloque() {
        Jornada j = jornadaConBloque(8, 0, 9, 0);
        sembrarBloque(j, 10, 0, 11, 0);

        BloqueResponse res = service.crearBloque(j.getId(), horas(9, 0, 10, 0));

        assertEquals(LocalTime.of(9, 0), res.horaInicio());
        assertEquals(3, bloques.count());
    }

    @Test
    @DisplayName("crearBloque: solapa solo con el segundo de varios bloques -> Conflicto")
    void crearBloque_solapaConElSegundoBloque_lanzaConflicto() {
        Jornada j = jornadaConBloque(8, 0, 9, 0);
        sembrarBloque(j, 10, 0, 11, 0);

        assertConflicto(SOLAPE, () -> service.crearBloque(j.getId(), horas(10, 30, 11, 30)));

        assertEquals(2, bloques.count());
    }

    @Test
    @DisplayName("crearBloque: las mismas horas en OTRA jornada no son un solape")
    void crearBloque_mismasHorasEnOtraJornada_creaBloque() {
        jornadaConBloque(9, 0, 10, 0);
        Jornada otra = sembrarJornada(FECHA.plusDays(1), Base.TALCAHUANO);

        BloqueResponse res = service.crearBloque(otra.getId(), horas(9, 0, 10, 0));

        assertEquals(LocalTime.of(9, 0), res.horaInicio());
        assertEquals(2, bloques.count());
    }

    // ------------------------------------------------------------------ obtenerBloques

    @Test
    @DisplayName("obtenerBloques: la jornada no existe -> RecursoNoEncontrado")
    void obtenerBloques_jornadaNoExiste_lanzaRecursoNoEncontrado() {
        assertNoEncontrado("Jornada 99 no existe", () -> service.obtenerBloques(99L));
    }

    @Test
    @DisplayName("obtenerBloques: jornada sin bloques -> lista vacía")
    void obtenerBloques_jornadaSinBloques_devuelveListaVacia() {
        Jornada j = sembrarJornada(FECHA, Base.TOBALABA);

        List<BloqueResponse> res = service.obtenerBloques(j.getId());

        assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("obtenerBloques: bloques sin asignaciones traen lista de asignaciones vacía")
    void obtenerBloques_bloquesSinAsignaciones_traenListaVacia() {
        Jornada j = jornadaConBloque(9, 0, 10, 0);

        List<BloqueResponse> res = service.obtenerBloques(j.getId());

        assertEquals(1, res.size());
        assertEquals(List.of(), res.get(0).asignaciones());
    }

    @Test
    @DisplayName("obtenerBloques: devuelve los bloques ordenados por hora de inicio")
    void obtenerBloques_bloquesDesordenados_losDevuelveOrdenados() {
        Jornada j = sembrarJornada(FECHA, Base.TOBALABA);
        sembrarBloque(j, 11, 0, 12, 0); // id 1
        sembrarBloque(j, 8, 0, 9, 0);   // id 2
        sembrarBloque(j, 9, 30, 10, 30); // id 3

        List<BloqueResponse> res = service.obtenerBloques(j.getId());

        assertEquals(List.of(2L, 3L, 1L), res.stream().map(BloqueResponse::id).toList());
    }

    @Test
    @DisplayName("obtenerBloques: agrupa cada asignación bajo su bloque")
    void obtenerBloques_conAsignaciones_lasAgrupaPorBloque() {
        Jornada j = sembrarJornada(FECHA, Base.TOBALABA);
        BloqueHorario b1 = sembrarBloque(j, 9, 0, 10, 0);
        BloqueHorario b2 = sembrarBloque(j, 10, 0, 11, 0);
        sembrarBloque(j, 11, 0, 12, 0);
        Asignacion a1 = sembrarAsignacion(1L, b1, EstadoAsignacion.PENDIENTE);
        Asignacion a2 = sembrarAsignacion(2L, b1, EstadoAsignacion.CONFIRMADA);
        Asignacion a3 = sembrarAsignacion(3L, b2, EstadoAsignacion.PENDIENTE);

        List<BloqueResponse> res = service.obtenerBloques(j.getId());

        assertEquals(3, res.size());
        assertEquals(List.of(a1.getId(), a2.getId()),
                res.get(0).asignaciones().stream().map(r -> r.id()).toList());
        assertEquals(List.of(a3.getId()), res.get(1).asignaciones().stream().map(r -> r.id()).toList());
        assertEquals(List.of(), res.get(2).asignaciones());
    }

    @Test
    @DisplayName("obtenerBloques: cada asignación trae todos sus campos")
    void obtenerBloques_asignacion_traeTodosSusCampos() {
        Jornada j = sembrarJornada(FECHA, Base.TOBALABA);
        BloqueHorario b = sembrarBloque(j, 9, 0, 10, 0);
        asignaciones.save(new Asignacion(null, 5L, 6L, 7L, b, EstadoAsignacion.CONFIRMADA));

        var asignacion = service.obtenerBloques(j.getId()).get(0).asignaciones().get(0);

        assertEquals(1L, asignacion.id());
        assertEquals(5L, asignacion.pasajeroId());
        assertEquals(b.getId(), asignacion.bloqueId());
        assertEquals(6L, asignacion.aeronaveId());
        assertEquals(7L, asignacion.pilotoId());
        assertEquals(EstadoAsignacion.CONFIRMADA, asignacion.estado());
    }

    @Test
    @DisplayName("obtenerBloques: incluye también las asignaciones CANCELADAS (comportamiento actual)")
    void obtenerBloques_conAsignacionCancelada_laIncluye() {
        Jornada j = sembrarJornada(FECHA, Base.TOBALABA);
        BloqueHorario b = sembrarBloque(j, 9, 0, 10, 0);
        sembrarAsignacion(1L, b, EstadoAsignacion.CANCELADA);

        List<BloqueResponse> res = service.obtenerBloques(j.getId());

        assertEquals(1, res.get(0).asignaciones().size());
        assertEquals(EstadoAsignacion.CANCELADA, res.get(0).asignaciones().get(0).estado());
    }

    @Test
    @DisplayName("obtenerBloques: no mezcla bloques ni asignaciones de otras jornadas")
    void obtenerBloques_conOtraJornada_noMezclaDatos() {
        Jornada j1 = sembrarJornada(FECHA, Base.TOBALABA);
        Jornada j2 = sembrarJornada(FECHA.plusDays(1), Base.TALCAHUANO);
        BloqueHorario b1 = sembrarBloque(j1, 9, 0, 10, 0);
        BloqueHorario b2 = sembrarBloque(j2, 9, 0, 10, 0);
        sembrarAsignacion(1L, b1, EstadoAsignacion.PENDIENTE);
        sembrarAsignacion(2L, b2, EstadoAsignacion.PENDIENTE);

        List<BloqueResponse> res = service.obtenerBloques(j1.getId());

        assertEquals(1, res.size());
        assertEquals(b1.getId(), res.get(0).id());
        assertEquals(1, res.get(0).asignaciones().size());
        assertEquals(1L, res.get(0).asignaciones().get(0).pasajeroId());
    }

    // ------------------------------------------------------------------ ayudas

    private static BloqueRequest horas(int hIni, int mIni, int hFin, int mFin) {
        return new BloqueRequest(LocalTime.of(hIni, mIni), LocalTime.of(hFin, mFin));
    }

    private Jornada sembrarJornada(LocalDate fecha, Base base) {
        return jornadas.save(new Jornada(null, fecha, base, new ArrayList<>()));
    }

    private BloqueHorario sembrarBloque(Jornada j, int hIni, int mIni, int hFin, int mFin) {
        return bloques.save(new BloqueHorario(null, j, LocalTime.of(hIni, mIni), LocalTime.of(hFin, mFin)));
    }

    private Jornada jornadaConBloque(int hIni, int mIni, int hFin, int mFin) {
        Jornada j = sembrarJornada(FECHA, Base.TOBALABA);
        sembrarBloque(j, hIni, mIni, hFin, mFin);
        return j;
    }

    private Asignacion sembrarAsignacion(Long pasajeroId, BloqueHorario bloque, EstadoAsignacion estado) {
        return asignaciones.save(new Asignacion(null, pasajeroId, 1L, 1L, bloque, estado));
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
