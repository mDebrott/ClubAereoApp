package com.clubaereo.pasajero_service.fake;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base reutilizable para fabricar repositorios Spring Data "falsos" que guardan todo en memoria.
 *
 * <p>Idea: un repositorio que extiende {@code JpaRepository} tiene ~30 métodos y no queremos
 * implementarlos a mano. En su lugar:
 * <ol>
 *   <li>Esta clase implementa SOLO los métodos que el código usa (save, findById, findAll,
 *       existsById, count, deleteById).</li>
 *   <li>Cada fake concreto agrega a mano las consultas propias (findByXxx, existsByXxx...).</li>
 *   <li>{@link #repo()} devuelve un {@link Proxy} de Java (NO de Mockito) que implementa la
 *       interfaz del repositorio y redirige cada llamada al método público de esta clase
 *       que tenga el mismo nombre y el mismo número de parámetros.</li>
 * </ol>
 *
 * <p>Detalles de diseño:
 * <ul>
 *   <li>Se guarda una COPIA de la entidad (ver {@link #copiar}). Así, un cambio hecho sobre un
 *       objeto que NO se pasó por {@code save()} no "se persiste" por accidente, igual que al
 *       hacer rollback en una base real.</li>
 *   <li>Los ids automáticos nunca colisionan con ids explícitos: si guardas una entidad con
 *       id=10, la siguiente automática recibe 11.</li>
 * </ul>
 *
 * @param <T> tipo de la entidad
 * @param <R> interfaz del repositorio (p. ej. PasajeroRepository)
 */
public abstract class InMemoryRepository<T, R> {

    private final Class<R> tipoRepositorio;
    private final Function<T, Long> lectorId;
    private final BiConsumer<T, Long> escritorId;
    private final Map<Long, T> almacen = new TreeMap<>();
    private long siguienteId = 1;
    private final R proxy;

    protected InMemoryRepository(Class<R> tipoRepositorio,
                                 Function<T, Long> lectorId,
                                 BiConsumer<T, Long> escritorId) {
        this.tipoRepositorio = tipoRepositorio;
        this.lectorId = lectorId;
        this.escritorId = escritorId;
        this.proxy = tipoRepositorio.cast(Proxy.newProxyInstance(
                tipoRepositorio.getClassLoader(),
                new Class<?>[] {tipoRepositorio},
                manejador()));
    }

    /** El repositorio "real" (un Proxy) que se le pasa al service. */
    public R repo() {
        return proxy;
    }

    // ------------------------------------------------------------------
    // Métodos de CrudRepository que implementa el fake.
    // Son públicos porque el Proxy los busca por nombre y nº de parámetros.
    // ------------------------------------------------------------------

    public T save(T entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }
        validarRestricciones(entidad); // antes de tocar nada: si falla, no se guarda ni se consume id
        Long id = lectorId.apply(entidad);
        if (id == null) {
            id = siguienteId++;
            escritorId.accept(entidad, id);
        } else if (id >= siguienteId) {
            siguienteId = id + 1;
        }
        almacen.put(id, copiar(entidad));
        return entidad;
    }

    public Optional<T> findById(Long id) {
        exigirId(id);
        T guardada = almacen.get(id);
        return Optional.ofNullable(guardada == null ? null : copiar(guardada));
    }

    public List<T> findAll() {
        return filtrar(e -> true);
    }

    public boolean existsById(Long id) {
        exigirId(id);
        return almacen.containsKey(id);
    }

    public long count() {
        return almacen.size();
    }

    public void deleteById(Long id) {
        exigirId(id);
        almacen.remove(id); // igual que Spring Data: si no existe, no pasa nada
    }

    // ------------------------------------------------------------------
    // Ganchos para los fakes concretos
    // ------------------------------------------------------------------

    /** Copia la entidad. Por defecto no copia; los fakes concretos deberían sobrescribirlo. */
    protected T copiar(T entidad) {
        return entidad;
    }

    /** Imita restricciones de la BD (UNIQUE, NOT NULL). Lanzar la excepción que lanzaría Spring. */
    protected void validarRestricciones(T entidad) {
        // por defecto, sin restricciones
    }

    /** Entidades almacenadas (sin copiar, solo lectura) para que los fakes escriban sus consultas. */
    protected Collection<T> almacenadas() {
        return Collections.unmodifiableCollection(almacen.values());
    }

    /** Devuelve COPIAS, ordenadas por id, de las entidades que cumplen el filtro. */
    protected List<T> filtrar(Predicate<T> filtro) {
        List<T> resultado = new ArrayList<>();
        for (T e : almacen.values()) {
            if (filtro.test(e)) {
                resultado.add(copiar(e));
            }
        }
        return resultado;
    }

    // ------------------------------------------------------------------
    // Proxy
    // ------------------------------------------------------------------

    private InvocationHandler manejador() {
        return (proxyObj, metodo, args) -> {
            if (metodo.getDeclaringClass() == Object.class) {
                return metodoDeObject(proxyObj, metodo, args);
            }
            int n = (args == null) ? 0 : args.length;
            Method destino = buscarMetodo(metodo.getName(), n);
            try {
                return destino.invoke(this, args);
            } catch (InvocationTargetException e) {
                throw e.getCause(); // el test debe ver la excepción real, no el envoltorio
            }
        };
    }

    private Object metodoDeObject(Object proxyObj, Method metodo, Object[] args) {
        switch (metodo.getName()) {
            case "toString":
                return "Fake" + tipoRepositorio.getSimpleName()
                        + "@" + Integer.toHexString(System.identityHashCode(proxyObj));
            case "hashCode":
                return System.identityHashCode(proxyObj);
            case "equals":
                return proxyObj == args[0];
            default:
                throw new UnsupportedOperationException(
                        "Método de Object no soportado en el proxy: " + metodo.getName());
        }
    }

    /**
     * Busca en el fake un método público con el mismo nombre y número de parámetros.
     * No se comparan los tipos: JpaRepository usa genéricos que en tiempo de ejecución son Object.
     */
    private Method buscarMetodo(String nombre, int numParametros) {
        List<Method> candidatos = new ArrayList<>();
        for (Method m : getClass().getMethods()) {
            if (m.getDeclaringClass() == Object.class
                    || Modifier.isStatic(m.getModifiers())
                    || m.isBridge()
                    || m.isSynthetic()) {
                continue;
            }
            if (m.getName().equals(nombre) && m.getParameterCount() == numParametros) {
                candidatos.add(m);
            }
        }
        if (candidatos.isEmpty()) {
            throw new UnsupportedOperationException(String.format(
                    "%s no implementa %s(...) con %d parámetro(s). Agrega un método público con ese "
                            + "nombre y número de parámetros al fake.",
                    getClass().getSimpleName(), nombre, numParametros));
        }
        if (candidatos.size() > 1) {
            throw new IllegalStateException(String.format(
                    "%s tiene %d métodos públicos ambiguos llamados %s con %d parámetro(s).",
                    getClass().getSimpleName(), candidatos.size(), nombre, numParametros));
        }
        Method elegido = candidatos.get(0);
        elegido.trySetAccessible();
        return elegido;
    }

    private static void exigirId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("The given id must not be null");
        }
    }
}
