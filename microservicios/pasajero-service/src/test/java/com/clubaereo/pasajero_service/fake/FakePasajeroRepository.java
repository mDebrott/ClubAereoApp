package com.clubaereo.pasajero_service.fake;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.clubaereo.pasajero_service.entities.Pasajero;
import com.clubaereo.pasajero_service.repository.PasajeroRepository;

/**
 * Repositorio de pasajeros en memoria. Los métodos de PasajeroRepository (findByXxx, existsByXxx)
 * se implementan a mano aquí, imitando lo que haría Spring Data contra PostgreSQL.
 *
 * Uso: {@code new PasajeroService(new FakePasajeroRepository().repo())}
 */
public class FakePasajeroRepository extends InMemoryRepository<Pasajero, PasajeroRepository> {

    public FakePasajeroRepository() {
        super(PasajeroRepository.class, Pasajero::getId, Pasajero::setId);
    }

    // ---------- Consultas propias de PasajeroRepository ----------

    /** SELECT ... WHERE jornada_id = ? (lista vacía si no hay filas). */
    public List<Pasajero> findByJornadaId(Long jornadaId) {
        return filtrar(p -> Objects.equals(p.getJornadaId(), jornadaId));
    }

    /**
     * SELECT ... WHERE rut = ?. Como la restricción única es (rut, jornada_id), un mismo RUT puede
     * estar en varias jornadas; Spring Data real lanza IncorrectResultSizeDataAccessException
     * cuando un Optional recibe más de una fila, y aquí lo imitamos.
     */
    public Optional<Pasajero> findByRut(String rut) {
        List<Pasajero> encontrados = filtrar(p -> Objects.equals(p.getRut(), rut));
        if (encontrados.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, encontrados.size());
        }
        return encontrados.stream().findFirst();
    }

    /** SELECT ... WHERE rut = ? AND jornada_id = ? (como mucho una fila por la restricción única). */
    public Optional<Pasajero> findByRutAndJornadaId(String rut, Long jornadaId) {
        return filtrar(p -> Objects.equals(p.getRut(), rut)
                && Objects.equals(p.getJornadaId(), jornadaId))
                .stream().findFirst();
    }

    public boolean existsByRutAndJornadaId(String rut, Long jornadaId) {
        return almacenadas().stream().anyMatch(p ->
                Objects.equals(p.getRut(), rut) && Objects.equals(p.getJornadaId(), jornadaId));
    }

    // ---------- Comportamiento de "base de datos" ----------

    /** Copia campo a campo. Si agregas un campo a Pasajero, agrégalo aquí también. */
    @Override
    protected Pasajero copiar(Pasajero o) {
        Pasajero c = new Pasajero();
        c.setId(o.getId());
        c.setRut(o.getRut());
        c.setNombreCompleto(o.getNombreCompleto());
        c.setJornadaId(o.getJornadaId());
        c.setPesoKg(o.getPesoKg());
        c.setConsentimiento(o.getConsentimiento());
        c.setFirmaBase64(o.getFirmaBase64());
        c.setFechaFirma(o.getFechaFirma());
        return c;
    }

    /** Imita las restricciones declaradas en la entidad: NOT NULL y UNIQUE (rut, jornada_id). */
    @Override
    protected void validarRestricciones(Pasajero p) {
        if (p.getRut() == null || p.getNombreCompleto() == null
                || p.getJornadaId() == null || p.getConsentimiento() == null) {
            throw new DataIntegrityViolationException(
                    "NOT NULL: rut, nombre_completo, jornada_id y consentimiento son obligatorios");
        }
        boolean duplicado = almacenadas().stream().anyMatch(otro ->
                !Objects.equals(otro.getId(), p.getId())
                        && Objects.equals(otro.getRut(), p.getRut())
                        && Objects.equals(otro.getJornadaId(), p.getJornadaId()));
        if (duplicado) {
            throw new DataIntegrityViolationException("UNIQUE uq_pasajero_rut_jornada violada");
        }
    }
}
