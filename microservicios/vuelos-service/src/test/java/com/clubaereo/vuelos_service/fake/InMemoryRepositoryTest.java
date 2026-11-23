package com.clubaereo.vuelos_service.fake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.clubaereo.vuelos_service.entities.Base;
import com.clubaereo.vuelos_service.entities.Jornada;
import com.clubaereo.vuelos_service.repository.JornadaRepository;

/** Verifica la base reutilizable (ids, operaciones básicas y comportamiento del Proxy). */
class InMemoryRepositoryTest {

    private FakeJornadaRepository fake;
    private JornadaRepository repo;

    @BeforeEach
    void preparar() {
        fake = new FakeJornadaRepository();
        repo = fake.repo();
    }

    @Test
    @DisplayName("save: asigna ids automáticos consecutivos a entidades sin id")
    void save_sinId_asignaIdsConsecutivos() {
        Jornada a = repo.save(jornada(1));
        Jornada b = repo.save(jornada(2));

        assertEquals(1L, a.getId());
        assertEquals(2L, b.getId());
    }

    @Test
    @DisplayName("save: respeta un id explícito y los ids automáticos siguientes no colisionan")
    void save_idExplicito_losSiguientesNoColisionan() {
        Jornada explicita = repo.save(jornadaConId(10L, 1));
        Jornada automatica = repo.save(jornada(2));

        assertEquals(10L, explicita.getId());
        assertEquals(11L, automatica.getId());
        assertEquals(2, repo.count());
    }

    @Test
    @DisplayName("save: un id explícito menor al siguiente no retrocede el contador")
    void save_idExplicitoMenor_noRetrocedeElContador() {
        repo.save(jornada(1));
        repo.save(jornada(2));
        repo.save(jornadaConId(1L, 3)); // reemplaza la fila 1

        Jornada nueva = repo.save(jornada(4));

        assertEquals(3L, nueva.getId());
    }

    @Test
    @DisplayName("save: guardar otra vez la misma entidad actualiza, no duplica")
    void save_mismaEntidadDosVeces_noDuplica() {
        Jornada j = repo.save(jornada(1));
        repo.save(j);

        assertEquals(1, repo.count());
    }

    @Test
    @DisplayName("findById / existsById: encuentran lo guardado y no lo inexistente")
    void findByIdYExistsById_distinguenExistentesDeInexistentes() {
        Jornada j = repo.save(jornada(1));

        assertSame(j, repo.findById(j.getId()).orElseThrow());
        assertTrue(repo.existsById(j.getId()));
        assertTrue(repo.findById(99L).isEmpty());
        assertFalse(repo.existsById(99L));
    }

    @Test
    @DisplayName("findAll: devuelve una copia (modificarla no altera el repositorio)")
    void findAll_devuelveCopia() {
        repo.save(jornada(1));

        List<Jornada> lista = repo.findAll();
        lista.clear();

        assertEquals(1, repo.findAll().size());
    }

    @Test
    @DisplayName("count: 0 en un repositorio vacío")
    void count_repositorioVacio_esCero() {
        assertEquals(0, repo.count());
    }

    @Test
    @DisplayName("deleteById: borra la fila y borrar un id inexistente no falla")
    void deleteById_borraYToleraInexistentes() {
        Jornada j = repo.save(jornada(1));

        repo.deleteById(j.getId());
        repo.deleteById(99L);

        assertEquals(0, repo.count());
    }

    @Test
    @DisplayName("proxy: la excepción real llega al test sin envoltura de reflexión")
    void proxy_excepcionDelFake_seDesenvuelve() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> repo.findById(null));

        assertEquals("The given id must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("proxy: un método no implementado lanza UnsupportedOperationException con mensaje claro")
    void proxy_metodoNoImplementado_lanzaUnsupportedConMensajeClaro() {
        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, repo::deleteAll);

        assertEquals("FakeJornadaRepository no implementa deleteAll(0 parámetros). Agrega un método público "
                + "con ese nombre y esa cantidad de parámetros al fake.", ex.getMessage());
    }

    @Test
    @DisplayName("proxy: toString, hashCode y equals no se envían al fake")
    void proxy_metodosDeObject_funcionan() {
        assertEquals("Proxy en memoria de JornadaRepository", repo.toString());
        assertEquals(repo.hashCode(), repo.hashCode());
        assertTrue(repo.equals(repo));
        assertFalse(repo.equals(new FakeJornadaRepository().repo()));
        assertNotEquals(null, repo);
    }

    @Test
    @DisplayName("repo: siempre devuelve el mismo proxy y comparte estado con el fake")
    void repo_mismoProxyYEstadoCompartido() {
        repo.save(jornada(1));

        assertSame(repo, fake.repo());
        assertEquals(1, fake.count());
    }

    @Test
    @DisplayName("proxy: una consulta propia (findByFechaAndBase) se despacha por nombre y nº de parámetros")
    void proxy_consultaPropia_seDespacha() {
        Jornada j = repo.save(jornada(5));

        assertSame(j, repo.findByFechaAndBase(LocalDate.of(2026, 11, 5), Base.TOBALABA).orElseThrow());
        assertTrue(repo.findByFechaAndBase(LocalDate.of(2026, 11, 5), Base.TALCAHUANO).isEmpty());
    }

    private static Jornada jornada(int dia) {
        return new Jornada(null, LocalDate.of(2026, 11, dia), Base.TOBALABA, new ArrayList<>());
    }

    private static Jornada jornadaConId(Long id, int dia) {
        return new Jornada(id, LocalDate.of(2026, 11, dia), Base.TOBALABA, new ArrayList<>());
    }
}
