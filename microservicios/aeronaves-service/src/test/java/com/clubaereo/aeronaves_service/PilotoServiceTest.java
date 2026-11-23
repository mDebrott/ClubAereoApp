package com.clubaereo.aeronaves_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.clubaereo.aeronaves_service.dto.PilotoRequest;
import com.clubaereo.aeronaves_service.dto.PilotoResponse;
import com.clubaereo.aeronaves_service.entities.Piloto;
import com.clubaereo.aeronaves_service.exception.RecursoNoEncontradoException;
import com.clubaereo.aeronaves_service.fake.FakePilotoRepository;
import com.clubaereo.aeronaves_service.service.PilotoService;

class PilotoServiceTest {

    private FakePilotoRepository fakeRepo;
    private PilotoService service;

    @BeforeEach
    void setUp() {
        fakeRepo = new FakePilotoRepository();
        service = new PilotoService(fakeRepo.repo());
    }

    @Test
    @DisplayName("crear: licencia null se interpreta como vigente")
    void crear_licenciaNull_quedaVigente() {
        PilotoResponse res = service.crear(new PilotoRequest("  Ana  ", null));

        assertNotNull(res.id());
        assertEquals("Ana", res.nombre());
        assertTrue(res.licenciaDgacVigente());
    }

    @Test
    @DisplayName("crear: respeta licencia = false")
    void crear_licenciaFalse_quedaNoVigente() {
        PilotoResponse res = service.crear(new PilotoRequest("Luis", false));

        assertFalse(res.licenciaDgacVigente());
    }

    @Test
    @DisplayName("listar: devuelve todos los pilotos")
    void listar_devuelveTodos() {
        fakeRepo.save(new Piloto(null, "Ana", true));
        fakeRepo.save(new Piloto(null, "Luis", false));

        assertEquals(2, service.listar().size());
    }

    @Test
    @DisplayName("obtener: id inexistente lanza RecursoNoEncontradoException")
    void obtener_inexistente_falla() {
        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
                () -> service.obtener(5L));

        assertEquals("Piloto no encontrado: 5", ex.getMessage());
    }

    @Test
    @DisplayName("cambiarLicencia: actualiza y persiste")
    void cambiarLicencia_actualizaYPersiste() {
        Piloto p = fakeRepo.save(new Piloto(null, "Ana", true));

        PilotoResponse res = service.cambiarLicencia(p.getId(), false);

        assertFalse(res.licenciaDgacVigente());
        assertFalse(fakeRepo.findById(p.getId()).orElseThrow().getLicenciaDgacVigente());
    }

    @Test
    @DisplayName("cambiarLicencia: piloto inexistente lanza RecursoNoEncontradoException")
    void cambiarLicencia_inexistente_falla() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.cambiarLicencia(99L, true));
    }
}
