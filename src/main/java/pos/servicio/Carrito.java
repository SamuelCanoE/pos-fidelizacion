package pos.servicio;
import java.util.*;
import pos.adt.ICarrito;
import pos.ds.*;
import pos.modelo.Modelo.Producto;
/** Carrito = Bag (contenido) + Pila (historial de escaneos para "Borrar último"). */
public class Carrito implements ICarrito {
    private final Bag<Producto> bolsa = new Bag<>();
    private final Pila<Producto> acciones = new Pila<>();
    public void agregar(Producto p) { bolsa.add(p); acciones.push(p); }
    public Producto deshacer() { Producto p = acciones.pop(); if (p != null) bolsa.quitarUltimo(); return p; }
    public Map<Producto, Integer> resumen() { Map<Producto, Integer> m = new LinkedHashMap<>(); for (Producto p : bolsa) m.merge(p, 1, Integer::sum); return m; }
    public long subtotal() { long s = 0; for (Producto p : bolsa) s += p.precio(); return s; }
    public int cantidad() { return bolsa.size(); }
    public List<Producto> acciones() { return acciones.contenido(); }
    public List<Producto> bolsa() { List<Producto> l = new ArrayList<>(); for (Producto p : bolsa) l.add(p); return l; }
}
