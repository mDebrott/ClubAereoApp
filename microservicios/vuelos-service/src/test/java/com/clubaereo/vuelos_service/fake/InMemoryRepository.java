package com.clubaereo.vuelos_service.fake;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Base reutilizable para repositorios "falsos" que viven en memoria.
 *
 * <p>Idea: en vez de implementar a mano los ~30 métodos de {@code JpaRepository}, esta clase
 * implementa solo los básicos (save, findById, findAll, existsById, count, deleteById) y crea,
 * con {@link java.lang.reflect.Proxy} de Java (no de Mockito), un objeto que SÍ implementa la
 * interfaz del repositorio. Cada llamada a ese objeto se redirige a un método público de este
 * fake con el mismo nombre y el mismo número de parámetros.
 *
 * <p>Los repositorios con consultas propias (findByXxx, @Query...) extienden esta clase y agregan
 * esos métodos a mano como métodos públicos.
 *
 * <p>Reglas para quien extienda esta clase:
 * <ul>
 *   <li>La clase hija debe ser {@code public} y sus consultas, métodos {@code public}.</li>
 *   <li>El tipo de retorno debe coincidir con el de la interfaz (si la interfaz devuelve
 *       {@code long}, devuelve {@code long}/{@code Long}, no {@code int}).</li>
 * </ul>
 *
 * @param <T> tipo de la entidad (su id debe ser {@code Long})
 * @param <R> interfaz del repositorio real (por ejemplo {@code AsignacionRepository})
 */
public abstract class InMemoryRepository<T, R> {

    /** Las "filas" de la tabla, en orden de inserción. Las clases hijas lo usan en sus consultas. */
    protected final Map<Long, T> datos = new LinkedHashMap<>();

    private final Class<R> tipoRepositorio;
    private long siguienteId = 1L;
    private R proxy;

    protected InMemoryRepository(Class<R> tipoRepositorio) {
        this.tipoRepositorio = tipoRepositorio;
    }

    /** Cómo leer el id de la entidad (las entidades no comparten una superclase). */
    protected abstract Long obtenerId(T entidad);

    /** Cómo escribir el id en la entidad. */
    protected abstract void asignarId(T entidad, Long id);

    // ------------------------------------------------------------------
    // Operaciones básicas de JpaRepository
    // ------------------------------------------------------------------

    /**
     * Si la entidad no tiene id, le asigna el siguiente. Si ya tiene uno explícito, lo respeta y
     * se asegura de que los ids automáticos siguientes no colisionen con él.
     */
    public <S extends T> S save(S entidad) {
        Long id = obtenerId(entidad);
        if (id == null) {
            id = siguienteId++;
            asignarId(entidad, id);
        } else if (id >= siguienteId) {
            siguienteId = id + 1;
        }
        datos.put(id, entidad);
        return entidad;
    }

    public Optional<T> findById(Long id) {
        exigirId(id);
        return Optional.ofNullable(datos.get(id));
    }

    /** Devuelve una copia de la lista, igual que una consulta real (no expone el almacén). */
    public List<T> findAll() {
        return new ArrayList<>(datos.values());
    }

    public boolean existsById(Long id) {
        exigirId(id);
        return datos.containsKey(id);
    }

    public long count() {
        return datos.size();
    }

    /** Igual que Spring Data: borrar un id que no existe no es un error. */
    public void deleteById(Long id) {
        exigirId(id);
        datos.remove(id);
    }

    // ------------------------------------------------------------------
    // El repositorio "real" (proxy)
    // ------------------------------------------------------------------

    /** Devuelve el repositorio (interfaz real) respaldado por este fake. Siempre el mismo objeto. */
    @SuppressWarnings("unchecked")
    public R repo() {
        if (proxy == null) {
            proxy = (R) Proxy.newProxyInstance(
                    tipoRepositorio.getClassLoader(),
                    new Class<?>[] { tipoRepositorio },
                    (p, metodo, args) -> despachar(p, metodo, args));
        }
        return proxy;
    }

    private Object despachar(Object proxyLlamado, Method metodo, Object[] args) throws Throwable {
        String nombre = metodo.getName();
        int numeroArgs = args == null ? 0 : args.length;

        // Métodos de Object: el Proxy también nos los envía a nosotros.
        if (nombre.equals("equals") && numeroArgs == 1) {
            return proxyLlamado == args[0];
        }
        if (nombre.equals("hashCode") && numeroArgs == 0) {
            return System.identityHashCode(proxyLlamado);
        }
        if (nombre.equals("toString") && numeroArgs == 0) {
            return "Proxy en memoria de " + tipoRepositorio.getSimpleName();
        }

        Method destino = buscarMetodo(nombre, numeroArgs);
        try {
            return destino.invoke(this, args);
        } catch (InvocationTargetException e) {
            // Que el test vea la excepción real (ConflictoException, IllegalArgumentException...)
            // y no la envoltura de reflexión.
            throw e.getCause();
        }
    }

    /**
     * Busca un método público del fake por NOMBRE y NÚMERO de parámetros. No se comparan tipos:
     * JpaRepository usa genéricos, y tras el borrado de tipos {@code findById(ID)} llega como
     * {@code findById(Object)}.
     */
    private Method buscarMetodo(String nombre, int numeroArgs) {
        List<Method> candidatos = new ArrayList<>();
        for (Method m : getClass().getMethods()) {
            boolean esDeObject = m.getDeclaringClass() == Object.class;
            if (!esDeObject && !m.isBridge() && !m.isSynthetic()
                    && m.getName().equals(nombre) && m.getParameterCount() == numeroArgs) {
                candidatos.add(m);
            }
        }
        if (candidatos.isEmpty()) {
            throw new UnsupportedOperationException(
                    getClass().getSimpleName() + " no implementa " + nombre + "(" + numeroArgs
                            + " parámetro" + (numeroArgs == 1 ? "" : "s") + "). Agrega un método "
                            + "público con ese nombre y esa cantidad de parámetros al fake.");
        }
        if (candidatos.size() > 1) {
            throw new IllegalStateException(
                    getClass().getSimpleName() + " tiene " + candidatos.size() + " métodos públicos "
                            + nombre + "(" + numeroArgs + " parámetros): el despacho por nombre y "
                            + "cantidad de parámetros es ambiguo. Renombra o elimina uno.");
        }
        Method destino = candidatos.get(0);
        destino.setAccessible(true);
        return destino;
    }

    // ------------------------------------------------------------------
    // Ayudas para imitar la semántica de SQL en las consultas de las clases hijas
    // ------------------------------------------------------------------

    /** Spring Data valida esto en SimpleJpaRepository: id nulo => IllegalArgumentException. */
    private static void exigirId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("The given id must not be null");
        }
    }

    /** SQL {@code a = b}: si cualquiera es NULL, el resultado NO es verdadero. */
    protected static boolean sqlIgual(Object a, Object b) {
        return a != null && b != null && a.equals(b);
    }

    /** SQL {@code a <> b}: si cualquiera es NULL, el resultado NO es verdadero. */
    protected static boolean sqlDistinto(Object a, Object b) {
        return a != null && b != null && !a.equals(b);
    }
}
