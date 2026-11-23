package com.clubaereo.pasajero_service.fake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.clubaereo.pasajero_service.entities.Pasajero;
import com.clubaereo.pasajero_service.repository.PasajeroRepository;

/** Verifica que las consultas escritas a mano en el fake se comporten como las de Spring Data. */
class FakePasajeroRepositoryTest {

    private PasajeroRepository repo;

    @BeforeEach
    void setUp() {
        repo = new FakePasajeroRepository().repo();
    }

    private Pasajero guardar(String rut, Long jornadaId) {
        Pasajero p = new Pasajero();
        p.setRut(rut);
        p.setNombreCompleto("Persona " + rut);
        p.setJornadaId(jornadaId);
        return repo.save(p);
    }

    // ---------------- findByJornadaId ----------------

    @Test
    @DisplayName("findByJornadaId: devuelve solo los pasajeros de esa jornada")
    void findByJornadaId_devuelveSoloLosDeEsaJornada() {
        guardar("11111111-1", 1L);
        guardar("22222222-2", 1L);
        guardar("12345678-5", 2L);

        List<Pasajero> resultado = repo.findByJornadaId(1L);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(p -> p.getJornadaId().equals(1L)));
    }

    @Test
    @DisplayName("findByJornadaId: sin filas devuelve lista vacía, no null")
    void findByJornadaId_sinFilas_devuelveListaVacia() {
        guardar("11111111-1", 1L);

        assertTrue(repo.findByJornadaId(99L).isEmpty());
    }

    @Test
    @DisplayName("findByJornadaId: parámetro nulo devuelve lista vacía")
    void findByJornadaId_nulo_devuelveListaVacia() {
        guardar("11111111-1", 1L);

        assertTrue(repo.findByJornadaId(null).isEmpty());
    }

    // ---------------- findByRut ----------------

    @Test
    @DisplayName("findByRut: una sola fila se encuentra")
    void findByRut_unaFila_laEncuentra() {
        guardar("11111111-1", 1L);

        assertEquals("11111111-1", repo.findByRut("11111111-1").orElseThrow().getRut());
    }

    @Test
    @DisplayName("findByRut: sin filas devuelve Optional vacío")
    void findByRut_sinFilas_devuelveVacio() {
        assertTrue(repo.findByRut("11111111-1").isEmpty());
    }

    @Test
    @DisplayName("findByRut: el mismo RUT en dos jornadas lanza IncorrectResultSizeDataAccessException (como Spring Data)")
    void findByRut_enVariasJornadas_lanzaIncorrectResultSize() {
        guardar("11111111-1", 1L);
        guardar("11111111-1", 2L);

        assertThrows(IncorrectResultSizeDataAccessException.class, () -> repo.findByRut("11111111-1"));
    }

    // ---------------- findByRutAndJornadaId / existsByRutAndJornadaId ----------------

    @Test
    @DisplayName("findByRutAndJornadaId: coincidencia exacta de RUT y jornada")
    void findByRutAndJornadaId_coincidenciaExacta() {
        guardar("11111111-1", 1L);
        guardar("11111111-1", 2L);

        Pasajero p = repo.findByRutAndJornadaId("11111111-1", 2L).orElseThrow();

        assertEquals(2L, p.getJornadaId());
    }

    @Test
    @DisplayName("findByRutAndJornadaId: RUT existente en otra jornada devuelve vacío")
    void findByRutAndJornadaId_otraJornada_devuelveVacio() {
        guardar("11111111-1", 1L);

        assertTrue(repo.findByRutAndJornadaId("11111111-1", 2L).isEmpty());
    }

    @Test
    @DisplayName("findByRutAndJornadaId: la comparación del RUT distingue mayúsculas (como la BD)")
    void findByRutAndJornadaId_distingueMayusculas() {
        guardar("10000013-K", 1L);

        assertTrue(repo.findByRutAndJornadaId("10000013-k", 1L).isEmpty());
    }

    @Test
    @DisplayName("existsByRutAndJornadaId: true solo si coinciden ambos campos")
    void existsByRutAndJornadaId_soloSiCoincidenAmbos() {
        guardar("11111111-1", 1L);

        assertTrue(repo.existsByRutAndJornadaId("11111111-1", 1L));
        assertFalse(repo.existsByRutAndJornadaId("11111111-1", 2L));
        assertFalse(repo.existsByRutAndJornadaId("22222222-2", 1L));
    }

    // ---------------- restricciones de la BD ----------------

    @Test
    @DisplayName("save: el mismo RUT en otra jornada está permitido")
    void save_mismoRutEnOtraJornada_estaPermitido() {
        guardar("11111111-1", 1L);
        guardar("11111111-1", 2L);

        assertEquals(2L, repo.count());
    }

    @Test
    @DisplayName("save: actualizar un registro existente no viola la restricción única contra sí mismo")
    void save_actualizarRegistroExistente_noViolaUnicoContraSiMismo() {
        Pasajero p = guardar("11111111-1", 1L);
        p.setNombreCompleto("Otro nombre");

        repo.save(p);

        assertEquals(1L, repo.count());
    }

    @Test
    @DisplayName("save: un campo NOT NULL en null lanza DataIntegrityViolationException")
    void save_campoObligatorioNulo_lanzaDataIntegrity() {
        Pasajero sinNombre = new Pasajero();
        sinNombre.setRut("11111111-1");
        sinNombre.setJornadaId(1L);

        assertThrows(DataIntegrityViolationException.class, () -> repo.save(sinNombre));
        assertEquals(0L, repo.count());
    }
}
