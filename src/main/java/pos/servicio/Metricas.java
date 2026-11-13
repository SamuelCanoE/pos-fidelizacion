package pos.servicio;
import java.util.*;
import java.util.function.Supplier;
/** Mide cuánto tarda cada operación (llamadas, promedio y máximo) para comparar memoria vs. base de datos. */
public class Metricas {
    public record Medicion(String operacion, long llamadas, double promedioUs, double maxUs) {}
    private static final class Acum { long n, total, max; }
    private final Map<String, Acum> datos = new TreeMap<>();

    public synchronized void registrar(String operacion, long nanos) {
        Acum a = datos.computeIfAbsent(operacion, k -> new Acum());
        a.n++; a.total += nanos; a.max = Math.max(a.max, nanos);
    }
    public <T> T medir(String operacion, Supplier<T> accion) {
        long t0 = System.nanoTime();
        try { return accion.get(); } finally { registrar(operacion, System.nanoTime() - t0); }
    }
    public void ejecutar(String operacion, Runnable accion) {
        long t0 = System.nanoTime();
        try { accion.run(); } finally { registrar(operacion, System.nanoTime() - t0); }
    }
    public synchronized List<Medicion> resumen() {
        List<Medicion> l = new ArrayList<>();
        for (var e : datos.entrySet()) {
            Acum a = e.getValue();
            l.add(new Medicion(e.getKey(), a.n, Math.round(a.total / (double) a.n / 10.0) / 100.0, Math.round(a.max / 10.0) / 100.0));
        }
        return l;
    }
    public synchronized void reiniciar() { datos.clear(); }
}
