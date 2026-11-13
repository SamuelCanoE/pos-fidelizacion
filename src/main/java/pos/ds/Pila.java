package pos.ds;
import java.util.*;
/** Pila LIFO sobre arreglo dinámico: push y pop en O(1) (amortizado el primero). */
public class Pila<T> {
    private Object[] a = new Object[16]; private int n;
    public void push(T x) { if (n == a.length) a = Arrays.copyOf(a, n * 2); a[n++] = x; }
    @SuppressWarnings("unchecked")
    public T pop() { if (n == 0) return null; T x = (T) a[--n]; a[n] = null; return x; }
    public int size() { return n; }
    /** Contenido del tope hacia el fondo, sin modificar la pila. */
    @SuppressWarnings("unchecked")
    public List<T> contenido() { List<T> r = new ArrayList<>(); for (int i = n - 1; i >= 0; i--) r.add((T) a[i]); return r; }
}
