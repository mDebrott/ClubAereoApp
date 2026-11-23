package com.clubaereo.aeronaves_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.clubaereo.aeronaves_service.dto.AeronaveRequest;
import com.clubaereo.aeronaves_service.dto.AeronaveResponse;
import com.clubaereo.aeronaves_service.entities.Aeronave;
import com.clubaereo.aeronaves_service.exception.RecursoNoEncontradoException;
import com.clubaereo.aeronaves_service.exception.ReglaNegocioException;
import com.clubaereo.aeronaves_service.fake.FakeAeronaveRepository;
import com.clubaereo.aeronaves_service.service.AeronaveService;

class AeronaveServiceTest {

    private FakeAeronaveRepository fakeRepo;
    private AeronaveService service;

    @BeforeEach
    void setUp() {
        fakeRepo = new FakeAeronaveRepository();
        service = new AeronaveService(fakeRepo.repo());
    }

    // ---------- crear ----------

    @Test
    @DisplayName("crear: normaliza la matrícula (trim + mayúsculas) y deja la aeronave disponible")
    void crear_normalizaMatriculaYDejaDisponible() {
        AeronaveResponse res = service.crear(new AeronaveRequest("  cc-abc ", " Cessna 172 ", 3));

        assertNotNull(res.id());
        assertEquals("CC-ABC", res.matricula());
        assertEquals("Cessna 172", res.modelo());
        assertEquals(3, res.capacidad());
        assertTrue(res.disponible());
        assertEquals(1, fakeRepo.count()); // quedó guardada
    }

    @Test
    @DisplayName("crear: matrícula repetida lanza ReglaNegocioException")
    void crear_matriculaDuplicada_falla() {
        service.crear(new AeronaveRequest("CC-ABC", "Cessna 172", 3));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.crear(new AeronaveRequest("cc-abc", "Piper", 2)));

        assertEquals("Ya existe una aeronave con matrícula CC-ABC", ex.getMessage());
        assertEquals(1, fakeRepo.count()); // no se guardó la segunda
    }

    // ---------- obtener ----------

    @Test
    @DisplayName("obtener: devuelve la aeronave existente")
    void obtener_existente_devuelveAeronave() {
        Aeronave guardada = fakeRepo.save(new Aeronave(null, "CC-XYZ", "Piper PA-28", 4, true));

        AeronaveResponse res = service.obtener(guardada.getId());

        assertEquals("CC-XYZ", res.matricula());
    }

    @Test
    @DisplayName("obtener: id inexistente lanza RecursoNoEncontradoException")
    void obtener_inexistente_falla() {
        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
                () -> service.obtener(99L));

        assertEquals("Aeronave no encontrada: 99", ex.getMessage());
    }

    // ---------- listarDisponibles ----------

    @Test
    @DisplayName("listarDisponibles: excluye las aeronaves no disponibles")
    void listarDisponibles_filtraNoDisponibles() {
        fakeRepo.save(new Aeronave(null, "CC-AAA", "Cessna 172", 3, true));
        fakeRepo.save(new Aeronave(null, "CC-BBB", "Cessna 150", 2, false));
        fakeRepo.save(new Aeronave(null, "CC-CCC", "Piper", 4, true));

        List<AeronaveResponse> res = service.listarDisponibles();

        assertEquals(2, res.size());
        assertTrue(res.stream().noneMatch(r -> r.matricula().equals("CC-BBB")));
    }

    @Test
    @DisplayName("listarDisponibles: sin aeronaves devuelve lista vacía")
    void listarDisponibles_sinDatos_devuelveVacia() {
        assertTrue(service.listarDisponibles().isEmpty());
    }

    // ---------- cambiarDisponibilidad ----------

    @Test
    @DisplayName("cambiarDisponibilidad: actualiza y persiste el nuevo valor")
    void cambiarDisponibilidad_actualizaYPersiste() {
        Aeronave a = fakeRepo.save(new Aeronave(null, "CC-AAA", "Cessna 172", 3, true));

        AeronaveResponse res = service.cambiarDisponibilidad(a.getId(), false);

        assertFalse(res.disponible());
        // Verificamos el ESTADO final en el fake (en vez de verify() de Mockito)
        assertFalse(fakeRepo.findById(a.getId()).orElseThrow().getDisponible());
    }

    @Test
    @DisplayName("cambiarDisponibilidad: id inexistente lanza RecursoNoEncontradoException")
    void cambiarDisponibilidad_inexistente_falla() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.cambiarDisponibilidad(99L, true));
    }
}
