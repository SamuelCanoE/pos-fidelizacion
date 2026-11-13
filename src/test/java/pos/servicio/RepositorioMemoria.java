package pos.servicio;
import java.util.*;
import pos.modelo.Modelo.*;
import pos.repo.Repositorio;

/** Repositorio falso para pruebas: mismos contratos que PostgreSQL, pero en listas. */
public class RepositorioMemoria implements Repositorio {
    final List<Producto> productos = new ArrayList<>(List.of(new Producto(1, "Manzana", 1500), new Producto(2, "Leche", 4200), new Producto(3, "Pan", 3000), new Producto(4, "Café", 14000), new Producto(5, "Aceite", 11000)));
    final List<Cliente> clientes = new ArrayList<>(List.of(new Cliente(1, "Ana Gómez", 34, false, 12), new Cliente(2, "Carlos Gómez", 67, false, 9), new Cliente(3, "Laura Ruiz", 29, true, 0), new Cliente(4, "Pedro Ruiz", 41, false, 0), new Cliente(5, "Marta Soto", 72, false, 0)));
    final List<int[]> uniones = new ArrayList<>();
    final List<Recibo> ventas = new ArrayList<>();
    public List<Producto> productos() { return List.copyOf(productos); }
    public Optional<Producto> producto(int id) { return productos.stream().filter(p -> p.id() == id).findFirst(); }
    public List<Cliente> clientes() { return List.copyOf(clientes); }
    public Optional<Cliente> cliente(int id) { return clientes.stream().filter(c -> c.id() == id).findFirst(); }
    public int crearCliente(String nombre, int edad, boolean embarazada) { int id = clientes.size() + 1; clientes.add(new Cliente(id, nombre, edad, embarazada, 0)); return id; }
    public List<int[]> uniones() { return List.copyOf(uniones); }
    public void guardarUnion(int padre, int hijo) { uniones.add(new int[]{padre, hijo}); }
    public Recibo guardarVenta(int clienteId, String nombre, Map<Producto, Integer> items, long sub, long desc, long total, int pts) {
        for (int i = 0; i < clientes.size(); i++) { Cliente c = clientes.get(i); if (c.id() == clienteId) clientes.set(i, new Cliente(c.id(), c.nombre(), c.edad(), c.embarazada(), c.puntos() + pts)); }
        Recibo r = new Recibo(ventas.size() + 1, nombre, sub, desc, total, pts, "00:00"); ventas.add(r); return r;
    }
    public List<Recibo> ventasDeHoy() { return List.copyOf(ventas); }
}
