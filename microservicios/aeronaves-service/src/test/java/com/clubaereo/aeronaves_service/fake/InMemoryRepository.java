package com.clubaereo.aeronaves_service.fake;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Base para los FAKES de repositorio (sin Mockito).
 *
 * Problema: JpaRepository tiene ~30 métodos y no queremos implementarlos todos.
 * Solución: guardamos los datos en un Map (la "base de datos" en memoria) y usamos
 * java.lang.reflect.Proxy (parte de Java, NO de Mockito) para crear un objeto que
 * implementa la interfaz del repositorio. Cada llamada se redirige a un método
 * público de esta clase (o de la subclase) con el mismo nombre y número de parámetros.
 *
 * @param <T> entidad que se guarda
 * @param <R> interfaz del repositorio (ej: AeronaveRepository)
 */
public abstract class InMemoryRepository<T, R> {

    protected final Map<Long, T> datos = new LinkedHashMap<>();
    private long siguienteId = 1L;

    protected abstract Long getId(T entidad);

    protected abstract void setId(T entidad, Long id);

    // ---- CRUD básico (lo que JpaRepository ofrece y los services usan) ----

    public T save(T entidad) {
        if (getId(entidad) == null) {
            setId(entidad, siguienteId++);
        } else {
            siguienteId = Math.max(siguienteId, getId(entidad) + 1);
        }
        datos.put(getId(entidad), entidad);
        return entidad;
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(datos.get(id));
    }

    public List<T> findAll() {
        return new ArrayList<>(datos.values());
    }

    public boolean existsById(Long id) {
        return datos.containsKey(id);
    }

    public long count() {
        return datos.size();
    }

    public void deleteById(Long id) {
        datos.remove(id);
    }

    // ---- Construcción del repositorio falso ----

    @SuppressWarnings("unchecked")
    public R comoRepositorio(Class<R> tipo) {
        return (R) Proxy.newProxyInstance(
                tipo.getClassLoader(),
                new Class<?>[] { tipo },
                (proxy, metodo, args) -> {
                    if (metodo.getDeclaringClass() == Object.class) {
                        return switch (metodo.getName()) {
                            case "toString" -> "Fake de " + tipo.getSimpleName();
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            default -> throw new UnsupportedOperationException(metodo.getName());
                        };
                    }
                    Method implementacion = buscarImplementacion(metodo);
                    if (implementacion == null) {
                        throw new UnsupportedOperationException(
                                "El fake no implementa " + metodo.getName()
                                        + ". Agrégalo como método público en " + getClass().getSimpleName());
                    }
                    try {
                        implementacion.setAccessible(true);
                        return implementacion.invoke(this, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause(); // que el test vea la excepción real
                    }
                });
    }

    private Method buscarImplementacion(Method pedido) {
        for (Method m : getClass().getMethods()) {
            if (m.getDeclaringClass() != Object.class
                    && !m.getName().equals("comoRepositorio")
                    && m.getName().equals(pedido.getName())
                    && m.getParameterCount() == pedido.getParameterCount()) {
                return m;
            }
        }
        return null;
    }
}
