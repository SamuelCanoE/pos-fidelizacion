package pos.repo;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import pos.modelo.Modelo.*;

/** Implementación con JDBC puro sobre PostgreSQL (consultas parametrizadas, venta en una sola transacción). */
public class RepositorioJdbc implements Repositorio {
    private static final Logger LOG = Logger.getLogger(RepositorioJdbc.class.getName());
    private final Connection cx;
    public RepositorioJdbc(Connection cx) { this.cx = cx; }

    public void crearEsquema(String sql) {
        try (Statement s = cx.createStatement()) { s.execute(sql); LOG.info("Esquema de base de datos verificado"); }
        catch (SQLException e) { throw new RepositorioException("No se pudo crear el esquema", e); }
    }
    private static Cliente mapCliente(ResultSet r) throws SQLException { return new Cliente(r.getInt(1), r.getString(2), r.getInt(3), r.getBoolean(4), r.getInt(5)); }
    private static Producto mapProducto(ResultSet r) throws SQLException { return new Producto(r.getInt(1), r.getString(2), r.getLong(3)); }

    public List<Producto> productos() {
        try (Statement s = cx.createStatement(); ResultSet r = s.executeQuery("SELECT id,nombre,precio FROM producto ORDER BY nombre")) {
            List<Producto> l = new ArrayList<>(); while (r.next()) l.add(mapProducto(r)); return l;
        } catch (SQLException e) { throw new RepositorioException("Error leyendo productos", e); }
    }
    public Optional<Producto> producto(int id) {
        try (PreparedStatement p = cx.prepareStatement("SELECT id,nombre,precio FROM producto WHERE id=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(mapProducto(r)) : Optional.empty(); }
        } catch (SQLException e) { throw new RepositorioException("Error leyendo el producto " + id, e); }
    }
    public List<Cliente> clientes() {
        try (Statement s = cx.createStatement(); ResultSet r = s.executeQuery("SELECT id,nombre,edad,embarazada,puntos FROM cliente ORDER BY id")) {
            List<Cliente> l = new ArrayList<>(); while (r.next()) l.add(mapCliente(r)); return l;
        } catch (SQLException e) { throw new RepositorioException("Error leyendo clientes", e); }
    }
    public Optional<Cliente> cliente(int id) {
        try (PreparedStatement p = cx.prepareStatement("SELECT id,nombre,edad,embarazada,puntos FROM cliente WHERE id=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(mapCliente(r)) : Optional.empty(); }
        } catch (SQLException e) { throw new RepositorioException("Error leyendo el cliente " + id, e); }
    }
    public int crearCliente(String nombre, int edad, boolean embarazada) {
        try (PreparedStatement p = cx.prepareStatement("INSERT INTO cliente(nombre,edad,embarazada) VALUES(?,?,?) RETURNING id")) {
            p.setString(1, nombre); p.setInt(2, edad); p.setBoolean(3, embarazada);
            try (ResultSet r = p.executeQuery()) { r.next(); return r.getInt(1); }
        } catch (SQLException e) { throw new RepositorioException("Error creando el cliente", e); }
    }
    public List<int[]> uniones() {
        try (Statement s = cx.createStatement(); ResultSet r = s.executeQuery("SELECT padre,hijo FROM union_familiar ORDER BY id")) {
            List<int[]> l = new ArrayList<>(); while (r.next()) l.add(new int[]{r.getInt(1), r.getInt(2)}); return l;
        } catch (SQLException e) { throw new RepositorioException("Error leyendo las uniones familiares", e); }
    }
    public void guardarUnion(int padre, int hijo) {
        try (PreparedStatement p = cx.prepareStatement("INSERT INTO union_familiar(padre,hijo) VALUES(?,?)")) {
            p.setInt(1, padre); p.setInt(2, hijo); p.executeUpdate();
        } catch (SQLException e) { throw new RepositorioException("Error guardando la unión familiar", e); }
    }
    public Recibo guardarVenta(int clienteId, String clienteNombre, Map<Producto, Integer> items, long subtotal, long descuento, long total, int puntos) {
        try {
            cx.setAutoCommit(false);
            try {
                int vid; String hora;
                try (PreparedStatement p = cx.prepareStatement("INSERT INTO venta(cliente_id,subtotal,descuento,total,puntos_ganados) VALUES(?,?,?,?,?) RETURNING id,to_char(fecha,'HH24:MI')")) {
                    p.setInt(1, clienteId); p.setLong(2, subtotal); p.setLong(3, descuento); p.setLong(4, total); p.setInt(5, puntos);
                    try (ResultSet r = p.executeQuery()) { r.next(); vid = r.getInt(1); hora = r.getString(2); }
                }
                try (PreparedStatement p = cx.prepareStatement("INSERT INTO venta_item(venta_id,producto_id,cantidad) VALUES(?,?,?)")) {
                    for (var e : items.entrySet()) { p.setInt(1, vid); p.setInt(2, e.getKey().id()); p.setInt(3, e.getValue()); p.addBatch(); }
                    p.executeBatch();
                }
                try (PreparedStatement p = cx.prepareStatement("UPDATE cliente SET puntos=puntos+? WHERE id=?")) { p.setInt(1, puntos); p.setInt(2, clienteId); p.executeUpdate(); }
                cx.commit();
                return new Recibo(vid, clienteNombre, subtotal, descuento, total, puntos, hora);
            } catch (SQLException | RuntimeException e) { cx.rollback(); throw e; }
            finally { cx.setAutoCommit(true); }
        } catch (SQLException e) { throw new RepositorioException("Error guardando la venta (se deshizo la transacción)", e); }
    }
    public List<Recibo> ventasDeHoy() {
        String sql = "SELECT v.id,c.nombre,v.subtotal,v.descuento,v.total,v.puntos_ganados,to_char(v.fecha,'HH24:MI') FROM venta v LEFT JOIN cliente c ON c.id=v.cliente_id WHERE v.fecha>=current_date ORDER BY v.id";
        try (Statement s = cx.createStatement(); ResultSet r = s.executeQuery(sql)) {
            List<Recibo> l = new ArrayList<>();
            while (r.next()) l.add(new Recibo(r.getInt(1), r.getString(2), r.getLong(3), r.getLong(4), r.getLong(5), r.getInt(6), r.getString(7)));
            return l;
        } catch (SQLException e) { throw new RepositorioException("Error leyendo las ventas de hoy", e); }
    }
}
