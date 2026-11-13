package pos.adt;
import java.util.List;
import java.util.Map;
import pos.modelo.Modelo.Producto;
/** ADT del carrito: define QUÉ hace, no CÓMO (Bag + Pila viven en la implementación). */
public interface ICarrito {
    void agregar(Producto p);
    Producto deshacer();              // null si no hay nada que deshacer
    Map<Producto, Integer> resumen(); // producto -> cantidad
    long subtotal();
    int cantidad();
    List<Producto> acciones();   // contenido de la pila, del tope hacia el fondo
    List<Producto> bolsa();      // contenido del Bag tal como está guardado
}
