package com.clubaereo.aeronaves_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.clubaereo.aeronaves_service.dto.AbrirVueloRequest;
import com.clubaereo.aeronaves_service.dto.CerrarVueloRequest;
import com.clubaereo.aeronaves_service.dto.EstadisticasVueloDTO;
import com.clubaereo.aeronaves_service.dto.VueloResponse;
import com.clubaereo.aeronaves_service.entities.Aeronave;
import com.clubaereo.aeronaves_service.entities.EstadoVuelo;
import com.clubaereo.aeronaves_service.entities.Piloto;
import com.clubaereo.aeronaves_service.entities.RegistroVuelo;
import com.clubaereo.aeronaves_service.exception.RecursoNoEncontradoException;
import com.clubaereo.aeronaves_service.exception.ReglaNegocioException;
import com.clubaereo.aeronaves_service.fake.FakeAeronaveRepository;
import com.clubaereo.aeronaves_service.fake.FakePilotoRepository;
import com.clubaereo.aeronaves_service.fake.FakeRegistroVueloRepository;
import com.clubaereo.aeronaves_service.service.VueloService;

/**
 * Versión SIN Mockito de VueloServiceTest.
 * En vez de @Mock + when(...).thenReturn(...), usamos repositorios falsos con datos reales
 * en memoria. En vez de verify(...), comprobamos el estado final en el fake.
 */
class VueloServiceTest {

    private FakeAeronaveRepository aeronaves;
    private FakePilotoRepository pilotos;
    private FakeRegistroVueloRepository vuelos;
    private VueloService service;

    private Aeronave cessna;
    private Piloto ana;

    @BeforeEach
    void setUp() {
        aeronaves = new FakeAeronaveRepository();
        pilotos = new FakePilotoRepository();
        vuelos = new FakeRegistroVueloRepository();
        // Mismo orden del constructor generado por @RequiredArgsConstructor
        service = new VueloService(aeronaves.repo(), pilotos.repo(), vuelos.repo());

        cessna = aeronaves.save(new Aeronave(null, "CC-ABC", "Cessna 172", 3, true));
        ana = pilotos.save(new Piloto(null, "Ana", true));
    }

    // ---------- helpers ----------

    private RegistroVuelo guardarVuelo(Aeronave a, Piloto p, Long jornadaId,
                                       String horometroInicial, EstadoVuelo estado) {
        return vuelos.save(RegistroVuelo.builder()
                .aeronave(a).piloto(p).jornadaId(jornadaId)
                .horometroInicial(new BigDecimal(horometroInicial))
                .estado(estado).build());
    }

    private RegistroVuelo vueloAbierto() {
        return guardarVuelo(cessna, ana, 1L, "100.0", EstadoVuelo.ABIERTO);
    }

    private AbrirVueloRequest abrirReq(Long aeronaveId, Long pilotoId) {
        return new AbrirVueloRequest(aeronaveId, pilotoId, 1L, new BigDecimal("100.0"));
    }

    // =========================== abrirVuelo ===========================

    @Test
    @DisplayName("abrirVuelo: caso feliz crea un vuelo ABIERTO y lo persiste")
    void abrirVuelo_valido_creaVueloAbierto() {
        VueloResponse res = service.abrirVuelo(abrirReq(cessna.getId(), ana.getId()));

        assertNotNull(res.id());
        assertEquals(EstadoVuelo.ABIERTO, res.estado());
        assertEquals("CC-ABC", res.matricula());
        assertEquals("Ana", res.nombrePiloto());
        assertEquals(new BigDecimal("100.0"), res.horometroInicial());
        assertEquals(1, vuelos.count());
    }

    @Test
    @DisplayName("abrirVuelo: aeronave inexistente")
    void abrirVuelo_aeronaveNoExiste_falla() {
        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
                () -> service.abrirVuelo(abrirReq(99L, ana.getId())));

        assertEquals("Aeronave no encontrada: 99", ex.getMessage());
    }

    @Test
    @DisplayName("abrirVuelo: piloto inexistente")
    void abrirVuelo_pilotoNoExiste_falla() {
        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
                () -> service.abrirVuelo(abrirReq(cessna.getId(), 99L)));

        assertEquals("Piloto no encontrado: 99", ex.getMessage());
    }

    @Test
    @DisplayName("abrirVuelo: aeronave no disponible")
    void abrirVuelo_aeronaveNoDisponible_falla() {
        cessna.setDisponible(false);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.abrirVuelo(abrirReq(cessna.getId(), ana.getId())));

        assertEquals("La aeronave CC-ABC no está disponible", ex.getMessage());
        assertEquals(0, vuelos.count());
    }

    @Test
    @DisplayName("abrirVuelo: piloto sin licencia DGAC vigente")
    void abrirVuelo_pilotoSinLicencia_falla() {
        ana.setLicenciaDgacVigente(false);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.abrirVuelo(abrirReq(cessna.getId(), ana.getId())));

        assertEquals("El piloto Ana no tiene licencia DGAC vigente", ex.getMessage());
    }

    @Test
    @DisplayName("abrirVuelo: aeronave que ya tiene un vuelo abierto")
    void abrirVuelo_aeronaveConVueloAbierto_falla() {
        Piloto luis = pilotos.save(new Piloto(null, "Luis", true));
        guardarVuelo(cessna, luis, 1L, "90.0", EstadoVuelo.ABIERTO);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.abrirVuelo(abrirReq(cessna.getId(), ana.getId())));

        assertEquals("La aeronave CC-ABC ya tiene un vuelo abierto", ex.getMessage());
    }

    @Test
    @DisplayName("abrirVuelo: piloto que ya tiene un vuelo abierto")
    void abrirVuelo_pilotoConVueloAbierto_falla() {
        Aeronave piper = aeronaves.save(new Aeronave(null, "CC-PPP", "Piper", 4, true));
        guardarVuelo(piper, ana, 1L, "50.0", EstadoVuelo.ABIERTO);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.abrirVuelo(abrirReq(cessna.getId(), ana.getId())));

        assertEquals("El piloto Ana ya tiene un vuelo abierto", ex.getMessage());
    }

    @Test
    @DisplayName("abrirVuelo: un vuelo CERRADO previo no bloquea abrir uno nuevo")
    void abrirVuelo_conVueloCerradoPrevio_permite() {
        guardarVuelo(cessna, ana, 1L, "90.0", EstadoVuelo.CERRADO);

        VueloResponse res = service.abrirVuelo(abrirReq(cessna.getId(), ana.getId()));

        assertEquals(EstadoVuelo.ABIERTO, res.estado());
        assertEquals(2, vuelos.count());
    }

    // =========================== cerrarVuelo ===========================

    @Test
    @DisplayName("cerrarVuelo: calcula horas consumidas y cierra el vuelo")
    void cerrarVuelo_calculaHorasConsumidas() {
        RegistroVuelo r = vueloAbierto();

        VueloResponse res = service.cerrarVuelo(r.getId(),
                new CerrarVueloRequest(new BigDecimal("101.5"), 2));

        assertEquals(new BigDecimal("1.5"), res.horasConsumidas());
        assertEquals(EstadoVuelo.CERRADO, res.estado());
        // Estado persistido en el fake:
        RegistroVuelo guardado = vuelos.findById(r.getId()).orElseThrow();
        assertEquals(EstadoVuelo.CERRADO, guardado.getEstado());
        assertEquals(2, guardado.getNTripulantes());
    }

    @Test
    @DisplayName("cerrarVuelo: redondea las horas a 1 decimal (HALF_UP)")
    void cerrarVuelo_redondeaHoras() {
        RegistroVuelo r = vueloAbierto();

        VueloResponse res = service.cerrarVuelo(r.getId(),
                new CerrarVueloRequest(new BigDecimal("100.25"), 1));

        assertEquals(new BigDecimal("0.3"), res.horasConsumidas());
    }

    @Test
    @DisplayName("cerrarVuelo: tripulantes iguales a la capacidad es válido (caso límite)")
    void cerrarVuelo_tripulantesIgualCapacidad_ok() {
        RegistroVuelo r = vueloAbierto();

        VueloResponse res = service.cerrarVuelo(r.getId(),
                new CerrarVueloRequest(new BigDecimal("101.0"), 3));

        assertEquals(EstadoVuelo.CERRADO, res.estado());
    }

    @Test
    @DisplayName("cerrarVuelo: vuelo inexistente")
    void cerrarVuelo_noExiste_falla() {
        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
                () -> service.cerrarVuelo(77L, new CerrarVueloRequest(new BigDecimal("101.0"), 1)));

        assertEquals("Vuelo no encontrado: 77", ex.getMessage());
    }

    @Test
    @DisplayName("cerrarVuelo: horómetro final igual al inicial falla")
    void cerrarVuelo_horometroIgual_falla() {
        RegistroVuelo r = vueloAbierto();

        assertThrows(ReglaNegocioException.class, () -> service.cerrarVuelo(r.getId(),
                new CerrarVueloRequest(new BigDecimal("100.0"), 1)));
    }

    @Test
    @DisplayName("cerrarVuelo: horómetro final menor al inicial falla")
    void cerrarVuelo_horometroMenor_falla() {
        RegistroVuelo r = vueloAbierto();

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.cerrarVuelo(r.getId(),
                        new CerrarVueloRequest(new BigDecimal("99.0"), 1)));

        assertEquals("El horómetro final debe ser mayor al inicial (100.0)", ex.getMessage());
    }

    @Test
    @DisplayName("cerrarVuelo: tripulantes sobre la capacidad falla")
    void cerrarVuelo_tripulantesSobreCapacidad_falla() {
        RegistroVuelo r = vueloAbierto();

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.cerrarVuelo(r.getId(),
                        new CerrarVueloRequest(new BigDecimal("101.0"), 4)));

        assertEquals("Los tripulantes (4) superan la capacidad de la aeronave (3)", ex.getMessage());
    }

    @Test
    @DisplayName("cerrarVuelo: un vuelo ya cerrado no se puede cerrar de nuevo")
    void cerrarVuelo_yaCerrado_falla() {
        RegistroVuelo r = vueloAbierto();
        r.setEstado(EstadoVuelo.CERRADO);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.cerrarVuelo(r.getId(),
                        new CerrarVueloRequest(new BigDecimal("101.0"), 1)));

        assertEquals("El vuelo " + r.getId() + " ya está cerrado", ex.getMessage());
    }

    // =========================== listarPorJornada ===========================

    @Test
    @DisplayName("listarPorJornada: solo devuelve los vuelos de esa jornada")
    void listarPorJornada_filtraPorJornada() {
        Piloto luis = pilotos.save(new Piloto(null, "Luis", true));
        guardarVuelo(cessna, ana, 1L, "100.0", EstadoVuelo.CERRADO);
        guardarVuelo(cessna, luis, 2L, "110.0", EstadoVuelo.CERRADO);

        List<VueloResponse> res = service.listarPorJornada(1L);

        assertEquals(1, res.size());
        assertEquals("Ana", res.get(0).nombrePiloto());
    }

    // =========================== obtenerEstadisticas ===========================

    @Test
    @DisplayName("obtenerEstadisticas: suma solo vuelos CERRADOS de la jornada")
    void obtenerEstadisticas_sumaVuelosCerrados() {
        Piloto luis = pilotos.save(new Piloto(null, "Luis", true));
        Aeronave piper = aeronaves.save(new Aeronave(null, "CC-PPP", "Piper", 4, true));

        RegistroVuelo v1 = guardarVuelo(cessna, ana, 1L, "100.0", EstadoVuelo.ABIERTO);
        RegistroVuelo v2 = guardarVuelo(piper, luis, 1L, "50.0", EstadoVuelo.ABIERTO);
        service.cerrarVuelo(v1.getId(), new CerrarVueloRequest(new BigDecimal("101.5"), 2)); // 1.5 h
        service.cerrarVuelo(v2.getId(), new CerrarVueloRequest(new BigDecimal("52.0"), 3));  // 2.0 h
        guardarVuelo(cessna, ana, 1L, "101.5", EstadoVuelo.ABIERTO); // abierto: no cuenta

        EstadisticasVueloDTO e = service.obtenerEstadisticas(1L);

        assertEquals(2L, e.totalVuelos());
        assertEquals(2L, e.pilotosDistintos());
        assertEquals(2L, e.aeronavesDistintas());
        assertEquals(5L, e.totalTripulantes());
        assertEquals(new BigDecimal("3.5"), e.horasTotales());
    }

    @Test
    @DisplayName("obtenerEstadisticas: jornada sin vuelos cerrados devuelve ceros, no null")
    void obtenerEstadisticas_sinVuelos_devuelveCeros() {
        EstadisticasVueloDTO e = service.obtenerEstadisticas(99L);

        assertEquals(0L, e.totalVuelos());
        assertEquals(0L, e.totalTripulantes());
        assertEquals(BigDecimal.ZERO, e.horasTotales());
    }
}
