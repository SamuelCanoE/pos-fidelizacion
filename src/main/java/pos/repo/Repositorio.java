package pos.repo;
import java.util.*;
import pos.modelo.Modelo.*;
/** ADT de persistencia: la lógica de negocio no sabe si abajo hay PostgreSQL u otra cosa (en las pruebas, una lista en memoria). */
public interface Repositorio {
    List<Producto> productos();
    Optional<Producto> producto(int id);
    List<Cliente> clientes();
    Optional<Cliente> cliente(int id);
    int crearCliente(String nombre, int edad, boolean embarazada);
    List<int[]> uniones();                       // pares {padre, hijo} en el orden en que se crearon
    void guardarUnion(int padre, int hijo);
    /** Guarda la venta, sus ítems y los puntos del cliente de forma atómica. */
    Recibo guardarVenta(int clienteId, String clienteNombre, Map<Producto, Integer> items, long subtotal, long descuento, long total, int puntos);
    List<Recibo> ventasDeHoy();
}
