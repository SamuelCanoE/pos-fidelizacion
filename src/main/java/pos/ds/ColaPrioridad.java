package pos.ds;
import java.util.*;
import pos.adt.IFila;
/** Min-Heap binario (arreglo base 1): ingresar y atender en O(log n), proximo en O(1). */
public class ColaPrioridad<T> implements IFila<T> {
    private Object[] h = new Object[17]; private int n; private final Comparator<T> cmp;
    public ColaPrioridad(Comparator<T> cmp) { this.cmp = cmp; }
    @SuppressWarnings("unchecked") private T at(int i) { return (T) h[i]; }
    private boolean mayor(int i, int j) { return cmp.compare(at(i), at(j)) > 0; }
    private void swap(int i, int j) { Object t = h[i]; h[i] = h[j]; h[j] = t; }
    private void subir(int k) { while (k > 1 && mayor(k / 2, k)) { swap(k / 2, k); k /= 2; } }
    private void bajar(int k) {
        while (2 * k <= n) { int j = 2 * k; if (j < n && mayor(j, j + 1)) j++; if (!mayor(k, j)) break; swap(k, j); k = j; }
    }
    public void ingresar(T x) { if (n + 1 == h.length) h = Arrays.copyOf(h, h.length * 2); h[++n] = x; subir(n); }
    public T atender() { if (n == 0) return null; T min = at(1); swap(1, n); h[n--] = null; bajar(1); return min; }
    public T proximo() { return n == 0 ? null : at(1); }
    public int size() { return n; }
    /** Los elementos en el orden del arreglo del heap (posiciones 1..n), no en orden de atención. */
    public List<T> interno() { List<T> r = new ArrayList<>(); for (int i = 1; i <= n; i++) r.add(at(i)); return r; }
    /** Orden de atención sin alterar la cola (copia el heap y lo vacía): O(n log n), solo para mostrar. */
    public List<T> ver() {
        ColaPrioridad<T> c = new ColaPrioridad<>(cmp); c.h = h.clone(); c.n = n;
        List<T> r = new ArrayList<>(); while (c.n > 0) r.add(c.atender()); return r;
    }
}
