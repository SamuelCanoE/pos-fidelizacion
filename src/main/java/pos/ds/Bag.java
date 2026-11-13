package pos.ds;
import java.util.Iterator;
/** Bolsa: sin orden relevante, admite duplicados. Lista simple con inserción en la cabeza: add O(1). */
public class Bag<T> implements Iterable<T> {
    private class Nodo { T v; Nodo sig; Nodo(T v, Nodo s) { this.v = v; sig = s; } }
    private Nodo cabeza; private int n;
    public void add(T x) { cabeza = new Nodo(x, cabeza); n++; }
    /** Quita el último agregado (la cabeza): O(1). Lo usa el deshacer de la Pila. */
    public T quitarUltimo() { if (cabeza == null) return null; T v = cabeza.v; cabeza = cabeza.sig; n--; return v; }
    public int size() { return n; }
    public Iterator<T> iterator() {
        return new Iterator<>() {
            Nodo a = cabeza;
            public boolean hasNext() { return a != null; }
            public T next() { T v = a.v; a = a.sig; return v; }
        };
    }
}
