package com.clubaereo.pasajero_service.fake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

import com.clubaereo.pasajero_service.entities.EstadoConsentimiento;
import com.clubaereo.pasajero_service.entities.Pasajero;
import com.clubaereo.pasajero_service.repository.PasajeroRepository;

/**
 * Pruebas de la infraestructura de pruebas: la base InMemoryRepository y su Proxy.
 * Si el fake falla, los tests del service darían falsos verdes; por eso se prueba aparte.
 */
class InMemoryRepositoryTest {

    private FakePasajeroRepository fake;
    private PasajeroRepository repo;

    @BeforeEach
    void setUp() {
        fake = new FakePasajeroRepository();
        repo = fake.repo();
    }

    private static Pasajero pasajero(String rut, Long jornadaId) {
        Pasajero p = new Pasajero();
        p.setRut(rut);
        p.setNombreCompleto("Persona " + rut);
        p.setJornadaId(jornadaId);
        return p;
    }

    private static Pasajero pasajeroConId(Long id, String rut) {
        Pasajero p = pasajero(rut, 1L);
        p.setId(id);
        return p;
    }

    // ---------------- ids ----------------

    @Test
    @DisplayName("save: una entidad nueva recibe el id 1 y se devuelve con ese id")
    void save_entidadNueva_asignaId1() {
        Pasajero guardado = repo.save(pasajero("11111111-1", 1L));

        assertEquals(1L, guardado.getId());
        assertEquals(1L, repo.count());
    }

    @Test
    @DisplayName("save: varias entidades nuevas reciben ids consecutivos")
    void save_variasEntidadesNuevas_asignaIdsConsecutivos() {
        Pasajero a = repo.save(pasajero("11111111-1", 1L));
        Pasajero b = repo.save(pasajero("22222222-2", 1L));
        Pasajero c = repo.save(pasajero("12345678-5", 1L));

        assertEquals(1L, a.getId());
        assertEquals(2L, b.getId());
        assertEquals(3L, c.getId());
    }

    @Test
    @DisplayName("save: respeta un id explícito")
    void save_idExplicito_loRespeta() {
        Pasajero guardado = repo.save(pasajeroConId(10L, "11111111-1"));

        assertEquals(10L, guardado.getId());
        assertTrue(repo.existsById(10L));
        assertFalse(repo.existsById(1L));
    }

    @Test
    @DisplayName("save: tras un id explícito, el siguiente automático no colisiona")
    void save_idExplicito_laSiguienteAutomaticaNoColisiona() {
        repo.save(pasajeroConId(10L, "11111111-1"));

        Pasajero automatico = repo.save(pasajero("22222222-2", 1L));

        assertEquals(11L, automatico.getId());
        assertEquals(2L, repo.count());
        assertEquals("11111111-1", repo.findById(10L).orElseThrow().getRut());
    }

    @Test
    @DisplayName("save: un id explícito menor al contador no lo hace retroceder")
    void save_idExplicitoMenorAlContador_noHaceRetrocederElContador() {
        repo.save(pasajero("11111111-1", 1L));          // id 1
        repo.save(pasajeroConId(10L, "22222222-2"));    // contador pasa a 11
        repo.save(pasajeroConId(5L, "12345678-5"));     // id explícito menor

        Pasajero automatico = repo.save(pasajero("7654321-6", 1L));

        assertEquals(11L, automatico.getId());
        assertEquals(4L, repo.count());
    }

    // ---------------- save: actualización, copias y restricciones ----------------

    @Test
    @DisplayName("save: guardar una entidad con id existente la actualiza sin duplicarla")
    void save_entidadExistente_actualizaSinDuplicar() {
        Pasajero p = repo.save(pasajero("11111111-1", 1L));
        p.setNombreCompleto("Nombre nuevo");

        repo.save(p);

        assertEquals(1L, repo.count());
        assertEquals("Nombre nuevo", repo.findById(p.getId()).orElseThrow().getNombreCompleto());
    }

    @Test
    @DisplayName("save: guarda una copia; cambiar el objeto original sin guardar no altera lo almacenado")
    void save_guardaUnaCopia_cambiarElOriginalNoAlteraLoAlmacenado() {
        Pasajero original = pasajero("11111111-1", 1L);
        repo.save(original);

        original.setNombreCompleto("Cambio sin save");

        assertEquals("Persona 11111111-1", repo.findById(original.getId()).orElseThrow().getNombreCompleto());
    }

    @Test
    @DisplayName("save: la restricción única (rut, jornada) lanza DataIntegrityViolationException sin guardar ni consumir id")
    void save_restriccionUnicaViolada_lanzaExcepcionSinGuardarNiConsumirId() {
        repo.save(pasajero("11111111-1", 1L)); // id 1

        assertThrows(DataIntegrityViolationException.class,
                () -> repo.save(pasajero("11111111-1", 1L)));

        assertEquals(1L, repo.count());
        Pasajero siguiente = repo.save(pasajero("22222222-2", 1L));
        assertEquals(2L, siguiente.getId());
    }

    @Test
    @DisplayName("save: entidad nula lanza IllegalArgumentException")
    void save_entidadNula_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> repo.save(null));
    }

    // ---------------- lectura y borrado ----------------

    @Test
    @DisplayName("findById: id inexistente devuelve Optional vacío")
    void findById_inexistente_devuelveVacio() {
        assertTrue(repo.findById(99L).isEmpty());
    }

    @Test
    @DisplayName("findById: devuelve una copia; modificarla no altera lo almacenado")
    void findById_devuelveCopia_modificarlaNoAlteraLoAlmacenado() {
        Pasajero p = repo.save(pasajero("11111111-1", 1L));

        Pasajero leido = repo.findById(p.getId()).orElseThrow();
        leido.setNombreCompleto("Modificado fuera");

        assertNotSame(leido, repo.findById(p.getId()).orElseThrow());
        assertEquals("Persona 11111111-1", repo.findById(p.getId()).orElseThrow().getNombreCompleto());
    }

    @Test
    @DisplayName("findById: id nulo lanza IllegalArgumentException (como Spring Data)")
    void findById_idNulo_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> repo.findById(null));
    }

    @Test
    @DisplayName("findAll: repositorio vacío devuelve lista vacía")
    void findAll_vacio_devuelveListaVacia() {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    @DisplayName("findAll: devuelve todas las entidades ordenadas por id")
    void findAll_devuelveTodasOrdenadasPorId() {
        repo.save(pasajeroConId(7L, "11111111-1"));
        repo.save(pasajeroConId(3L, "22222222-2"));

        List<Pasajero> todos = repo.findAll();

        assertEquals(2, todos.size());
        assertEquals(3L, todos.get(0).getId());
        assertEquals(7L, todos.get(1).getId());
    }

    @Test
    @DisplayName("count: repositorio vacío devuelve 0")
    void count_vacio_devuelveCero() {
        assertEquals(0L, repo.count());
    }

    @Test
    @DisplayName("existsById: refleja si la entidad existe")
    void existsById_reflejaSiExiste() {
        Pasajero p = repo.save(pasajero("11111111-1", 1L));

        assertTrue(repo.existsById(p.getId()));
        assertFalse(repo.existsById(p.getId() + 1));
    }

    @Test
    @DisplayName("deleteById: elimina la entidad existente")
    void deleteById_existente_laElimina() {
        Pasajero p = repo.save(pasajero("11111111-1", 1L));

        repo.deleteById(p.getId());

        assertEquals(0L, repo.count());
        assertTrue(repo.findById(p.getId()).isEmpty());
    }

    @Test
    @DisplayName("deleteById: id inexistente no lanza error (como Spring Data)")
    void deleteById_inexistente_noFalla() {
        repo.save(pasajero("11111111-1", 1L));

        repo.deleteById(99L);

        assertEquals(1L, repo.count());
    }

    // ---------------- el Proxy ----------------

    @Test
    @DisplayName("proxy: un método de JpaRepository sin implementar lanza UnsupportedOperationException con mensaje claro")
    void proxy_metodoNoImplementado_lanzaUnsupportedConMensajeClaro() {
        UnsupportedOperationException ex =
                assertThrows(UnsupportedOperationException.class, () -> repo.flush());

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("flush"), ex.getMessage());
        assertTrue(ex.getMessage().contains("FakePasajeroRepository"), ex.getMessage());
    }

    @Test
    @DisplayName("proxy: se distingue por número de parámetros (findAll() sí, findAll(Sort) no)")
    void proxy_sobrecargaPorNumeroDeParametros() {
        repo.save(pasajero("11111111-1", 1L));

        assertEquals(1, repo.findAll().size());
        assertThrows(UnsupportedOperationException.class, () -> repo.findAll(Sort.unsorted()));
    }

    @Test
    @DisplayName("proxy: toString, hashCode y equals funcionan")
    void proxy_toStringHashCodeEquals() {
        PasajeroRepository otro = new FakePasajeroRepository().repo();

        assertTrue(repo.toString().contains("FakePasajeroRepository"));
        assertEquals(repo.hashCode(), repo.hashCode());
        assertTrue(repo.equals(repo));
        assertFalse(repo.equals(otro));
        assertFalse(repo.equals(null));
        assertNotEquals(repo, otro);
    }

    @Test
    @DisplayName("proxy: la excepción del fake llega tal cual, sin InvocationTargetException ni UndeclaredThrowable")
    void proxy_excepcionDelFake_llegaSinEnvoltorio() {
        repo.save(pasajero("11111111-1", 1L));

        Exception ex = assertThrows(Exception.class, () -> repo.save(pasajero("11111111-1", 1L)));

        assertEquals(DataIntegrityViolationException.class, ex.getClass());
    }

    @Test
    @DisplayName("proxy: un pasajero guardado conserva el consentimiento PENDIENTE por defecto")
    void proxy_pasajeroNuevo_tieneConsentimientoPendientePorDefecto() {
        Pasajero p = repo.save(pasajero("11111111-1", 1L));

        assertEquals(EstadoConsentimiento.PENDIENTE,
                repo.findById(p.getId()).orElseThrow().getConsentimiento());
    }
}
