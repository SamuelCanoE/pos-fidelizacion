package pos.ds;
import java.util.*;
import pos.adt.IFila;
/** Cola FIFO con lista enlazada y punteros a ambos extremos: ingresar y atender en O(1). */
public class Cola<T> implements IFila<T> {
    private static class Nodo<T> { T v; Nodo<T> sig; Nodo(T v) { this.v = v; } }
    private Nodo<T> ini, fin; private int n;
    public void ingresar(T x) { Nodo<T> q = new Nodo<>(x); if (fin == null) ini = q; else fin.sig = q; fin = q; n++; }
    public T atender() { if (ini == null) return null; T v = ini.v; ini = ini.sig; if (ini == null) fin = null; n--; return v; }
    public T proximo() { return ini == null ? null : ini.v; }
    public int size() { return n; }
    public List<T> interno() { return ver(); }
    public List<T> ver() { List<T> r = new ArrayList<>(); for (Nodo<T> a = ini; a != null; a = a.sig) r.add(a.v); return r; }
}
