package pos.ds;
import java.util.*;
/** Lista doblemente enlazada con cabeza y cola: agregarFinal O(1) sin redimensionar, recorrido en ambos sentidos. */
public class ListaDoble<T> {
    private static class Nodo<T> { T v; Nodo<T> ant, sig; Nodo(T v) { this.v = v; } }
    private Nodo<T> ini, fin; private int n;
    public void agregarFinal(T x) {
        Nodo<T> q = new Nodo<>(x);
        if (fin == null) ini = fin = q; else { fin.sig = q; q.ant = fin; fin = q; }
        n++;
    }
    public int size() { return n; }
    public List<T> desdeElInicio() { List<T> r = new ArrayList<>(); for (Nodo<T> a = ini; a != null; a = a.sig) r.add(a.v); return r; }
    public List<T> desdeElFinal() { List<T> r = new ArrayList<>(); for (Nodo<T> a = fin; a != null; a = a.ant) r.add(a.v); return r; }
}
